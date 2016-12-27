package fr.perso.nfelix.app.ui.services;

import lombok.Getter;
import lombok.Setter;

/**
 * TaskProgress structure
 *
 * @author N.FELIX
 */
@Getter
@Setter
public class TaskProgressData {

  private String jobName;
  private boolean cancelled = false;

  /**
   * reset data
   */
  public void reset() {
    jobName = null;
    cancelled = false;
  }
}
