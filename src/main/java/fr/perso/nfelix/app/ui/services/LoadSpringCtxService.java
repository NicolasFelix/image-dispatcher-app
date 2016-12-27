package fr.perso.nfelix.app.ui.services;

import fr.perso.nfelix.app.ui.controllers.IFXMaskableArea;
import fr.perso.nfelix.app.ui.utils.SpringContextHolder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import lombok.extern.slf4j.Slf4j;

/**
 * javafx service used to (re)load spring batch context
 *
 * @author N.FELIX
 */
@Slf4j
public class LoadSpringCtxService extends Service<Boolean> {

  private IFXMaskableArea parentController;

  private Set<String> profiles;
  private Set<String> beansToCheck;

  private boolean forceContextReload = false;

  /**
   * constructor
   *
   * @param parentController parent controller
   * @param profiles         profiles to be loaded
   * @param beansToCheck     beans to check if they already exists...
   */
  public LoadSpringCtxService(IFXMaskableArea parentController, List<String> profiles, List<String> beansToCheck) {
    super();
    this.parentController = parentController;
    this.profiles = new HashSet<>(profiles);
    this.beansToCheck = new HashSet<>(beansToCheck);
  }

  /**
   * constructor
   *
   * @param parentController   parent controller
   * @param profiles           profiles to be loaded
   * @param beansToCheck       beans to check if they already exists...
   * @param forceContextReload should we reload the context ?
   */
  public LoadSpringCtxService(IFXMaskableArea parentController, List<String> profiles, List<String> beansToCheck, final boolean forceContextReload) {
    this(parentController, profiles, beansToCheck);
    this.forceContextReload = forceContextReload;
  }

  /**
   * constructor
   *
   * @param parentController parent controller
   * @param profile          single profile to be loaded
   * @param beanToCheck      single bean to check if they already exists...
   */
  public LoadSpringCtxService(IFXMaskableArea parentController, String profile, String beanToCheck) {
    this(parentController, Collections.singletonList(profile), Collections.singletonList(beanToCheck));
  }

  @Override
  protected Task<Boolean> createTask() {
    return new Task<Boolean>() {
      @Override
      protected Boolean call()
          throws Exception {
        boolean loaded;
        try {
          LOGGER.debug(">>> call");

          parentController.showHideMask(true);

          if(profiles == null) {
            profiles = new HashSet(1);
          }
          if(beansToCheck == null) {
            beansToCheck = Collections.emptySet();
          }

          if(forceContextReload) {
            loaded = SpringContextHolder.getInstance().refreshSpringContext(true, profiles.toArray(new String[profiles.size()]));
          }
          else {
            loaded = SpringContextHolder.getInstance()
                .refreshSpringContextIfBeanMissing(beansToCheck.toArray(new String[beansToCheck.size()]), profiles.toArray(new String[profiles.size()]));
          }
        }
        finally {
          parentController.showHideMask(false);
          LOGGER.debug("<<< call");
        }
        return loaded;
      }
    };
  }
}
