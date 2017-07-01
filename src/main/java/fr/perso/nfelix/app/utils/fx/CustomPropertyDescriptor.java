package fr.perso.nfelix.app.utils.fx;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

/**
 * <pre>
 * extended {@link java.beans.PropertyDescriptor} enabling to sort/order bean properties
 * and visible status
 * </pre>
 *
 * @author N.FELIX
 */
public class CustomPropertyDescriptor extends PropertyDescriptor {

  @Getter
  private Map<String, Object> additionalProps = null;

  @Getter
  @Setter
  private int order;

  @Setter
  private Predicate<CustomPropertyDescriptor> visiblePredicate;

  public CustomPropertyDescriptor(String propertyName, Class<?> beanClass)
      throws IntrospectionException {
    super(propertyName, beanClass);
  }

  public CustomPropertyDescriptor(String propertyName, Class<?> beanClass, String readMethodName, String writeMethodName)
      throws IntrospectionException {
    super(propertyName, beanClass, readMethodName, writeMethodName);
  }

  public CustomPropertyDescriptor(String propertyName, Method readMethod, Method writeMethod)
      throws IntrospectionException {
    super(propertyName, readMethod, writeMethod);
  }

  public void addProperty(final String key, final Object value) {
    if(StringUtils.isBlank(key)) {
      return;
    }

    if(additionalProps == null) {
      additionalProps = new HashMap<>();
    }
    additionalProps.put(key, value);
  }

  /**
   * get null safe predicate
   *
   * @return {@link Predicate}
   */
  public Predicate<CustomPropertyDescriptor> getVisiblePredicate() {
    if(visiblePredicate == null) {
      visiblePredicate = propertyDescriptor -> true;
    }
    return visiblePredicate;
  }

  /**
   * Order Property Comparator clazz
   */
  public static class OrderedPropertyDescComparable implements Comparator<PropertyDescriptor>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(PropertyDescriptor o1, PropertyDescriptor o2) {
      int ret = 0;
      if(o1 != null && o2 != null) {
        if(o1 instanceof CustomPropertyDescriptor && o2 instanceof CustomPropertyDescriptor) {
          ret = ((CustomPropertyDescriptor) o1).getOrder() - ((CustomPropertyDescriptor) o2).getOrder();
        }

        if(ret == 0) {
          if(o1.getDisplayName() != null) {
            ret = o1.getDisplayName().compareTo(o2.getDisplayName());
          }
          else {
            if(o2.getDisplayName() != null) {
              ret = 1;
            }
          }
          if(ret == 0) {
            if(o1.getName() != null) {
              ret = o1.getName().compareTo(o2.getName());
            }
            else if(o2.getName() != null) {
              ret = 1;
            }
          }
        }
      }
      return ret;
    }
  }

  @Override
  public boolean equals(Object obj) {
    return super.equals(obj) && order == ((CustomPropertyDescriptor) obj).getOrder();
  }

  @Override
  public int hashCode() {
    return 37 * super.hashCode() + Integer.hashCode(order);
  }
}
