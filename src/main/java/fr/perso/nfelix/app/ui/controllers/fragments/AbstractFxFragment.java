package fr.perso.nfelix.app.ui.controllers.fragments;

import fr.perso.nfelix.app.ui.controllers.IFXWorkableArea;
import fr.perso.nfelix.app.ui.controllers.IFxController;
import fr.perso.nfelix.app.ui.controllers.IUpdatableUI;
import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import java.net.URL;
import java.util.ResourceBundle;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;

/**
 * AbstractFxFragment, common asbstraction
 *
 * @author N.FELIX
 */
@Slf4j
public abstract class AbstractFxFragment implements IFxFragment, IFXWorkableArea, IUpdatableUI {

  @Getter(AccessLevel.PROTECTED)
  @Setter
  protected ResourceBundle resources;

  @Getter(AccessLevel.PROTECTED)
  @Setter
  protected IFxController parentController;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private boolean running = false;

  @Getter
  @Setter(AccessLevel.PROTECTED)
  private boolean expertModeOnly = false;

  /**
   * to be call when view has been created, but not yet shown (do not used it for initialize UI as main app is not injected yet)
   */
  public void postInit() {
  }

  public void dispose() {
  }

  public void initUI() {
    // nada
  }

  public void clear() {
  }

  @Override
  public void onUpdateJobName(String jobName) {
  }

  @Override
  public void onUpdateProgressMessage(String progress) {
  }

  @Override
  public void onUpdateProgress(double progress) {
  }

  @Override
  public void onUpdateText(final String msg) {
  }

  @Override
  public void enableUI() {
    enableOrDisableUI(true);
  }

  @Override
  public void disableUI() {
    enableOrDisableUI(false);
  }

  protected Logger getLogger() {
    return LOGGER;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    this.resources = resources;

    postInit();
  }

  protected String getSafeResourceValue(final String key) {
    return getSafeResourceValue(key, (Object) null);
  }

  protected String getSafeResourceValue(final String key, Object... arguments) {
    if(resources == null) {
      return ">>> resourceBundle is undefined <<<";
    }

    return JavaFXUtils.getSafeResourceValue(resources, key, arguments);
  }

}
