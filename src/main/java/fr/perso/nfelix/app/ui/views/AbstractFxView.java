package fr.perso.nfelix.app.ui.views;

import fr.perso.nfelix.app.ui.controllers.AbstractFxController;
import fr.perso.nfelix.app.ui.controllers.IFxController;
import lombok.extern.slf4j.Slf4j;

/**
 * abstract fx view
 *
 * @author N.FELIX
 */
@Slf4j
public abstract class AbstractFxView implements IFxView {

  protected IFxController parentController = null;

  /**
   * Constructor
   */
  public AbstractFxView() {
    super();
  }

  @Override
  public void start() {
    ((AbstractFxController) getController()).postInit();
  }

  @Override
  public void stop() {
    ((AbstractFxController) getController()).dispose();
  }

  protected IFxController getController() {
    return parentController;
  }
}
