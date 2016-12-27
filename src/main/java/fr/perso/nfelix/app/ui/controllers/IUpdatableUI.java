package fr.perso.nfelix.app.ui.controllers;

/**
 * updatable UI interface
 *
 * @author N.FELIX
 */
public interface IUpdatableUI {

  /**
   * is service running
   *
   * @return true if service is running
   */
  boolean isRunning();

  /**
   * update msg event
   *
   * @param msg message
   */
  void onUpdateText(final String msg);

  /**
   * update job  event
   *
   * @param jobName message
   */
  void onUpdateJobName(final String jobName);

  /**
   * update progress (percent)
   *
   * @param progress new (percent) value
   */
  void onUpdateProgress(final double progress);

  /**
   * update progress msg
   *
   * @param progressMsg progress msg
   */
  void onUpdateProgressMessage(final String progressMsg);
}

