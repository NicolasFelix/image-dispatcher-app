package fr.perso.nfelix.app.ui.config;

import static fr.perso.nfelix.app.ui.config.ImportConfig.*;
import static fr.perso.nfelix.app.ui.config.editor.SpinnerPropertyEditor.RANGE_KEY;

import fr.perso.nfelix.app.DispatcherConfig;
import fr.perso.nfelix.app.ui.config.editor.ActionButtonPropertyEditor;
import fr.perso.nfelix.app.ui.config.editor.FolderNavigationPropertyEditor;
import fr.perso.nfelix.app.ui.config.editor.SpinnerPropertyEditor;
import fr.perso.nfelix.app.utils.fx.CustomPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Import configuration BeanInfo, used while creating a {@link org.controlsfx.control.PropertySheet} element.
 *
 * @author N.FELIX
 */
@Getter
@Setter
@Slf4j
public class ImportConfigBeanInfo extends AbstractBeanAware {

  public ImportConfigBeanInfo() {
    super(ImportConfig.class);
  }

  @Override
  public PropertyDescriptor[] getPropertyDescriptors() {

    try {
      int order = 1;
      List<PropertyDescriptor> pds = new ArrayList<>(16);

      // import folder
      final CustomPropertyDescriptor pdImport = (CustomPropertyDescriptor) buildPropertyDescriptor(IMPORTFOLDER_KEY, order);
      pdImport.setPropertyEditorClass(FolderNavigationPropertyEditor.class);
      pdImport.addProperty(ActionButtonPropertyEditor.EVENT_HANDLER_KEY, "chooseFolder");
      pds.add(pdImport);
      ++order;
      // scan folder
      final CustomPropertyDescriptor pdScan = (CustomPropertyDescriptor) buildPropertyDescriptor(SCANFOLDER_KEY, order);
      pdScan.setPropertyEditorClass(FolderNavigationPropertyEditor.class);
      pdScan.addProperty(ActionButtonPropertyEditor.EVENT_HANDLER_KEY, "chooseFolder");
      pds.add(pdScan);
      ++order;

      final CustomPropertyDescriptor pdTimeOffset = (CustomPropertyDescriptor) buildPropertyDescriptor(TIMEOFFSET_KEY, order);
      // pdTimeOffset.setPropertyEditorClass(SpinnerPropertyEditor.class);
      pdTimeOffset.addProperty(RANGE_KEY, Arrays.asList(-18, 18));
      pds.add(pdTimeOffset);
      ++order;
      
      final CustomPropertyDescriptor pdRenameFile = (CustomPropertyDescriptor) buildPropertyDescriptor(RENAMEFILE_KEY, order);
      pds.add(pdRenameFile);
      // ++order;

      return pds.toArray(new PropertyDescriptor[0]);
    }
    catch(IntrospectionException ie) {
      LOGGER.error("error while inspecting bean: " + ie.getLocalizedMessage(), ie);
    }
    return null;
  }

}
