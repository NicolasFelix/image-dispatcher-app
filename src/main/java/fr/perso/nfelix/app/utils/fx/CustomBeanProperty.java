package fr.perso.nfelix.app.utils.fx;

import java.beans.PropertyDescriptor;
import java.util.HashMap;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.property.BeanProperty;

/**
 * extended {@link BeanProperty}
 * @author N.FELIX
 */
public class CustomBeanProperty extends BeanProperty {

  private Map<String, Object> additionalProps = null;

  /**
   * Constructor
   * @param bean               bean
   * @param propertyDescriptor     bean descriptor
   */
  public CustomBeanProperty(Object bean, PropertyDescriptor propertyDescriptor) {
    super(bean, propertyDescriptor);

    initAdditionalProps(propertyDescriptor);
  }

  private void initAdditionalProps(PropertyDescriptor propertyDescriptor) {
    if(propertyDescriptor instanceof CustomPropertyDescriptor) {
      final Map<String, Object> addedProps = ((CustomPropertyDescriptor) propertyDescriptor).getAdditionalProps();
      if(addedProps != null) {
        for(Map.Entry<String, Object> e : addedProps.entrySet()) {
          addProperty(e.getKey(), e.getValue());
        }
      }
    }
  }

  @Override
  public IPropertySheetBean getBean() {
    return (IPropertySheetBean) super.getBean();
  }

  @Override
  public String getCategory() {
    return getBean().getCategory();
  }

  private void addProperty(final String key, final Object value) {
    if(StringUtils.isBlank(key)) {
      return;
    }

    if(additionalProps == null) {
      additionalProps = new HashMap<>();
    }
    additionalProps.put(key, value);
  }

  /**
   * get Property from key
   * @param key key
   * @return value
   */
  public Object getProperty(final String key) {
    if(additionalProps == null) {
      return null;
    }
    if(StringUtils.isBlank(key)) {
      return null;
    }

    return additionalProps.get(key);
  }
}
