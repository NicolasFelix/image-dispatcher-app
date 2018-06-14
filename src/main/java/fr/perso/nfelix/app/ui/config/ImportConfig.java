package fr.perso.nfelix.app.ui.config;

import fr.perso.nfelix.app.utils.fx.AbstractPropertySheetBean;
import java.util.ResourceBundle;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.lang3.StringUtils;

/**
 * GlobalConfig
 *
 * @author N.FELIX
 */
@Getter
@Setter
@Slf4j
@EqualsAndHashCode(doNotUseGetters = true, callSuper = true)
public class ImportConfig extends AbstractPropertySheetBean {

  /** import folder definition */
  static final String[] IMPORTFOLDER_KEY = { "importFolder", "R\u00e9pertoire contenant les images \u00e0 int\u00e9grer/scanner" };
  /** scan folder definition */
  static final String[] SCANFOLDER_KEY   = { "scanFolder", "R\u00e9pertoire de sortie" };
  /** rename file definition */
  static final String[] RENAMEFILE_KEY   = { "renameFile", "Renommage syst\u00e9matique des fichiers" };
  /** time offset definition */
  static final String[] TIMEOFFSET_KEY   = { "timeOffset", "Appliquer un décalage sur la date du fichier" };

  private String  importFolder;
  private String  scanFolder;
  private int     timeOffset = 0;
  private boolean renameFile = false;

  public ImportConfig(String category, ResourceBundle resources) {
    super(category, resources);
  }

  /**
   * check if structure is valid
   *
   * @return true if valid
   */
  public boolean isValid() {
    return StringUtils.isNotBlank(importFolder) && StringUtils.isNotBlank(scanFolder);
  }

  @Override
  public String[] getPropertyNames() {
    return new String[] { IMPORTFOLDER_KEY[0], SCANFOLDER_KEY[0], RENAMEFILE_KEY[0], TIMEOFFSET_KEY[0] };
  }

  @Override
  public Class getPropertyType(final String propName) {

    if(RENAMEFILE_KEY[0].equals(propName)) {
      return Boolean.TYPE;
    }
    if(TIMEOFFSET_KEY[0].equals(propName)) {
      return Integer.TYPE;
    }
    return super.getPropertyType(propName);
  }

  @Override
  public ImportConfig clone() {
    try {
      return (ImportConfig) super.clone();
    }
    catch(CloneNotSupportedException e) {
      LOGGER.error("ImportConfig clone failed: " + e.getLocalizedMessage(), e);
    }
    return null;
  }

  @Override
  public void readAdditionalProperties(SubnodeConfiguration iniFile) {
  }
}
