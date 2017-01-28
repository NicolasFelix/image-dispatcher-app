package fr.perso.nfelix.app.ui.services;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.extern.slf4j.Slf4j;

/**
 * JobRejectedExecution Handler
 *
 * @author N.FELIX
 */
@Slf4j
class JobRejectedExecutionHandler implements RejectedExecutionHandler {

  @Override
  public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
    try {
      if(executor != null && executor.getQueue() != null) {
        LOGGER.debug("task '{}' has been rejected. Putting it to queue.", r);
        executor.getQueue().put(r);
      }
    }
    catch(InterruptedException ie) {
      LOGGER.error("Work discarded, thread was interrupted while waiting for space to schedule: '{}'", r);
    }
  }
}
