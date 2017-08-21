package fr.perso.nfelix.app.ui.typedef;

import fr.perso.nfelix.app.utils.DSystemUtils;

/**
 * define constant
 *
 * @author N.FELIX
 */
@SuppressWarnings("all")
public abstract class Constants {

  public final static String APP_NAME       = "ImgDispatcherApp";
  public final static String SPRING_CONTEXT = "classpath:/config/ImgDispatcherApp-springContext.xml";

  // images
  public final static String APP_ICON              = "/imgDispatcherApp-icon.png";
  public final static String SPLASH_IMAGE          = "/images/splash.png";
  // menu icons (16px * 16px)
  public final static String EXIT_MENU_ICON        = "/images/menu/exit-16x16.png";
  public final static String HELP_MENU_ICON        = "/images/menu/help-16x16.png";
  public final static String EXPERT_MENU_ICON      = "/images/menu/expert-16x16.png";
  public final static String LOGS_MENU_ICON        = "/images/menu/logs-16x16.png";
  public final static String USERGUIDE_MENU_ICON   = "/images/menu/userguide-16x16.png";
  public final static String WARN_MENU_ICON        = "/images/menu/warn-16x16.png";
  public final static String NOTALLOWED_MENU_ICON  = "/images/menu/notAllowed-16x16.png";
  public final static String CHECK_MENU_ICON       = "/images/menu/check-16x16.png";
  public final static String STATS_MENU_ICON       = "/images/menu/stats-16x16.png";
  public final static String CONSISTENCY_MENU_ICON = "/images/menu/consistency-16x16.png";
  public final static String COMPARISON_MENU_ICON  = "/images/menu/comparison-16x16.png";
  public final static String TOOLS_MENU_ICON       = "/images/menu/tools-16x16.png";
  public final static String STORAGE_MENU_ICON     = "/images/menu/storage-16x16.png";
  public final static String STORAGE_FS_MENU_ICON  = "/images/menu/storage-file-16x16.png";
  public final static String STORAGE_BDD_MENU_ICON = "/images/menu/storage-bdd-16x16.png";
  public final static String EYES_MENU_ICON        = "/images/menu/eyes-16x16.png";

  // icons (32px * 32px)
  public final static String EXIT_ICON      = "/images/exit-32x32.png";
  public final static String HELP_ICON      = "/images/help-32x32.png";
  public final static String WARN_ICON      = "/images/warn-32x32.png";
  public final static String NOTALOWED_ICON = "/images/notAllowed-32x32.png";
  public final static String CHECK_ICON     = "/images/check-32x32.png";
  public final static String STATS_ICON     = "/images/stats-32x32.png";

  // icons (16px * 16px)
  public final static String RUN_ICON    = "/images/run-16x16.png";
  public final static String STOP_ICON   = "/images/stop-16x16.png";
  public final static String EMPTY_ICON  = "/images/empty-16x16.png";
  public final static String FOLDER_ICON = "/images/folder-16x16.png";
  public final static String QUERY_ICON  = "/images/query-16x16.png";
  public final static String ADD_ICON    = "/images/add-16x16.png";
  public final static String DELETE_ICON = "/images/delete-16x16.png";

  // status
  public final static String STATUS_OK_ICON   = "/images/status/ok-16x16.png";
  public final static String STATUS_WARN_ICON = "/images/status/warn-16x16.png";

  // menu icons (16px * 16px)
  public final static String STATUS_KO_ICON = "/images/status/ko-16x16.png";

  // database
  public final static String ORACLE_DB_ICON       = "/images/rdbms/oracle.png";
  public final static String POSTGRE_DB_ICON      = "/images/rdbms/postgresql.png";
  public final static String MYSQL_DB_ICON        = "/images/rdbms/mysql.png";
  // format
  public final static String PDF_FORMAT_ICON      = "/images/format/pdf.png";
  public final static String HTML_FORMAT_ICON     = "/images/format/html.png";
  public final static String EXCEL_FORMAT_ICON    = "/images/format/excel.png";
  // format
  public final static String DEBUG_LOG_LEVEL_ICON = "/images/log/debug.png";
  public final static String INFO_LOG_LEVEL_ICON  = "/images/log/info.png";
  public final static String OFF_LOG_LEVEL_ICON   = "/images/log/off.png";

  // theme
  public final static String DARK_THEME_ICON  = "/images/theme/dark.png";
  public final static String LIGHT_THEME_ICON = "/images/theme/light.png";

  public final static String TEST_ICON = "/images/menu/tools-16x16.png";

  // common extensions
  public final static String FXML_EXTENSION = ".fxml";
  public final static String I18N_EXTENSION = ".properties";
  public final static String CSS_EXTENSION  = ".css";

  // view
  public final static String FXML_ROOT     = "/fr/perso/nfelix/app/ui/views/";
  public final static String FXML_MAIN     = "main";
  public final static String FXML_HOME     = "home";
  public final static String FXML_SETTINGS = "settings";

  // main css style
  public final static String STYLES_ROOT = "/styles/";
  public final static String MAIN_STYLE  = STYLES_ROOT + "appStyle-";

  // message keys
  public final static String I18N_ROOT = "fr.perso.nfelix.app.ui.views.i18n.";

  public final static String ABOUT_TITLE = "about.title";

  public final static String WINDOWS_POS_X      = "windowsPosX";
  public final static String WINDOWS_POS_Y      = "windowsPosY";
  public final static String WINDOWS_POS_WIDTH  = "windowsPosWidth";
  public final static String WINDOWS_POS_HEIGHT = "windowsPosHeight";
  public final static String WINDOWS_MAXIMIZED  = "windowsMaximized";
  public final static int    WINDOWS_MIN_WIDTH  = 600;
  public final static int    WINDOWS_MIN_HEIGHT = 400;

  public final static String LINE_SEP = DSystemUtils.getProperty("line.separator");

  public final static String ENCODED_PWD = "$2a$10$ESxDFX3KJNcR9kWTaILWfOsVypbkVDfTLb7xkWKPcoxXIBH9EkLTm";

  /** Short date save pattern */
  public static final String SAVE_PATTERN_DATE    = "yyyyMMdd";
  /** Date display pattern */
  public static final String DISPLAY_PATTERN_DATE = "dd/MM/yyyy";
  public static final String FORMAT_DURATION_HMS  = "H:mm:ss";

}
