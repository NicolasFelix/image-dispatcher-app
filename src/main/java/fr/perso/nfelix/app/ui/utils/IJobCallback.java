package fr.perso.nfelix.app.ui.utils;

/**
 * define interface that can be used to exchange information between the running job/service/and UI
 *
 * @author N.FELIX
 */
public interface IJobCallback {

  /**
   * some progress done...
   *
   * @param message message to be displayed/logged
   */
  void onUpdateText(final String message);

  /**
   * some progress done...
   *
   * @param jobName jobName
   */
  void onUpdateJob(final String jobName);

  /**
   * some progress done...
   *
   * @param progress between 0 and 100
   */
  void onUpdateProgress(final double progress);

  /**
   * operation has been cancelled
   *
   * @return true if operation has been cancelled
   */
  boolean isOperationCancelled();

  /**
   * reset data
   */
  void reset();
}
