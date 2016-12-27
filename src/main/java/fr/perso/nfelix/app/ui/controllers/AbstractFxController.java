package fr.perso.nfelix.app.ui.controllers;

import static fr.perso.nfelix.app.ui.utils.JavaFXUtils.createDialog;
import static fr.perso.nfelix.app.utils.DJavaUtils.MAVEN_SNAPSHOT_TOKENS;

import fr.perso.nfelix.app.DispatcherConfig;
import fr.perso.nfelix.app.ImgDispatcherUIApp;
import fr.perso.nfelix.app.ui.utils.IRootController;
import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import fr.perso.nfelix.app.ui.utils.SpringContextHolder;
import java.io.*;
import java.net.URL;
import java.util.*;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.TrueFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

/**
 * AbsctractFxController, common asbstraction
 *
 * @author N.FELIX
 */
@Slf4j
public abstract class AbstractFxController extends Parent implements IFxController, IFXWorkableArea, IFXMaskableArea {

  private static final String LOGBACK_PROPERTY_FILE = "logback.properties";
  private static final String LOGBACK_PATH_KEY      = "log_path";
  private static final String LOG_FILE_NAME         = "imgDispatcherApp.log";

  private static final String APPLICATION_PROPS = "/config/application.properties";
  private static final String VERSION_KEY       = "application.version";
  private static final String BUILDNUMBER_KEY   = "application.buildNumber";

  private static final String USERGUIDE_FILE = "imgDispatcherAppUserGuide.html";

  @Getter
  @Setter
  protected ResourceBundle resources;

  @Setter
  private IRootController rootController;

  /**
   * to be call when view has been created, but not yet shown (do not used it for initialize UI as main app is not injected yet)
   */
  public void postInit() {
  }

  public void dispose() {
  }

  public void initUI() {
    // nada...
  }

  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.resources = resources;

