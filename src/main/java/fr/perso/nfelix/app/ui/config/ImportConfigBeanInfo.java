package fr.perso.nfelix.app.ui.config;

import static fr.perso.nfelix.app.ui.config.ImportConfig.IMPORTFOLDER_KEY;
import static fr.perso.nfelix.app.ui.config.ImportConfig.SCANFOLDER_KEY;

import fr.perso.nfelix.app.ui.config.editor.ActionButtonPropertyEditor;
import fr.perso.nfelix.app.ui.config.editor.FolderNavigationPropertyEditor;
import fr.perso.nfelix.app.utils.fx.CustomPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
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

      // zipFolder
      final CustomPropertyDescriptor pdZip = (CustomPropertyDescriptor) buildPropertyDescriptor(IMPORTFOLDER_KEY, order);
      pdZip.setPropertyEditorClass(FolderNavigationPropertyEditor.class);
      pdZip.addProperty(ActionButtonPropertyEditor.EVENT_HANDLER_KEY, "chooseFolder");
      pds.add(pdZip);
      ++order;
      // inputFolder
      final CustomPropertyDescriptor pdInput = (CustomPropertyDescriptor) buildPropertyDescriptor(SCANFOLDER_KEY, order);
      pdInput.setPropertyEditorClass(FolderNavigationPropertyEditor.class);
      pdInput.addProperty(ActionButtonPropertyEditor.EVENT_HANDLER_KEY, "chooseFolder");
      pds.add(pdInput);
      ++order;

      return pds.toArray(new PropertyDescriptor[pds.size()]);
    }
    catch(IntrospectionException ie) {
      LOGGER.error("error while inspecting bean: " + ie.getLocalizedMessage(), ie);
    }
    return null;
  }

}
