package fr.perso.nfelix.app.ui.services;

import fr.perso.nfelix.app.ui.controllers.IUpdatableUI;
import fr.perso.nfelix.app.ui.utils.IJobCallback;
import lombok.Getter;
import lombok.Setter;

/**
 * JobCallback class
 *
 * @author N.FELIX
 */
// @Slf4j
public class JobCallback implements IJobCallback {

  @Setter
  private IUpdatableUI uiRef;

  @Setter
  @Getter
  private boolean operationCancelled;

  @Override
  public void onUpdateText(final String message) {
    // LOGGER.error("Yattaa : {}", message);
    if(uiRef != null) {
      /*Platform.runLater(() -> */
      uiRef.onUpdateText(message);
    }
  }

  @Override
  public void onUpdateJob(String jobName) {
    if(uiRef != null) {
      /*Platform.runLater(() -> */
      uiRef.onUpdateJobName(jobName);
    }
  }

  @Override
  public void onUpdateProgress(double progress) {
    if(uiRef != null) {
      /*Platform.runLater(() ->*/
      uiRef.onUpdateProgress(progress);
    }
  }

  @Override
  public void reset() {
    uiRef = null;
    operationCancelled = false;
  }
}
