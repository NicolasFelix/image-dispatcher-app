package fr.perso.nfelix.app.ui.controllers;

import fr.perso.nfelix.app.DispatcherConfig;
import fr.perso.nfelix.app.ImgDispatcherApp;
import fr.perso.nfelix.app.ui.utils.IRootController;
import javafx.fxml.Initializable;

/**
 * IFxController, common interface
 *
 * @author N.FELIX
 */
public interface IFxController extends Initializable {

  /**
   * This method will allow the injection of the Parent ScreenPane
   *
   * @param rootController main/root controller
   */
  void setRootController(IRootController rootController);

  /**
   * initUI
   */
  void initUI();

  /**
   * switch to view from code
   *
   * @param fxmlKey fxml code
   */
  void switchToView(final String fxmlKey);

  /**
   * get main app
   *
   * @return {@link ImgDispatcherApp}
   */
  ImgDispatcherApp getMainApp();

  /**
   * get configuration
   *
   * @return {@link DispatcherConfig}
   */
  DispatcherConfig getConfig();

}