    postInit();
  }

  @Override
  public void switchToView(final String fxmlKey) {

    getLogger().debug(">>> switchToView('{}')", fxmlKey);
    try {
      rootController.switchToView(fxmlKey);
    }
    catch(Exception e1) {
      getLogger().error(e1.getLocalizedMessage(), e1);
    }
    finally {
      getLogger().debug("<<< switchToView");
    }
  }

  protected void resetView(final String fxmlKey) {
    getLogger().debug(">>> resetView('{}')", fxmlKey);
    try {
      boolean viewHasBeenRemoved = rootController.resetView(fxmlKey);
      getLogger().debug("view '{}' has {} been removed", fxmlKey, viewHasBeenRemoved ? "" : "NOT");
    }
    finally {
      getLogger().debug("<<< resetView");
    }
  }

  protected String getSafeResourceValue(final String key) {
    return getSafeResourceValue(key, (Object) null);
  }

  protected String getSafeResourceValue(final String key, Object... arguments) {
    if(resources == null) {
      return ">>> resourceBundle is undefined <<<";
    }

    return JavaFXUtils.getSafeResourceValue(resources, key, arguments);
  }

  @Override
  public ImgDispatcherUIApp getMainApp() {
    return rootController.getMainApp();
  }

  @Override
  public DispatcherConfig getConfig() {
    return getMainApp().getConfig();
  }

  protected void quitApp() {

    LOGGER.info(">>> closing application");
    saveConfigIfNeeded();

    getMainApp().saveWindowsPosition();

    try {
      if(SpringContextHolder.getInstance().getSpringContext() != null) {
        LOGGER.info("closing spring context");
        SpringContextHolder.getInstance().getSpringContext().close();
      }
    }
    catch(Exception e) {
      LOGGER.error("error while closing spring context: " + e.getLocalizedMessage(), e);
    }

    Platform.exit();
    LOGGER.info("<<< application closed");
  }

  protected void saveConfigIfNeeded() {
    if(getMainApp().getConfig().isDirty()) {
      try {
        getMainApp().getConfig().save();
      }
      catch(ConfigurationException | IOException e) {
        LOGGER.error("error while saving settings: " + e.getLocalizedMessage(), e);
      }
    }
  }

  /**
   * display user guide
   *
   * @param ae {@link ActionEvent}
   */
  public void showUserGuide(ActionEvent ae) {
    Alert dlg = null;
    Collection<File> userGuidesFiles = FileUtils
        .listFiles(new File(DispatcherConfig.getExecutionPath()), FileFilterUtils.nameFileFilter(USERGUIDE_FILE, IOCase.INSENSITIVE),
            FileFilterUtils.trueFileFilter());
    if(CollectionUtils.isNotEmpty(userGuidesFiles)) {
      JavaFXUtils.openFile(userGuidesFiles.iterator().next());
    }
    else {
      dlg = createDialog(resources, Alert.AlertType.ERROR, "userguide.file.notfound.title", "userguide.file.notfound.header", null, null,
          new Object[] { USERGUIDE_FILE }, null, null);
    }
    if(dlg != null) {
      dlg.showAndWait();
    }
  }

  /**
   * display logs
   *
   * @param ae {@link ActionEvent}
   */
  public void showLogs(ActionEvent ae)
      throws IOException {
    Alert dlg = null;
    Collection<File> logbackFiles = FileUtils
        .listFiles(new File(DispatcherConfig.getExecutionPath()), FileFilterUtils.nameFileFilter(LOGBACK_PROPERTY_FILE, IOCase.SENSITIVE),
            FileFilterUtils.trueFileFilter());

    if(CollectionUtils.isNotEmpty(logbackFiles)) {
      final Iterator<File> logFIt = logbackFiles.iterator();
      File lf = logFIt.next();
      if(logbackFiles.size() > 1) {
        LOGGER.warn("more than 1 '{}' file found", logbackFiles.size());
        LOGGER.warn(lf.getAbsolutePath());
        while(logFIt.hasNext()) {
          LOGGER.warn(logFIt.next().getAbsolutePath());
        }
      }

      Properties props = new Properties();
      try(FileInputStream fis = new FileInputStream(lf)) {
        props.load(fis);
      }
      catch(FileNotFoundException e) {
        LOGGER.error("error while opening '" + lf.getAbsolutePath() + "' : " + e.getLocalizedMessage(), e);
      }
      dlg = openOrAlert(props);
    }
    if(dlg != null) {
      // no file, take the one inside...
      Properties props = new Properties();
      try(InputStream fis = AbstractFxController.class.getResourceAsStream("/logback.properties")) {
        props.load(fis);
      }
      catch(FileNotFoundException e) {
        LOGGER.error("error while opening 'logback.properties' : " + e.getLocalizedMessage(), e);
      }

      dlg = openOrAlert(props);
    }
    if(dlg != null) {
      dlg.showAndWait();
    }
  }

  private Alert openOrAlert(Properties props) {
    Alert dlg = null;
    String logPath = props != null ? props.getProperty(LOGBACK_PATH_KEY) : null;
    if(StringUtils.isNotBlank(logPath)) {
      final File dir = new File(logPath);
      if(!dir.exists()) {
        dlg = createDialog(resources, Alert.AlertType.ERROR, "logback.directory.notfound.title", "logback.directory.notfound.header", null, null,
            new Object[] { logPath }, null, null);
      }
      else {
        Collection<File> logsFiles = FileUtils.listFiles(dir, FileFilterUtils.nameFileFilter(LOG_FILE_NAME, IOCase.SENSITIVE), TrueFileFilter.INSTANCE);
        LOGGER.debug("{} log files found", logsFiles.size());

        // only keep the most recent one
        Optional<File> mostRecentLogFile = logsFiles.stream().max(Comparator.comparingLong(File::lastModified));
        mostRecentLogFile.ifPresent(JavaFXUtils::openFile);
      }
    }
    else {
      dlg = createDialog(resources, Alert.AlertType.ERROR, "logback.key.notfound.title", "logback.key.notfound.header", null, null,
          new Object[] { LOGBACK_PROPERTY_FILE, LOGBACK_PATH_KEY }, null, null);
    }
    return dlg;
  }

  /**
   * show about box
   *
   * @param ae {@link ActionEvent}
   */
  public void showAboutBox(ActionEvent ae) {

    Alert dlg = createDialog(resources, Alert.AlertType.INFORMATION, "about.title", null, "about.content");

    // application version and build number
    String mavenAppVersion = null;
    String mavenAppBuildNumber = null;

    try(InputStream fis = AbstractFxController.class.getResourceAsStream(APPLICATION_PROPS)) {

      Properties appProps = new Properties();
      appProps.load(fis);

      mavenAppVersion = cleanVersion(appProps.getProperty(VERSION_KEY));
      mavenAppBuildNumber = appProps.getProperty(BUILDNUMBER_KEY);
    }
    catch(IOException e) {
      LOGGER.error("error while reading application.properties file: " + e.getLocalizedMessage(), e);
    }

    mavenAppVersion = checkValue(mavenAppVersion, "ExchangeApp version");
    mavenAppBuildNumber = checkValue(mavenAppBuildNumber, "ExchangeApp build number");

    dlg.getDialogPane().setHeaderText(getSafeResourceValue("about.header", mavenAppVersion, mavenAppBuildNumber));
    dlg.showAndWait();
  }

  protected void enableCtrl(Node ctrl) {
    enableDisableCtrl(ctrl, true);
  }

  protected void disableCtrl(Node ctrl) {
    enableDisableCtrl(ctrl, false);
  }

  protected void enableDisableCtrl(Node ctrl, boolean enable) {
    ctrl.setDisable(!enable);
  }

  protected void enableDisableMenuBar(MenuBar menu, boolean enable) {
    enableDisableCtrl(menu, enable);
    menu.getMenus().forEach(m -> enableDisableMenu(m, enable));
  }

  protected void enableDisableMenu(Menu menu, boolean enable) {
    menu.setDisable(!enable);
    menu.getItems().forEach(m -> m.setDisable(!enable));
  }

  private String checkValue(String valueToChecked, String msg) {

    if(StringUtils.isBlank(valueToChecked)) {
      valueToChecked = "???";
      getLogger().warn("{} NOT found, setting '{}'", msg, valueToChecked);
    }
    return valueToChecked;
  }

  private static String cleanVersion(String input) {
    if(StringUtils.isNotBlank(input)) {
      for(String suffix : MAVEN_SNAPSHOT_TOKENS) {
        input = StringUtils.removeEndIgnoreCase(input, suffix);
      }
    }
    return input;
  }

  @Override
  public void enableUI() {
    enableOrDisableUI(true);
  }

  @Override
  public void disableUI() {
    enableOrDisableUI(false);
  }

  @Override
  public void showHideMask(boolean show) {
  }

  @Override
  public void enableOrDisableUI(boolean enable) {
  }
}
