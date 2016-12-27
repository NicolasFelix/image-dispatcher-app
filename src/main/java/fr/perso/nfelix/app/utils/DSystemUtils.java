package fr.perso.nfelix.app.utils;

import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * Operations on {@link String} that are <code>null</code> safe.
 * </p>
 *
 * @author N.FELIX
 */
public abstract class DSystemUtils {

  /** The logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DSystemUtils.class);

  /**
   * Set system property to <code>value</code>. If SecurityManager denies property modification, silently ignore property change. if value is null, property
   * won't be set.
   *
   * @param property property name
   * @param value    new value
   */
  public static void setProperty(String property, Object value) {
    try {
      if(value != null) {
        Properties prop = System.getProperties();
        prop.put(property, value);
      }
    }
    catch(SecurityException ex) {
      // recall method so that property change takes effect
      LOGGER.error("Security exception, property could not be set.", ex);
    }
  }

  /**
   * get system property value<br/>
   * null if not found
   *
   * @param property property name
   * @return value
   */
  public static String getProperty(String property) {
    return getProperty(property, null);
  }

  /**
   * get system property value<br/>
   * null if not found
   *
   * @param property     property name
   * @param defaultValue default value, if not found
   * @return value
   */
  public static String getProperty(String property, final String defaultValue) {
    try {
      return System.getProperty(property, defaultValue);
    }
    catch(SecurityException ex) {
      // recall method so that property change takes effect
      LOGGER.error("Security exception, property could not be get.", ex);
    }
    return defaultValue;
  }

  /**
   * is system property already set ?
   *
   * @param property property name
   * @return true if already set
   */
  public static boolean isPropertySet(String property) {
    return (getProperty(property) != null);
  }
}
