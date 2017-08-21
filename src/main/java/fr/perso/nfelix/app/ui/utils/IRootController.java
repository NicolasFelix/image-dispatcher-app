package fr.perso.nfelix.app.ui.utils;

import fr.perso.nfelix.app.ImgDispatcherUIApp;

/**
 * @author N.FELIX
 */
public interface IRootController {

  /**
   * get main app
   *
   * @return {@link ImgDispatcherUIApp}
   */
  ImgDispatcherUIApp getMainApp();

  /**
   * switch to view from code/key
   *
   * @param fxmlKey fxml key
   * @throws Exception in case of...
   */
  void switchToView(final String fxmlKey)
      throws Exception;

  /**
   * reset to view from code/key
   *
   * @param fxmlKey fxml key
   * @return true if removed
   */
  boolean resetView(final String fxmlKey);

}
