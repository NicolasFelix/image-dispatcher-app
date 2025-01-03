package fr.perso.nfelix.app;

import static fr.perso.nfelix.app.ui.typedef.Constants.*;

import com.sun.javafx.application.LauncherImpl;
import fr.perso.nfelix.app.ui.config.GlobalConfig;
import fr.perso.nfelix.app.ui.typedef.JobConstants;
import fr.perso.nfelix.app.ui.utils.FXViewManager;
import fr.perso.nfelix.app.ui.utils.IRootController;
import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import fr.perso.nfelix.app.ui.utils.SpringContextHolder;
import fr.perso.nfelix.app.utils.ApplicationHolder;
import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javafx.application.Application;
import javafx.event.Event;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Control;
import javafx.scene.image.Image;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.*;

/**
 * main application clazz.
 */
@Slf4j
public class ImgDispatcherApp extends Application {

  private static final int          PORT   = 43666;
  @SuppressWarnings("FieldCanBeLocal")
  private              ServerSocket socket = null;

  private static boolean restoreWindowsPosition = true;

  @Getter
  private URL mainCss = null;

  @Getter
  @Setter
  private Stage appStage = null;

  @Getter
  private DispatcherConfig config;

  @Getter
  private ResourceBundle mainResources;

  @Getter
  private IRootController mainController;

  /**
   * the fucking main.
   *
   * @param args args
   */
  public static void main(String... args) {
    LOGGER.info("used classpath: '{}'", System.getProperty("java.class.path"));

    try {
      LOGGER.info(">>> launching application");
      final String[] HIDE_SPLASH_OPT = { "h", "hideSplash", "hide splash screen" };
      final String[] RESET_WINDOWS_POS_OPT = { "w", "resetWindow", "reset window position" };
      final String[] EXPERT_MODE_OPT = { "e123456$_$", "expertMode123456$_$", "exportMode ON" };

      Options options = new Options();
      Option hideOpt = new Option(HIDE_SPLASH_OPT[0], HIDE_SPLASH_OPT[1], false, HIDE_SPLASH_OPT[2]);
      Option expertOpt = new Option(EXPERT_MODE_OPT[0], EXPERT_MODE_OPT[1], false, EXPERT_MODE_OPT[2]);
      Option resetWindowsPositionOpt = new Option(RESET_WINDOWS_POS_OPT[0], RESET_WINDOWS_POS_OPT[1], false, RESET_WINDOWS_POS_OPT[2]);
      options.addOption(expertOpt);
      options.addOption(resetWindowsPositionOpt);
      options.addOption(hideOpt);
      CommandLine cmd = null;
      try {
        cmd = (new DefaultParser()).parse(options, args, false);
      }
      catch(ParseException e) {
        LOGGER.error("error while parsing command line: " + e.getLocalizedMessage(), e);
      }

      if(cmd != null) {
        if(cmd.hasOption(EXPERT_MODE_OPT[0]) || cmd.hasOption(EXPERT_MODE_OPT[1])) {
          DispatcherConfig.setExpertMode(true);
        }
        if(cmd.hasOption(RESET_WINDOWS_POS_OPT[0]) || cmd.hasOption(RESET_WINDOWS_POS_OPT[1])) {
          restoreWindowsPosition = false;
        }

        if(cmd.hasOption(HIDE_SPLASH_OPT[0]) || cmd.hasOption(HIDE_SPLASH_OPT[1])) {
          launch(args);
          return;
        }
      }
      // launch application with splashScreen
      LauncherImpl.launchApplication(ImgDispatcherApp.class, ImgDispatcherAppPreloader.class, args);
    }
    catch(Throwable th) {
      LOGGER.error(th.getLocalizedMessage(), th);
    }
    finally {
      LOGGER.info("<<< launch done");
    }
  }

