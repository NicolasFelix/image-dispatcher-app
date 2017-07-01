package fr.perso.nfelix.app;

import fr.perso.nfelix.app.ui.config.GlobalConfig;
import fr.perso.nfelix.app.ui.config.ImportConfig;
import fr.perso.nfelix.app.ui.typedef.JobConstants;
import fr.perso.nfelix.app.utils.DFileUtils;
import fr.perso.nfelix.app.utils.DIOUtils;
import fr.perso.nfelix.app.utils.DSystemUtils;
import fr.perso.nfelix.app.utils.fx.AbstractPropertySheetBean;
import fr.perso.nfelix.app.utils.sgbd.DalConstants;
import java.io.*;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

/**
 * ExchangeConfig class holding application configuration
 *
 * @author N.FELIX
 */
@Slf4j
@EqualsAndHashCode(doNotUseGetters = true, exclude = { "dirty", "preventDirtyChanges", })
public class DispatcherConfig implements Cloneable, Serializable {

  private static final long serialVersionUID = 1L;

  private final static String INI_FILE_NAME        = "imgDispatcher.properties";
  private final static String CONFIG_FOLDER        = "config/";
  private final static String SAVE_CONFIG_FOLDER   = CONFIG_FOLDER + "saved/";
  // used to inject property to Spring
  private static final String SYSTEM_CONFIG_FOLDER = "config.folder";

  private final static String IMPORT_SECTION = "01 - Import";
  private final static String GLOBAL_SECTION = "99 - Autres";

  private final static String METHOD_GET_PREFIX = "get";
  private final static String METHOD_IS_PREFIX  = "is";
  private final static String METHOD_SET_PREFIX = "set";

  private final static String QUOTE              = "\"";
  private final static String EQUAL              = "=";
  private final static String COMMA              = ",";
  private final static String COMMA_SUBSTITUTION = "¤¤";

  private transient ResourceBundle mainResources;

  @Getter
  private ImportConfig importConfig;

  @Getter
  private GlobalConfig globalConfig;

  @Getter
  @Setter
  private static boolean expertMode = false;

  @Getter
  private static String executionPath = DSystemUtils.getProperty("imgDispatcher.app.path");

  // indicate if options should be saved.
  @Getter
  @Setter
  private boolean dirty = false;

  // indicate if dirty option can be changed (usefull while working on UI initialization)
  @Getter
  @Setter
  private boolean preventDirtyChanges = false;

  @Getter
  private Map<String, AbstractPropertySheetBean> subConfigs;

  /**
   * Constructeur
   *
   * @param mainResources application main resource bundle file
   */
  public DispatcherConfig(ResourceBundle mainResources) {
    super();

    this.mainResources = mainResources;

    try {
      executionPath = FilenameUtils.normalizeNoEndSeparator(executionPath);
      if(StringUtils.isBlank(executionPath)) {
        executionPath = FilenameUtils
            .normalizeNoEndSeparator(new File(DispatcherConfig.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath()).getParent());
      }

      executionPath += File.separatorChar;
      try {
        FileUtils.forceMkdir(new File(getConfigFolder()));
      }
      catch(IOException e) {
        // ignored
      }

      DSystemUtils.setProperty(SYSTEM_CONFIG_FOLDER, getConfigFolder());
      LOGGER.info("executionPath: '{}'", executionPath);

    }
    catch(URISyntaxException e) {
      LOGGER.error("error while getting library path:" + e.getLocalizedMessage(), e);
    }
  }

  private void clear() {
    globalConfig = new GlobalConfig(GLOBAL_SECTION, mainResources);
    importConfig = new ImportConfig(IMPORT_SECTION, mainResources);

    subConfigs = new HashMap<>(1);
    subConfigs.put(GLOBAL_SECTION, globalConfig);
    subConfigs.put(IMPORT_SECTION, importConfig);
  }

  /**
   * load ini file
   */
  public void load() {

    clear();

    INIConfiguration iniFile = new INIConfiguration();
    File iniF = new File(executionPath + CONFIG_FOLDER + INI_FILE_NAME);
    if(!iniF.exists()) {
      return;
    }

    try(FileInputStream fis = new FileInputStream(iniF); InputStreamReader reader = new InputStreamReader(fis, "UTF8")) {
      iniFile.read(reader);
    }
    catch(ConfigurationException | IOException e) {
      LOGGER.error("error while loading mapping file '{}'", executionPath + CONFIG_FOLDER + INI_FILE_NAME);
      LOGGER.error(e.getLocalizedMessage(), e);
    }

    for(Map.Entry<String, AbstractPropertySheetBean> config : subConfigs.entrySet()) {
      final SubnodeConfiguration section = iniFile.getSection(config.getKey());
      if(section != null) {
        initConfigFromSection(config.getValue(), section);
      }
    }
  }

