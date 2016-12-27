package fr.perso.nfelix.app.ui.controllers.fragments;

import fr.perso.nfelix.app.ui.controllers.IFxController;
import javafx.fxml.Initializable;

/**
 * javafx fragment interface
 *
 * @author N.FELIX
 */
public interface IFxFragment extends Initializable {

  /**
   * clear data...
   */
  void clear();

  /**
   * This method will allow the injection of the Parent Controller
   *
   * @param parentController parentController controller
   */
  void setParentController(IFxController parentController);
}
