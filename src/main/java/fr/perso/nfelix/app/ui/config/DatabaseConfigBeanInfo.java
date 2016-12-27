package fr.perso.nfelix.app.ui.config;

import static fr.perso.nfelix.app.ui.config.DatabaseConfig.*;

import fr.perso.nfelix.app.ui.config.editor.ActionButtonPropertyEditor;
import fr.perso.nfelix.app.ui.typedef.Constants;
import fr.perso.nfelix.app.utils.fx.CustomPropertyDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

/**
 * Database configuration BeanInfo, used while creating a {@link org.controlsfx.control.PropertySheet} element.
 *
 * @author N.FELIX
 */
@Getter
@Setter
@Slf4j
public class DatabaseConfigBeanInfo extends AbstractBeanAware {

  public DatabaseConfigBeanInfo() {
    super(DatabaseConfig.class);
  }

  @Override
  public PropertyDescriptor[] getPropertyDescriptors() {

    try {
      int order = 1;

      List<PropertyDescriptor> pds = new ArrayList<>(4);

      // url
      pds.add(buildPropertyDescriptor(URL_KEY, order));
      ++order;
      // username
      pds.add(buildPropertyDescriptor(USER_KEY, order));
      ++order;
      // password
      pds.add(buildPropertyDescriptor(PASSWORD_KEY, order));
      ++order;

      // test button
      CustomPropertyDescriptor pdTest = new CustomPropertyDescriptor("fakeMethod", getBeanClazz());
      pdTest.addProperty(ActionButtonPropertyEditor.ICON_KEY, Constants.TEST_ICON);
      pdTest.addProperty(ActionButtonPropertyEditor.EVENT_HANDLER_KEY, "handleTestMethod");
      pdTest.setPropertyEditorClass(ActionButtonPropertyEditor.class);
      pdTest.setDisplayName("Tester");
      pdTest.setOrder(order);
      pds.add(pdTest);

      return pds.toArray(new PropertyDescriptor[pds.size()]);
    }
    catch(IntrospectionException ie) {
      LOGGER.error("error while inspecting bean: " + ie.getLocalizedMessage(), ie);
    }
    return null;
  }

}
