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

  public final static String[] IMPORTFOLDER_KEY = { "importFolder", "Répertoire contenant les images à intégrer/scanner" };
  public final static String[] SCANFOLDER_KEY   = { "scanFolder", "Répertoire de sortie" };

  private String importFolder;
  private String scanFolder;

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
    return new String[] { IMPORTFOLDER_KEY[0], SCANFOLDER_KEY[0] };
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
