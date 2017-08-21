package fr.perso.nfelix.app.ui.services.utils;

/**
 * ProgressCallBack interface
 */
public interface ProgressCallBack {

  /** callback method
   *
   * @param rbc {@link CallbackByteChannel}
   * @param progress progress
   */
   void callback(CallbackByteChannel rbc, double progress);
}  