package fr.perso.nfelix.app.ui.utils;

import static fr.perso.nfelix.app.ui.typedef.Constants.SPRING_CONTEXT;

import fr.perso.nfelix.app.ImgDispatcherApp;
import fr.perso.nfelix.app.ui.typedef.JobConstants;
import fr.perso.nfelix.app.utils.spring.SpringApplicationContextHolder;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanIsAbstractException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Util class to keep a reference/lad/reload the spring context
 *
 * @author N.FELIX
 */
@Slf4j
public class SpringContextHolder {

  private static SpringContextHolder INSTANCE;

  private ImgDispatcherApp mainApp;

  /**
   * hidden default constructor
   */
  private SpringContextHolder() {
    super();
  }

  public static void buildSpringContext(ImgDispatcherApp mainApp) {
    INSTANCE = new SpringContextHolder();
    INSTANCE.mainApp = mainApp;
  }

  public static SpringContextHolder getInstance() {
    if(INSTANCE == null) {
      throw new RuntimeException("SpringContextHolder hasn't been initialized !!");
    }
    return INSTANCE;
  }

  /**
   * <pre>
   * method to refresh spring context
   * profiles are automatically guessed/computed
   * </pre>
   *
   * @param beanNameToCheck bean name to check existency
   * @param profiles        profiles to be added
   * @return true if resfresh succeeded
   */
  public boolean refreshSpringContextIfBeanMissing(final String beanNameToCheck, final String[] profiles) {
    return refreshSpringContextIfBeanMissing(new String[] { beanNameToCheck }, profiles);
  }

  /**
   * <pre>
   * method to refresh spring context
   * profiles are automatically guessed/computed
   * </pre>
   *
   * @param beanNameToCheck bean name to check existency
   * @param profile         profiles to  add
   * @return true if resfresh succeeded
   */
  public boolean refreshSpringContextIfBeanMissing(final String beanNameToCheck, final String profile) {
    return refreshSpringContextIfBeanMissing(new String[] { beanNameToCheck }, new String[] { profile });
  }

  /**
   * <pre>
   * method to refresh spring context
   * profiles are automatically guessed/computed
   * </pre>
   *
   * @param beanNamesToCheck bean names to check existency
   * @param profiles         profiles to be added
   * @return true if resfresh succeeded
   */
  public boolean refreshSpringContextIfBeanMissing(final String[] beanNamesToCheck, final String[] profiles) {
    boolean ret = true;
    if(isAnyBeanMissing(beanNamesToCheck)) {
      ret = refreshSpringContext(false, profiles);
      if(ret) {
        if(isAnyBeanMissing(beanNamesToCheck)) {
          LOGGER.warn("after spring refresh, bean(s) still missing ({})", (Object[]) beanNamesToCheck);
        }
      }
    }
    return ret;
  }

  /**
   * method to refresh spring context
   *
   * @param profiles any profiles added to...
   * @return true if refresh succeeded
   */
  public boolean refreshSpringContext(String... profiles) {
    return refreshSpringContext(false, profiles);
  }

  /**
   * method to refresh spring context
   *
   * @param forceSpringCtxRefresh force spring context refresh ? (i.e. it will be destroyed, then recreated)
   * @param profiles              any profiles added to...
   * @return true if refresh succeeded
   */
  synchronized public boolean refreshSpringContext(boolean forceSpringCtxRefresh, String... profiles) {
    try {
      final String[] filteredProfiles = Stream.of(profiles).filter(StringUtils::isNotBlank).collect(Collectors.toList()).toArray(new String[0]);
      LOGGER.info("loading spring-context ('{}') with profiles: '{}'", SPRING_CONTEXT, StringUtils.join(filteredProfiles, ","));
      final long start = System.currentTimeMillis();

      mainApp.getConfig().writeJobParameters(JobConstants.SETTINGS_PROPERTIES);

      boolean newContext = false;
      ClassPathXmlApplicationContext context = (ClassPathXmlApplicationContext) getSpringContext();

      if(forceSpringCtxRefresh && context != null) {
        SpringApplicationContextHolder.close();
        context = null;
      }

      if(context == null) {
        context = new ClassPathXmlApplicationContext();
        newContext = true;
      }

      context.getEnvironment().setActiveProfiles(filteredProfiles);
      context.setConfigLocation(SPRING_CONTEXT);

      if(!newContext) {
        SpringApplicationContextHolder.refresh();
      }
      else {
        context.refresh();
      }

      // dumpBeans(context);

      LOGGER.info("Spring Context loaded successfully ! ({} ms)", System.currentTimeMillis() - start);
      return true;
    }
    catch(Exception e) {
      LOGGER.error("error while initializing spring context:" + e.getLocalizedMessage(), e);

      Platform.runLater(() -> {
        final Alert dialog = JavaFXUtils
            .createDialog(mainApp.getMainResources(), Alert.AlertType.ERROR, "application.spring.error.title", "application.spring.error.header",
                "application.spring.error.content", null, null, null, e);
        dialog.showAndWait();
      });
    }
    return false;
  }

  private void dumpBeans(ClassPathXmlApplicationContext ctx) {

    Stream.of(ctx.getBeanDefinitionNames()).forEach(s -> {
      final Object bean;
      try {
        bean = ctx.getBean(s);
        if(bean != null) {
          LOGGER.info("{}: {}", s, bean.getClass().getSimpleName());
        }
        else {
          LOGGER.warn("no bean class for name '{}'", s);
        }
      }
      catch(BeanIsAbstractException ignored) {
        //
      }
      catch(BeansException be) {
        LOGGER.error(be.getLocalizedMessage(), be);
      }
    });
  }

  /**
   * get spring bean
   *
   * @param beanName bean name
   * @return bean if any (or null)
   */
  public Object getBean(String beanName) {
    return getBean(beanName, true);
  }

  /**
   * get spring bean
   *
   * @param beanName bean name
   * @param logError error should be logged ?
   * @return bean if any (or null)
   */
  public Object getBean(String beanName, boolean logError) {
    if(isContextInitialized()) {
      try {
        return getSpringContext().getBean(beanName);
      }
      catch(BeansException e) {
        if(logError) {
          LOGGER.error("error while looking for bean '" + beanName + "': " + e.getLocalizedMessage(), e);
        }
      }
    }
    else {
      if(logError) {
        LOGGER.debug("spring context not (yet) loaded/available !");
      }
    }
    return null;
  }

  private boolean isAnyBeanMissing(final String[] beanNames) {
    boolean ret = false;

    if(beanNames != null && beanNames.length > 0) {
      ret = Arrays.stream(beanNames).anyMatch(beanName -> StringUtils.isNotBlank(beanName) && getBean(beanName, false) == null);
    }
    return ret;
  }

  private boolean isContextInitialized() {
    boolean ret = false;
    if(getSpringContext() != null) {
      try {
        ret = (getSpringContext().getBeanFactory() != null);
      }
      catch(Exception ignored) {
        // osef
      }
    }
    return ret;
  }

  /**
   * get spring application context
   *
   * @return {@link ConfigurableApplicationContext}
   */
  public ConfigurableApplicationContext getSpringContext() {
    return (ConfigurableApplicationContext) SpringApplicationContextHolder.getSpringContext();
  }
}