  @Override
  public void start(Stage appStage)
      throws Exception {

    mainResources = JavaFXUtils.loadResourceBundle(FXML_MAIN);

    try {
      setAppStage(appStage);
      SpringContextHolder.buildSpringContext(this);

      ApplicationHolder.getINSTANCE().setMainApp(this);
      initMainUI(appStage);
      initConfig();
      SpringContextHolder.getInstance().refreshSpringContext(JobConstants.FAKE_PROFILE);

      restoreWindowsPosition();

      appStage.show();
    }
    catch(Throwable th) {
      LOGGER.error(th.getLocalizedMessage(), th);

      final Alert dialog = JavaFXUtils.createDialog(mainResources, Alert.AlertType.ERROR, "application.startup.error.title", "application.startup.error.header",
          "application.startup.error.content", null, null, null, th);
      dialog.showAndWait();
      throw th;
    }
  }

  private void restoreWindowsPosition() {
    if(!restoreWindowsPosition) {
      return;
    }
    final Preferences pref = Preferences.userRoot().node(APP_NAME);

    appStage.setMinHeight(WINDOWS_MIN_HEIGHT);
    appStage.setMinWidth(WINDOWS_MIN_WIDTH);

    appStage.setX(pref.getDouble(WINDOWS_POS_X, 10));
    appStage.setY(pref.getDouble(WINDOWS_POS_Y, 10));
    appStage.setWidth(pref.getDouble(WINDOWS_POS_WIDTH, WINDOWS_MIN_WIDTH));
    appStage.setHeight(pref.getDouble(WINDOWS_POS_HEIGHT, WINDOWS_MIN_HEIGHT));
    appStage.setMaximized(pref.getBoolean(WINDOWS_MAXIMIZED, false));
  }

  /**
   * save Windows Position
   */
  public void saveWindowsPosition() {
    final Preferences pref = Preferences.userRoot().node(APP_NAME);

    pref.putDouble(WINDOWS_POS_X, appStage.getX());
    pref.putDouble(WINDOWS_POS_Y, appStage.getY());
    pref.putDouble(WINDOWS_POS_WIDTH, appStage.getWidth());
    pref.putDouble(WINDOWS_POS_HEIGHT, appStage.getHeight());
    pref.putBoolean(WINDOWS_MAXIMIZED, appStage.isMaximized());
  }

  private void initMainUI(Stage stage)
      throws Exception {

    // prevent multiple instances running
    checkIfApplicationIsAlreadyRunning();

    // prevent main application to be closed using X icon
    stage.setOnCloseRequest(Event::consume);

    switchStyle(MAIN_STYLE + GlobalConfig.LIGHT_THEME + CSS_EXTENSION);
    mainController = new FXViewManager(this);
    final Scene mainScene = ((FXViewManager) mainController).loadMainScene(stage);
    mainScene.addEventFilter(KeyEvent.KEY_PRESSED, this::handleShortcut);
    stage.setScene(mainScene);
    mainController.switchToView(FXML_HOME);

    stage.setTitle(APP_NAME);
    stage.getIcons().add(new Image(APP_ICON, 32, 32, true, true));
  }

  private void initConfig() {

    config = new DispatcherConfig(mainResources);
    config.load();
  }

  private void handleShortcut(KeyEvent ke) {
    Node node = appStage.getScene().getFocusOwner();
    if(node instanceof Control) {
      Control ctrl = (Control) node;
      if(ctrl.getContextMenu() != null) {
        ctrl.getContextMenu().getItems().stream().filter(item -> item.getAccelerator() != null && item.getAccelerator().match(ke)).forEach(item -> {
          item.fire();
          ke.consume();
        });
      }
    }
  }

  private void checkIfApplicationIsAlreadyRunning() {
    try {
      // Bind to localhost adapter with a zero connection queue
      socket = new ServerSocket(PORT, 0, InetAddress.getByAddress(new byte[] { 127, 0, 0, 1 }));
    }
    catch(BindException e) {
      LOGGER.warn("Application is already running.");

      final Alert dialog = JavaFXUtils
          .createDialog(mainResources, Alert.AlertType.ERROR, "application.title", "application.already.running.header", "application.already.running.content");
      dialog.showAndWait();
    }
    catch(IOException ioe) {
      LOGGER.error("Unexpected error: '" + ioe.getLocalizedMessage() + "'", ioe);
    }
  }

  /**
   * reset main css
   *
   * @param css css file
   */
  public void switchStyle(final String css) {
    mainCss = ImgDispatcherApp.class.getResource(css);
  }
}