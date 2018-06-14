package fr.perso.nfelix.app.ui.config;

import static fr.perso.nfelix.app.ui.config.GlobalConfig.*;
import static fr.perso.nfelix.app.ui.config.editor.ComboWithIconPropertyEditor.ITEMS_KEY;

import fr.perso.nfelix.app.ui.config.editor.ComboWithIconPropertyEditor;
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
 * Global configuration Ben Info, used while creating a {@link org.controlsfx.control.PropertySheet} element.
 *
 * @author N.FELIX
 */
@Getter
@Setter
@Slf4j
public class GlobalConfigBeanInfo extends AbstractBeanAware {

  public GlobalConfigBeanInfo() {
    super(GlobalConfig.class);
  }

  @Override
  public PropertyDescriptor[] getPropertyDescriptors() {

    try {
      // output format
      int order = 1;
      List<PropertyDescriptor> pds = new ArrayList<>(3);

      // log level
      CustomPropertyDescriptor pdLogLevel = (CustomPropertyDescriptor) buildPropertyDescriptor(LOGLEVEL_KEY, order);
      pdLogLevel.setPropertyEditorClass(ComboWithIconPropertyEditor.class);
      pdLogLevel.addProperty(ITEMS_KEY, Arrays.asList(AVAILABLE_LOG_LEVELS));
      pds.add(pdLogLevel);
      ++order;
      // theme
      CustomPropertyDescriptor pdTheme = (CustomPropertyDescriptor) buildPropertyDescriptor(THEME_KEY, order);
      pdTheme.setPropertyEditorClass(ComboWithIconPropertyEditor.class);
      pdTheme.addProperty(ITEMS_KEY, Arrays.asList(AVAILABLE_THEMES));
      pds.add(pdTheme);
      ++order;

      pds.add(buildPropertyDescriptor(DUMPSTEP_KEY, order));

      return pds.toArray(new PropertyDescriptor[0]);
    }
    catch(IntrospectionException ie) {
      LOGGER.error("error while inspecting bean: " + ie.getLocalizedMessage(), ie);
    }
    return null;
  }

}
