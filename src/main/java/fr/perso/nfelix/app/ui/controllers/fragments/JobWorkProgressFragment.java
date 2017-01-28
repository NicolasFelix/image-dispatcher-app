package fr.perso.nfelix.app.ui.controllers.fragments;

import static fr.perso.nfelix.app.ui.typedef.Constants.LINE_SEP;
import static fr.perso.nfelix.app.ui.typedef.Constants.STOP_ICON;

import fr.perso.nfelix.app.ui.controllers.HomeController;
import fr.perso.nfelix.app.ui.services.JobCallback;
import fr.perso.nfelix.app.ui.services.TaskProgressData;
import fr.perso.nfelix.app.ui.typedef.JobConstants;
import fr.perso.nfelix.app.ui.utils.SpringContextHolder;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.Notifications;

/**
 * JobWorkProgress Fragment  panel
 *
 * @author N.FELIX
 */
@Slf4j
public class JobWorkProgressFragment extends AbstractFxFragment {

  // the following are visible while job is running
  @FXML
  private StackPane   jobInProgressPane;
  @FXML
  private StackPane   jobActionPane;
  @FXML
  private TextArea    progressArea;
  @FXML
  private ProgressBar progressBar;

  @FXML
  private Button cancelButton;

  private TaskProgressData taskData = new TaskProgressData();

  @Override
  public void onUpdateText(String msg) {
    Platform.runLater(() -> {
      progressArea.appendText(msg);
      progressArea.appendText(LINE_SEP);
    });
  }

  @Override
  public void onUpdateProgress(double progress) {
    Platform.runLater(() -> progressBar.setProgress(progress));
  }

  @Override
  public void initUI() {
    super.initUI();

    progressArea.setEditable(false);
  }

  @Override
  public void postInit() {
    super.postInit();

    cancelButton.setGraphic(new ImageView(new Image(STOP_ICON)));
    cancelButton.setOnAction(event -> {
      taskData.setCancelled(true);
      cancelButton.setDisable(true);

      // notifiy callback
      JobCallback jc = (JobCallback) SpringContextHolder.getInstance().getBean(JobConstants.JOB_CALLBACK_BEAN);
      if(jc == null) {
        LOGGER.warn("no job callback defined with bean name '{}'", JobConstants.JOB_CALLBACK_BEAN);
      }
      else {
        jc.setOperationCancelled(true);
      }
      onUpdateText(getSafeResourceValue("import.stop.user.asked"));
    });
  }

  @Override
  public void clear() {
    super.clear();
    taskData.reset();
    progressArea.clear();
  }

  public void start() {
    progressBar.setProgress(-1.0d);
    jobInProgressPane.setVisible(true);
    jobActionPane.setDisable(false);
    cancelButton.setDisable(false);
  }

  public void setDisable(boolean disable) {
    jobInProgressPane.setDisable(disable);
  }

  public void setVisible(boolean visible) {
    jobInProgressPane.setVisible(visible);
  }

  private void displayNotification(boolean failed, String msgSuffix) {
    ((HomeController) parentController).enableDisableUI(true, true);

    jobActionPane.setDisable(true);
    progressBar.setVisible(false);
    onUpdateJobName("...");

    Notifications notificationBuilder = Notifications.create().title(getSafeResourceValue("run.job.notification.title." + msgSuffix))
        .text(getSafeResourceValue("run.job.notification.content." + msgSuffix)).hideAfter(Duration.seconds(5.0)).position(Pos.BOTTOM_RIGHT);
    if(failed) {
      notificationBuilder.showError();
    }
    else {
      notificationBuilder.showInformation();
    }
  }

  @Override
  public void enableOrDisableUI(boolean enable) {
  }
}
