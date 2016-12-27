package fr.perso.nfelix.app.ui.config;

import static fr.perso.nfelix.app.ui.config.DatabaseConfig.DBStatus.*;

import fr.perso.nfelix.app.ui.config.editor.ComboWithIconPropertyEditor;
import fr.perso.nfelix.app.ui.typedef.Constants;
import fr.perso.nfelix.app.ui.typedef.JobConstants;
import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import fr.perso.nfelix.app.utils.fx.AbstractPropertySheetBean;
import fr.perso.nfelix.app.utils.fx.CustomBeanProperty;
import fr.perso.nfelix.app.utils.sgbd.DatabaseType;
import fr.perso.nfelix.app.utils.sgbd.JdbcUtils;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.control.Alert;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * Database configuration
 *
 * @author N.FELIX
 */
@Getter
@Setter
@Slf4j
@ToString(exclude = { "password" })
@EqualsAndHashCode(doNotUseGetters = true, callSuper = true)
public class DatabaseConfig extends AbstractPropertySheetBean {

  public final static String[] PILOTE_KEY   = { "driver", "Pilote JDBC" };
  public final static String[] URL_KEY      = { "url", "Url" };
  public final static String[] USER_KEY     = { "user", "Utilisateur" };
  public final static String[] PASSWORD_KEY = { "password", "Mot de passe" };

  private static final String ORACLE_SEARCH_WORD  = "oracle";
  private static final String POSTGRE_SEARCH_WORD = "postgresql";
  private static final String MYSQL_SEARCH_WORD   = "mysql";
  private static final String P6SPY_SEARCH_WORD   = "p6spy";

  // driver
  private static final String ORACLE_DRIVER   = "oracle.jdbc.OracleDriver";
  private static final String POSTGRE_DRIVER  = "org.postgresql.Driver";
  private static final String MYSQL_DRIVER    = "com.mysql.jdbc.Driver";
  private static final String P6SPY_DRIVER    = "com.p6spy.engine.spy.P6SpyDriver";
  // dialect
  private static final String ORACLE_DIALECT  = "com.digitech.common.dal.sgbd.oracle.CustomOracle10gDialect";
  private static final String POSTGRE_DIALECT = "com.digitech.common.dal.sgbd.postgresql.CustomPostgreSQLDialect";
  private static final String MYSQL_DIALECT   = "com.digitech.common.dal.sgbd.mysql.CustomMySQL5InnoDBDDialect";

  private static final String[][] URL_DRIVER_LOOKUP = { new String[] { P6SPY_SEARCH_WORD, P6SPY_DRIVER, null },
      new String[] { ORACLE_SEARCH_WORD, ORACLE_DRIVER, ORACLE_DIALECT }, new String[] { POSTGRE_SEARCH_WORD, POSTGRE_DRIVER, POSTGRE_DIALECT },
      new String[] { MYSQL_SEARCH_WORD, MYSQL_DRIVER, MYSQL_DIALECT } };

  private final static ComboWithIconPropertyEditor.ComboWithIconStruc ORACLE     = new ComboWithIconPropertyEditor.ComboWithIconStruc(ORACLE_DRIVER,
      ORACLE_SEARCH_WORD, Constants.ORACLE_DB_ICON);
  private final static ComboWithIconPropertyEditor.ComboWithIconStruc POSTGRESQL = new ComboWithIconPropertyEditor.ComboWithIconStruc(POSTGRE_DRIVER,
      POSTGRE_SEARCH_WORD, Constants.POSTGRE_DB_ICON);
  private final static ComboWithIconPropertyEditor.ComboWithIconStruc MYSQL      = new ComboWithIconPropertyEditor.ComboWithIconStruc(MYSQL_DRIVER,
      MYSQL_SEARCH_WORD, Constants.MYSQL_DB_ICON);
  // private final static ComboWithIconStruc[] AVAILABLE_JDBC_DRIVERS = { ORACLE, POSTGRESQL, MYSQL };

  private String driver = ORACLE_DRIVER;
  private String url;
  private String user;
  private String password;

  public enum DBStatus {INVALID, KO, OK}

  public DatabaseConfig(final String category, ResourceBundle resources) {
    this(category, resources, true);
  }

  public DatabaseConfig(String category, ResourceBundle resources, boolean jobConfigAllowed) {
    super(category, resources);
    this.jobConfigParameterAllowed = jobConfigAllowed;
  }

  @Override
  public String[] getPropertyNames() {
    return new String[] { PILOTE_KEY[0], URL_KEY[0], USER_KEY[0], PASSWORD_KEY[0] };
  }

