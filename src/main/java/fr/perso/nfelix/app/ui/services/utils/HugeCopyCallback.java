package fr.perso.nfelix.app.ui.services.utils;

import fr.perso.nfelix.app.ui.controllers.IUpdatableUI;
import fr.perso.nfelix.app.ui.services.CancellableService;
import java.nio.file.Path;
import java.text.DecimalFormat;

/**
 * HugeCopyCallback, see {@link ProgressCallBack}
 * @author N.FELIX
 */
public class HugeCopyCallback implements ProgressCallBack {

  double latestProgress = -1d;
  private final int           STEP = 10;
  private final DecimalFormat df   = new DecimalFormat("#");

  // private final Thread             worker;
  private final IUpdatableUI       updatableUI;
  private final CancellableService caller;
  private final Path               source;

  /**
   * constructor
   *
   * @param service service caller
   * @param source source file
   */
  public HugeCopyCallback(Path source, CancellableService service) {
    super();
    // this.worker = worker;
    this.caller = service;
    this.updatableUI = service.getUpdatableUI();
    this.source = source;
  }

  @Override
  public void callback(CallbackByteChannel rbc, double progress) {
    if(latestProgress == -1) {
      latestProgress = progress;
    }
    if((progress - latestProgress) > STEP) {
      latestProgress = progress;
      if(updatableUI != null) {
        updatableUI.onUpdateText("File '" + source.getFileName().toString() + "', backup in progress  (" + df.format(latestProgress) + " %)");
      }
    }

    if(caller.isOperationCancelled()) {
      //noinspection ResultOfMethodCallIgnored
      Thread.interrupted();
    }
  }
}