  public void save()
      throws ConfigurationException, IOException {

    final String iniFileName = executionPath + CONFIG_FOLDER + INI_FILE_NAME;
    final File localInitFile = new File(iniFileName);
    if(localInitFile.exists()) {

      // make a save, if not existing yet
      final File savedFolder = new File(executionPath + SAVE_CONFIG_FOLDER);
      String formatteToday = LocalDateTime.now().format(DateTimeFormatter.ofPattern(DalConstants.SAVE_PATTERN_DATE));

      File savedIniFile = new File(savedFolder, formatteToday + "_" + INI_FILE_NAME);
      if(!savedIniFile.exists()) {
        DFileUtils.forceMkdirQuietly(savedFolder);
        DIOUtils.copyFile(localInitFile, savedIniFile);
      }

      DFileUtils.forceDeleteQuietly(localInitFile);
    }
    if(!localInitFile.createNewFile()) {
      LOGGER.error("unable to create file '{}'", iniFileName);
    }
    INIConfiguration iniFile = new INIConfiguration();

    for(AbstractPropertySheetBean config : subConfigs.values()) {
      addSectionFromConfig(iniFile, config);
    }

    try(FileWriter writer = new FileWriter(iniFileName)) {
      iniFile.write(writer);
    }
    finally {
      dirty = false;
    }

  }

  private void initConfigFromSection(AbstractPropertySheetBean config, SubnodeConfiguration iniSection) {

    final Iterator<String> keys = iniSection.getKeys();
    while(keys.hasNext()) {
      String k = keys.next();

      Arrays.stream(config.getPropertyNames()).filter(propName -> StringUtils.equalsIgnoreCase(k, propName)).findFirst().ifPresent(
          propName -> invokeSetMethod(config, propName, StringUtils.replace(StringUtils.removeEnd(iniSection.getString(k), QUOTE), COMMA_SUBSTITUTION, COMMA)));
    }

    config.readAdditionalProperties(iniSection);
  }

  private void addSectionFromConfig(INIConfiguration iniFile, AbstractPropertySheetBean config) {
    if(config != null) {
      for(String propName : config.getPropertyNames()) {

        final Object value = invokeGetMethod(config, propName);
        Object finalValue = value;
        if(value != null && value instanceof String) {

          finalValue = StringUtils.replace((String) value, "\r\n", "\n");
          finalValue = StringUtils.replace((String) finalValue, "\n", "  \\\n");
          finalValue = StringUtils.replace((String) finalValue, COMMA, COMMA_SUBSTITUTION);
          if(StringUtils.contains((String) value, EQUAL)) {
            finalValue = QUOTE + finalValue + QUOTE;
          }
        }
        iniFile.addProperty(config.getCategory() + "." + propName, finalValue);
      }
      config.writeAdditionalProperties(iniFile);
    }
  }

  /**
   * get computed config folder
   *
   * @return /config/
   */
  public static String getConfigFolder() {
    return executionPath + CONFIG_FOLDER;
  }

  private void invokeSetMethod(AbstractPropertySheetBean config, final String methodName, final String value) {
    Class typeClazz = config.getPropertyType(methodName);
    final String capitalizeMN = StringUtils.capitalize(methodName);
    Method method = MethodUtils.getAccessibleMethod(config.getClass(), METHOD_SET_PREFIX + capitalizeMN, typeClazz);
    if(method == null) {
      LOGGER.warn("no SET method '{}' found for class '{}'", capitalizeMN, config.getClass().getSimpleName());
      return;
    }
    try {
      method.invoke(config, ConvertUtils.convert(value, typeClazz));
    }
    catch(Exception e) {
      LOGGER.error("invokeSetMethod(" + config + ", " + methodName + ", " + value + "): " + e.getLocalizedMessage(), e);
    }
  }

  private Object invokeGetMethod(Object bean, final String methodName) {
    final String capitalizeMN = StringUtils.capitalize(methodName);
    Method method = MethodUtils.getAccessibleMethod(bean.getClass(), METHOD_GET_PREFIX + capitalizeMN);
    if(method == null) {
      // boolean ?
      method = MethodUtils.getAccessibleMethod(bean.getClass(), METHOD_IS_PREFIX + capitalizeMN);
      if(method == null) {
        LOGGER.warn("no GET method '{}' found for class '{}'", capitalizeMN, bean.getClass().getSimpleName());
        return null;
      }
    }
    try {
      return method.invoke(bean);
    }
    catch(Exception e) {
      LOGGER.error("invokeGetMethod(" + bean + ", " + methodName + "): " + e.getLocalizedMessage(), e);
    }
    return null;
  }

  /**
   * write job parameters to a property file
   *
   * @param propertiesFileName property file name
   */
  public void writeJobParameters(final String propertiesFileName)
      throws IOException {
    Properties props = new Properties();

    for(AbstractPropertySheetBean config : subConfigs.values()) {
      config.addJobConfigParameters(props);
    }

    // this can be used while generated extract, to guess header and footer templates
    props.put(JobConstants.SETUP_PATH_KEY, getConfigFolder());

    try(FileOutputStream fos = new FileOutputStream(new File(getConfigFolder(), propertiesFileName))) {
      props.store(fos, " Do not write to this file, it is automatically generated !!!");
    }
    catch(IOException e) {
      LOGGER.error("error while saving '{}': '{}'", propertiesFileName, e.getLocalizedMessage());
      LOGGER.error(e.getLocalizedMessage(), e);
      throw e;
    }
  }

  @Override
  public DispatcherConfig clone() {
    try {
      return (DispatcherConfig) super.clone();
    }
    catch(CloneNotSupportedException e) {
      LOGGER.error("ExchangeConfig clone failed: " + e.getLocalizedMessage(), e);
    }
    return null;
  }
}
