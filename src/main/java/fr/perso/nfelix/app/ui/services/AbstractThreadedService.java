package fr.perso.nfelix.app.ui.services;

import fr.perso.nfelix.app.ui.controllers.IUpdatableUI;
import lombok.Setter;

/**
 * AbstractThreadedService
 *
 * @param <V> result type
 * @author N.FELIX
 */
public abstract class AbstractThreadedService<V> extends CancellableService<V> {

  @Setter
  protected int dumpStep          = 50;

  public AbstractThreadedService(IUpdatableUI updatableUI) {
    super(updatableUI);
  }

  public AbstractThreadedService(TaskProgressData taskData, IUpdatableUI updatableUI) {
    super(taskData, updatableUI);
  }
}
