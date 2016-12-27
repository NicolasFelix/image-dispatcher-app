package fr.perso.nfelix.app.utils.spring;

import javax.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Defines the Spring application context holder. This allows to access to the Spring context from a no Spring component.
 *
 * @author J.DeBouillanne
 */
public class SpringApplicationContextHolder implements ApplicationContextAware {
  private static ApplicationContext context = null;

  private static final Logger LOGGER = LoggerFactory.getLogger(SpringApplicationContextHolder.class);

  public void setApplicationContext(ApplicationContext ctx)
      throws BeansException {
    if(context == null) {
      context = ctx;
    }
    else if(ctx != null && context != ctx) {
      LOGGER.warn("new Spring context has not been set to global holder !");
    }
  }

  /**
   * Gets the Spring context.
   *
   * @return the Spring context
   */
  public static ApplicationContext getSpringContext() {
    return context;
  }

  /**
   * preparing destroy method... Not (yet) useful
   */
  @PreDestroy
  public static void destroy() {
    // nada
  }

  /**
   * destroy / close context
   */
  public static void close() {
    if(context != null) {
      if(context instanceof ConfigurableApplicationContext) {
        clearCache();

        try {
          ((ConfigurableApplicationContext) context).close();
        }
        catch(Exception e) {
          LOGGER.error("error while closing spring context: " + e.getLocalizedMessage(), e);
        }
      }
      context = null;
    }
  }

  /**
   * refresh application context
   *
   * @return true if refresh was successfully done
   * @throws Exception in case of...
   */
  public static boolean refresh()
      throws Exception {
    boolean ret = false;
    if(context != null && context instanceof ConfigurableApplicationContext) {
      clearCache();

      try {
        ((ConfigurableApplicationContext) context).refresh();
        ret = true;
      }
      catch(BeansException | IllegalStateException e) {
        LOGGER.error("error while refreshing spring context: " + e.getLocalizedMessage(), e);
        throw e;
      }
    }
    else {
      LOGGER.error("context is null or not an instsance of ConfigurableApplicationContext !");
    }
    return ret;
  }

  private static void clearCache() {
    // do not forget to release cache !

  }

}
