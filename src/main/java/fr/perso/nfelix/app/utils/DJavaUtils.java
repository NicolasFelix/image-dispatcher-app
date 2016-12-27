package fr.perso.nfelix.app.utils;

import java.io.InputStream;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Java utils methods User: N.FELIX
 */
public abstract class DJavaUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(DJavaUtils.class);

  private final static String   MAVEN_ROOT_PATH       = "/META-INF/maven/";
  private final static String   MAVEN_PROP_FILE       = "pom.properties";
  private final static String   SEP                   = "/";
  /** The available maven snapshot tokens */
  public final static  String[] MAVEN_SNAPSHOT_TOKENS = { "-SNAPSHOT", ".RC", "-RC" };

  /**
   * get class implementation (Manifest declaration)
   *
   * @param clazz class to look version for
   * @return class implementation version
   */
  public static String getClassVersion(Class<?> clazz) {
    if(clazz != null) {
      try {
        String ret = clazz.getPackage().getImplementationVersion();
        if(StringUtils.isEmpty(ret)) {
          ret = clazz.getPackage().getSpecificationVersion();
        }

        if(StringUtils.isEmpty(ret)) {
          LOGGER.warn("no implementation version found for class '{}' ! Has the manifest.MF file been delivered ?", clazz.getName());
        }
        return ret;
      }
      catch(Throwable th) {
        LOGGER.error("error while getting class information: " + th.getLocalizedMessage(), th);
      }
    }
    return null;
  }

  /**
   * get version from maven pom.properties file
   *
   * @param groupId    maven groupId
   * @param artifactId maven artifactId
   * @return maven artifact version
   */
  public static synchronized String getMavenVersion(final String groupId, final String artifactId) {

    if(StringUtils.isNotBlank(artifactId) && StringUtils.isNotBlank(groupId)) {
      try(InputStream is = DJavaUtils.class.getResourceAsStream(MAVEN_ROOT_PATH + groupId + SEP + artifactId + SEP + MAVEN_PROP_FILE)) {
        Properties p = new Properties();
        p.load(is);

        String ret = p.getProperty("version", "");
        if(StringUtils.isEmpty(ret)) {
          LOGGER.warn("no version found into pom.properties file (groupID:'{}', artifactID:'{}')", groupId, artifactId);
        }
        return ret;
      }
      catch(Exception e) {
        LOGGER.error("error while getting maven information: " + e.getLocalizedMessage(), e);
      }
    }
    return null;
  }

  /**
   * Defines if the maven version is a snapshot.
   *
   * @param groupId    the group id
   * @param artifactId the artifact id
   * @return true if the maven version is a snapshot
   */
  public static boolean isMavenVersionSnapshot(final String groupId, final String artifactId) {
    String version = getMavenVersion(groupId, artifactId);
    if(StringUtils.isEmpty(version)) {
      return false;
    }
    for(String snapshot : MAVEN_SNAPSHOT_TOKENS) {
      if(StringUtils.containsIgnoreCase(version, snapshot)) {
        return true;
      }
    }
    return false;
  }
}
