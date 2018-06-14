package fr.perso.nfelix.app.ui.controllers;

import static fr.perso.nfelix.app.ui.typedef.Constants.*;
import static fr.perso.nfelix.app.ui.utils.JavaFXUtils.createDialog;
import static fr.perso.nfelix.app.ui.utils.JavaFXUtils.createPasswordDialog;

import fr.perso.nfelix.app.DispatcherConfig;
import fr.perso.nfelix.app.ImgDispatcherUIAppPreloader;
import fr.perso.nfelix.app.ui.controllers.fragments.JobWorkProgressFragment;
import fr.perso.nfelix.app.ui.services.CancellableService;
import fr.perso.nfelix.app.ui.services.FindAndDispatchImgService;
import fr.perso.nfelix.app.ui.services.IPreValidateService;
import fr.perso.nfelix.app.ui.services.TaskProgressData;
import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import fr.perso.nfelix.app.ui.utils.PasswordInputDialog;
import java.io.File;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.ActionMap;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class HomeController extends AbstractFxController {

  @FXML
  private MenuItem   exitMenuItem;
  @FXML
  private MenuItem   preferencesMenuItem;
  @FXML
  private MenuItem   aboutMenuItem;
  @FXML
  private MenuItem   userGuideMenuItem;
  @FXML
  private MenuItem   logsMenuItem;
  @FXML
  private MenuItem   modeExpertMenuItem;
  @FXML
  private MenuBar    homeMenuBar;
  @FXML
  private MaskerPane maskPane;
  @FXML
  private StackPane  insideMainPane;

  //thos will contain job-in-progress panel
  @FXML
  private Parent                  jobProgressFragment;
  @FXML
  private JobWorkProgressFragment jobProgressFragmentController;

  @FXML
  private Button runButton;

  @Override
  public void postInit() {
    super.postInit();
    ActionMap.register(this);

    jobProgressFragmentController.setParentController(this);

    // do not put image into css, as with shortcut declaration, icon will be duplicated
    exitMenuItem.setGraphic(new ImageView(new Image(EXIT_MENU_ICON)));
    preferencesMenuItem.setGraphic(new ImageView(new Image(TOOLS_MENU_ICON)));
    aboutMenuItem.setGraphic(new ImageView(new Image(HELP_MENU_ICON)));
    modeExpertMenuItem.setGraphic(new ImageView(new Image(EXPERT_MENU_ICON)));
    userGuideMenuItem.setGraphic(new ImageView(new Image(USERGUIDE_MENU_ICON)));
    logsMenuItem.setGraphic(new ImageView(new Image(LOGS_MENU_ICON)));
  }

  @Override
  public void initUI() {
    super.initUI();

    try {
      LOGGER.debug(">>> initUI");

      final DispatcherConfig config = getMainApp().getConfig();
      modeExpertMenuItem.setDisable(DispatcherConfig.isExpertMode());

      // work in progress part...
      jobProgressFragmentController.initUI();
    }
    catch(Throwable th) {
      LOGGER.error(th.getLocalizedMessage(), th);
    }
    finally {
      getMainApp().notifyPreloader(new ImgDispatcherUIAppPreloader.HideNotification());
      LOGGER.debug("<<< initUI");
    }
  }


  public void quitApp(ActionEvent actionEvent) {
    quitApp();
  }

  public void goToSettings(ActionEvent ae) {
    switchToView(FXML_SETTINGS);
  }

  /**
   * here, job will be run/launched !
   *
   * @param ae {@link ActionEvent}
   */
  public void runJobs(ActionEvent ae) {

    LOGGER.info(">>> runJobs");
    FindAndDispatchImgService service = null;
    try {
      jobProgressFragmentController.clear();
      disableUI();

      service = buildDispatchService();
      if(service != null) {
        jobProgressFragmentController.start();
        service.start();
      }
    }
    catch(Exception e) {
      LOGGER.error(e.getLocalizedMessage(), e);
      service = null;
    }
    finally {
      if(service == null) {
        enableUI();
      }
    }
  }

  private void displayNotification(boolean failed, String msgSuffix) {
    enableDisableUI(true, true);
    jobProgressFragmentController.onUpdateJobName("...");
    jobProgressFragmentController.setVisible(false);

    Notifications notificationBuilder = Notifications.create().title(getSafeResourceValue("run.job.notification.title." + msgSuffix))
        .text(getSafeResourceValue("run.job.notification.content." + msgSuffix)).hideAfter(Duration.seconds(5.0)).position(Pos.BOTTOM_RIGHT);
    if(failed) {
      notificationBuilder.showError();
    }
    else {
      notificationBuilder.showInformation();
    }
  }

  public void showHideMask(boolean show) {
    maskPane.setVisible(show);
  }

  /**
   * activation du mode expert
   *
   * @param ae {@link ActionEvent}
   */
  public void activateExpertMode(ActionEvent ae) {

    PasswordInputDialog dlg = createPasswordDialog(resources, "mode.expert.title", "mode.expert.header", "mode.expert.content");
    dlg.showAndWait().ifPresent(password -> {
      if(StringUtils.isNotBlank(password)) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if(passwordEncoder.matches(password, ENCODED_PWD)) {
          DispatcherConfig.setExpertMode(true);
          resetView(FXML_SETTINGS);

          JavaFXUtils
              .displayNotification(Alert.AlertType.INFORMATION, getSafeResourceValue("mode.expert.title"), getSafeResourceValue("mode.expert" + ".activated"));
          modeExpertMenuItem.setDisable(true);
        }
        else {
          JavaFXUtils
              .displayNotification(Alert.AlertType.WARNING, getSafeResourceValue("mode.expert.title"), getSafeResourceValue("mode.expert.not.activated"));
        }
      }
    });
  }

  private void launchService(CancellableService<String> service, final String maskKey, final String dialogPrefixKey) {

    LOGGER.debug(">>> launchService({})", service);

    try {
      maskPane.setText(getSafeResourceValue(maskKey));

      if(service instanceof IPreValidateService) {
        ((IPreValidateService) service).preValidate();
      }

      // cancelled
      service.setOnCancelled(event -> {
        maskPane.setVisible(false);
        enableDisableUI(true, false);
      });

      // failed
      service.setOnFailed(event -> {
        String errorMsg = service.getException() != null ? service.getException().getLocalizedMessage() : "";
        final Alert dialog = createDialog(resources, Alert.AlertType.ERROR, dialogPrefixKey + ".title", dialogPrefixKey + ".header",
            dialogPrefixKey + ".content", null, null, new Object[] { errorMsg }, service.getException());
        dialog.showAndWait();
        maskPane.setVisible(false);
        enableDisableUI(true, false);
      });

      // succeeded
      service.setOnSucceeded(event -> {
        enableDisableUI(true, false);
        JavaFXUtils.openFile(new File(service.getValue()));
      });

      enableDisableUI(false, false);
      service.start();
    }
    catch(Exception e) {
      Alert dialog = createDialog(resources, Alert.AlertType.ERROR, dialogPrefixKey + ".title", dialogPrefixKey + ".header", dialogPrefixKey + ".content", null,
          null, new Object[] { e.getLocalizedMessage() }, e);
      dialog.showAndWait();
    }
    finally {
      LOGGER.debug("<<< launchService");
    }
  }

  /**
   * switch storage to Bdd
   *
   * @param ae {@link ActionEvent}
   */
  public void switchToBddStorage(ActionEvent ae) {

    LOGGER.debug(">>> switchToBddStorage");

    Alert dlg = createDialog(resources, Alert.AlertType.CONFIRMATION, "confirm.storage.confirm.title", "confirm.storage.confirm.bdd.header",
        "confirm.storage.confirm.bdd.content");
    dlg.showAndWait().ifPresent(buttonType -> {
      // if(ButtonType.OK.equals(buttonType)) {
      // in case of anything tido
      // ignored
      // }
    });

    LOGGER.debug("<<< switchToBddStorage");
  }

  @Override
  public void enableOrDisableUI(boolean enable) {
    enableDisableMenuBar(homeMenuBar, enable);
  }

  public void enableDisableUI(boolean enable, boolean mainStackToo) {
    enableOrDisableUI(enable);
    if(mainStackToo) {
      insideMainPane.setVisible(enable);
      insideMainPane.setManaged(enable);
    }
  }

  private FindAndDispatchImgService buildDispatchService() {
    LOGGER.debug(">>> buildDispatchService");

    jobProgressFragmentController.clear();
    FindAndDispatchImgService findAndDispatchImgService = new FindAndDispatchImgService(jobProgressFragmentController);

    try {
      findAndDispatchImgService.setResources(getResources());
      final TaskProgressData taskData = jobProgressFragmentController.getTaskData();
      findAndDispatchImgService.setTaskData(taskData);

      findAndDispatchImgService.preValidate();

      // fail event
      findAndDispatchImgService.setOnFailed(event -> {
        if(findAndDispatchImgService.getException() != null) {
          jobProgressFragmentController.onUpdateText(ExceptionUtils.getStackTrace(findAndDispatchImgService.getException()));
        }
        displayNotification(true, "ko");
      });
      // cancel event
      findAndDispatchImgService.setOnCancelled(event -> displayNotification(false, "cancel"));
      // success event
      findAndDispatchImgService.setOnSucceeded(event -> displayNotification(false,  taskData.isCancelled() ? "cancel" : "ok"));
      return findAndDispatchImgService;
    }
    catch(Exception e) {
      Alert dialog = createDialog(resources, Alert.AlertType.ERROR, "extraction.generation.service.error.title", "extraction.generation.service.error.header",
          "extraction.generation.service.error.content", null, null, new Object[] { e.getLocalizedMessage() }, e);
      dialog.showAndWait();
    }
    finally {
      LOGGER.debug("<<< buildDispatchService");
    }
    return null;
  }

}