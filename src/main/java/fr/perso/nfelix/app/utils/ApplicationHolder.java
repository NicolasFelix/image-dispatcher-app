package fr.perso.nfelix.app.utils;

import fr.perso.nfelix.app.DispatcherConfig;
import fr.perso.nfelix.app.ImgDispatcherUIApp;
import lombok.Getter;
import lombok.Setter;

/**
 * Application holder singleton
 *
 * @author N.FELIX
 */
public class ApplicationHolder {

  @Getter
  private final static ApplicationHolder INSTANCE = new ApplicationHolder();

  @Getter
  @Setter
  private ImgDispatcherUIApp mainApp;

  /**
   * get configuration
   *
   * @return {@link DispatcherConfig}
   */
  public DispatcherConfig getConfig() {
    return mainApp.getConfig();
  }
}
