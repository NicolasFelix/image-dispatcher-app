package fr.perso.nfelix.app.ui.config;

import static fr.perso.nfelix.app.ui.typedef.Constants.*;

import ch.qos.logback.classic.Level;
import fr.perso.nfelix.app.ui.config.editor.ComboWithIconPropertyEditor.ComboWithIconStruct;
import fr.perso.nfelix.app.utils.ApplicationHolder;
import fr.perso.nfelix.app.utils.fx.AbstractPropertySheetBean;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * GlobalConfig
 *
 * @author N.FELIX
 */
@Slf4j
@EqualsAndHashCode(doNotUseGetters = true, callSuper = true, exclude = { "logLevel" })
public class GlobalConfig extends AbstractPropertySheetBean {

  final static String[] LOGLEVEL_KEY = { "logLevel", "Niveau de logs" };
  final static String[] THEME_KEY    = { "theme", "Th\u00e8me" };
  final static String[] DUMPSTEP_KEY = { "dumpStep", "Intervalle de log" };

  // Log configuration
  private final static String DEBUG_LEVEL = "DEBUG";
  private final static String INFO_LEVEL  = "INFO";
  private final static String OFF_LEVEL   = "OFF";

  private final static ComboWithIconStruct   LOG_DEBUG            = new ComboWithIconStruct(DEBUG_LEVEL, DEBUG_LEVEL, DEBUG_LOG_LEVEL_ICON);
  private final static ComboWithIconStruct   LOG_INFO             = new ComboWithIconStruct(INFO_LEVEL, INFO_LEVEL, INFO_LOG_LEVEL_ICON);
  private final static ComboWithIconStruct   LOG_OFF              = new ComboWithIconStruct(OFF_LEVEL, OFF_LEVEL, OFF_LOG_LEVEL_ICON);
  final static         ComboWithIconStruct[] AVAILABLE_LOG_LEVELS = { LOG_DEBUG, LOG_INFO, LOG_OFF };

  public final static  String                DARK_THEME       = "dark";
  /** light theme constant */
  public final static  String                LIGHT_THEME      = "light";
  private final static ComboWithIconStruct   THEME_DARK       = new ComboWithIconStruct(DARK_THEME, DARK_THEME, DARK_THEME_ICON);
  private final static ComboWithIconStruct   THEME_LIGHT      = new ComboWithIconStruct(LIGHT_THEME, LIGHT_THEME, LIGHT_THEME_ICON);
  final static         ComboWithIconStruct[] AVAILABLE_THEMES = { THEME_DARK, THEME_LIGHT };
  @Setter
  @Getter
  private String logLevel = INFO_LEVEL;

  @Setter
  @Getter
  private String theme = LIGHT_THEME;

  @Setter
  @Getter
  private Integer dumpStep = 50;

  public GlobalConfig(String category, ResourceBundle resources) {
    super(category, resources);
  }

  /**
   * check if structure is valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    return true;
  }

  @Override
  public String[] getPropertyNames() {
    return new String[] { LOGLEVEL_KEY[0], THEME_KEY[0], DUMPSTEP_KEY[0] };
  }

  @Override
  public Class getPropertyType(final String propName) {

    if(DUMPSTEP_KEY[0].equals(propName)) {
      return Integer.class;
    }
    return super.getPropertyType(propName);
  }

  public void resetLogLevel() {
    Logger root = LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
    if(root instanceof ch.qos.logback.classic.Logger) {
      try {
        final Level level = Level.valueOf(logLevel);
        ((ch.qos.logback.classic.Logger) root).setLevel(level);
      }
      catch(Exception e) {
        LOGGER.error("error while getting log level '" + logLevel + "': " + e.getLocalizedMessage(), e);
      }
    }
  }

  @Override
  public GlobalConfig clone() {
    try {
      return (GlobalConfig) super.clone();
    }
    catch(CloneNotSupportedException e) {
      LOGGER.error("GlobalConfig clone failed: " + e.getLocalizedMessage(), e);
    }
    return null;
  }

  @Override
  public void readAdditionalProperties(SubnodeConfiguration iniFile) {
    applyGlobalChanges();
  }

  public void applyGlobalChanges() {
    resetLogLevel();
    resetTheme();
  }

  /**
   * reset theme
   */
  public void resetTheme() {
    ApplicationHolder.getINSTANCE().getMainApp().switchStyle(MAIN_STYLE + theme + CSS_EXTENSION);
  }
}