  /**
   * check if structure is valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    guessDriver();
    return StringUtils.isNotBlank(driver) && StringUtils.isNotBlank(url) && StringUtils.isNotBlank(user) && StringUtils.isNotBlank(password);
  }

  private void guessDriver() {
    if(StringUtils.isNotBlank(url)) {
      for(String[] ud : URL_DRIVER_LOOKUP) {
        if(url.contains(ud[0])) {
          driver = ud[1];
          LOGGER.debug("driver set to '{}' according to url: '{}'", driver, url);
          return;
        }
      }
      LOGGER.warn("no driver guessed for url: '{}'", url);
    }
  }

  private String guessDialect() {
    if(StringUtils.isNotBlank(url)) {
      for(String[] ud : URL_DRIVER_LOOKUP) {
        if(url.contains(ud[0]) && ud[2] != null) {
          LOGGER.debug("dialect set to '{}' according to url: '{}'", driver, url);
          return ud[2];
        }
      }
      LOGGER.warn("no dialect guessed for url: '{}'", url);
    }
    return null;
  }

  /**
   * get database type according to settings
   *
   * @return {@link DatabaseType}
   */
  public DatabaseType guessDatabaseType() {
    if(StringUtils.isNotBlank(url)) {
      for(String[] ud : URL_DRIVER_LOOKUP) {
        if(url.contains(ud[0]) && ud[2] != null) {
          DatabaseType ret = null;
          switch( ud[2] ) {
          case ORACLE_DIALECT:
            ret = DatabaseType.ORACLE;
            break;
          case POSTGRE_DIALECT:
            ret = DatabaseType.POSTGRES;
            break;
          case MYSQL_DIALECT:
            ret = DatabaseType.MYSQL;
            break;
          default:
            break;
          }

          if(ret != null) {
            return ret;
          }
        }
      }
      LOGGER.warn("no database type guessed for url: '{}'", url);
    }
    return null;
  }

  public boolean isFakeMethod() {
    return true;
  }

  public void setFakeMethod(boolean value) {
  }

  @SuppressWarnings("EmptyTryBlock")
  public void handleTestMethod(ActionEvent ae, CustomBeanProperty bean) {

    Alert dialog = null;
    DBStatus status = getStatus();
    switch( status ) {
    case INVALID:
      dialog = JavaFXUtils
          .createDialog(resources, Alert.AlertType.WARNING, "jdbc.test.title", "jdbc.test.invalid.settings.header", "jdbc.test.invalid.settings.content");
      break;
    case KO:
      // connect again to have exception
      try(Connection connect = JdbcUtils.connect(this)) {
        // ignored
      }
      catch(ClassNotFoundException | SQLException e) {
        dialog = JavaFXUtils.createDialog(resources, Alert.AlertType.ERROR, "jdbc.test.title", "jdbc.test.ko.header", "jdbc.test.ko.content", null, null,
            new Object[] { getUrl(), e.getLocalizedMessage() }, e);
      }
      break;
    case OK:
      dialog = JavaFXUtils.createDialog(resources, Alert.AlertType.INFORMATION, "jdbc.test.title", "jdbc.test.ok.header", "jdbc.test.ok.content", null, null,
          new Object[] { getUrl() }, null);
      break;
    }

    if(dialog != null) {
      dialog.showAndWait();
    }
  }

  /**
   * get db status
   *
   * @return {@link DBStatus}
   */
  public DBStatus getStatus() {
    DBStatus ret = INVALID;

    if(isValid()) {
      final long start = System.currentTimeMillis();
      LOGGER.debug("connecting to DB: '{}'", this);
      try(Connection connect = JdbcUtils.connect(this)) {
        ret = OK;
      }
      catch(ClassNotFoundException | SQLException e) {
        ret = KO;
      }
      finally {
        LOGGER.debug("connected to DB('{}') {} in {} ms", this, ret, (System.currentTimeMillis() - start));
      }
    }

    return ret;
  }

  /**
   * just to be displayed on home screen
   *
   * @return information
   */
  public String getInfoString() {
    return ((url == null) ? "" : url) + " - " + ((user == null) ? "" : user);
  }

  public void addJobConfigParameters(Properties props) {
    if(jobConfigParameterAllowed) {
      addSafeConfigParameter(props, JobConstants.CW_DIALECT, guessDialect());
      addSafeConfigParameter(props, JobConstants.CW_DRIVER, driver);
      addSafeConfigParameter(props, JobConstants.CW_URL, url);
      addSafeConfigParameter(props, JobConstants.CW_USER, user);
      addSafeConfigParameter(props, JobConstants.CW_PASSWORD, password);
    }
  }

  @Override
  public DatabaseConfig clone() {
    try {
      return (DatabaseConfig) super.clone();
    }
    catch(CloneNotSupportedException e) {
      LOGGER.error("DatabaseConfig clone failed: " + e.getLocalizedMessage(), e);
    }
    return null;
  }
}
