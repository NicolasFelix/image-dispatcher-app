package fr.perso.nfelix.app.ui.services;

import lombok.Setter;

/**
 * AbstractThreadedService
 *
 * @author N.FELIX
 */
public abstract class AbstractThreadedService<V> extends CancellableService<V> {

  @Setter
  protected int threadCoefficient = 10;
  @Setter
  protected int dumpStep          = 100;

  public AbstractThreadedService() {
    super();
  }

  public AbstractThreadedService(TaskProgressData taskData) {
    super(taskData);
  }
}
