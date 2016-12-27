package fr.perso.nfelix.app.utils.fx;

import fr.perso.nfelix.app.ui.config.IBeanConfigAware;
import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.control.PropertySheet.Item;

/**
 * Convenience utility class for creating {@link PropertySheet} instances based
 * on a JavaBean.
 */
@Slf4j
public abstract class BeanPropertyToSheetItemConverter {

  private BeanPropertyToSheetItemConverter() {
    // no op
  }

  /**
   * Given a JavaBean, this method will return a list of {@link Item} intances,
   * which may be directly placed inside a {@link PropertySheet} (via its
   * {@link PropertySheet#getItems() items list}.
   * This method will not return read-only properties.
   *
   * @param bean The JavaBean that should be introspected and be editable via
   *             a {@link PropertySheet}.
   * @return A list of {@link Item} instances representing the properties of the
   * JavaBean.
   */
  public static ObservableList<Item> getProperties(final Object bean) {
    return getProperties(bean, (p) -> true);
  }

  /**
   * Given a JavaBean, this method will return a list of {@link Item} intances,
   * which may be directly placed inside a {@link PropertySheet} (via its
   * {@link PropertySheet#getItems() items list}.
   *
   * @param bean      The JavaBean that should be introspected and be editable via
   *                  a {@link PropertySheet}.
   * @param predicate Predicate to test whether the property should be included in the
   *                  list of results.
   * @return A list of {@link Item} instances representing the properties of the
   * JavaBean.
   */
  public static ObservableList<Item> getProperties(final Object bean, Predicate<PropertyDescriptor> predicate) {
    ObservableList<Item> ret = FXCollections.observableList(new LinkedList<>());
    try {
      BeanInfo beanInfo = Introspector.getBeanInfo(bean.getClass(), null);
      if(beanInfo instanceof IBeanConfigAware) {
        ((IBeanConfigAware) beanInfo).setParentBean(bean);
      }
      List<PropertyDescriptor> propertyDescriptors = Arrays.asList(beanInfo.getPropertyDescriptors());

      if(propertyDescriptors != null && !propertyDescriptors.isEmpty()) {
        Collections.sort(propertyDescriptors, new CustomPropertyDescriptor.OrderedPropertyDescComparable());
      }

      ret.addAll(propertyDescriptors.stream().filter(p -> ((CustomPropertyDescriptor) p).getVisiblePredicate().test((CustomPropertyDescriptor) p))
          .map(p -> new CustomBeanProperty(bean, p)).collect(Collectors.toList()));
    }
    catch(IntrospectionException ie) {
      LOGGER.error("error while introspecting bean: '" + bean.getClass().getSimpleName() + "' --> " + ie.getLocalizedMessage(), ie);
    }
    return ret;
  }

}
