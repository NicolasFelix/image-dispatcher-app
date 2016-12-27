package fr.perso.nfelix.app.utils.sgbd;

import fr.perso.nfelix.app.ui.config.DatabaseConfig;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * JdbcUtils class
 *
 * @author N.FELIX
 */
public abstract class JdbcUtils {

  /**
   * create JDBC connection
   *
   * @param config database configuration
   * @return new Connection
   * @throws ClassNotFoundException in case of
   * @throws SQLException           in case of
   */
  public static Connection connect(DatabaseConfig config)
      throws ClassNotFoundException, SQLException {

    Class.forName(config.getDriver());

    return DriverManager.getConnection(config.getUrl(), config.getUser(), config.getPassword());
  }

  /**
   * close quielty connection
   *
   * @param con connexion to be cclosed
   */
  public static void closeQuietly(Connection con) {

    if(con != null) {
      try {
        con.close();
      }
      catch(SQLException ignored) {
        // osef
      }
    }
  }
}
