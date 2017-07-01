package fr.perso.nfelix.app.ui.config;

import fr.perso.nfelix.app.utils.fx.CustomPropertyDescriptor;
import java.beans.BeanDescriptor;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.beans.SimpleBeanInfo;
import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.Getter;

/**
 * @author N.FELIX
 */
public class AbstractBeanAware extends SimpleBeanInfo implements IBeanConfigAware {

  @Getter(AccessLevel.PROTECTED)
  private   Class  beanClazz;
  protected Object parentBean;

  public AbstractBeanAware(Class beanClazz) {
    super();
    this.beanClazz = beanClazz;
  }

  @Override
  public void setParentBean(Object parentBean) {
    this.parentBean = parentBean;
  }

  @Override
  public Object getParentBean() {
    return parentBean;
  }

  @Override
  public int getDefaultPropertyIndex() {
    return -1;
  }

  @Override
  public BeanDescriptor getBeanDescriptor() {
    return new BeanDescriptor(beanClazz);
  }

  protected PropertyDescriptor buildPropertyDescriptor(String[] keys, int order)
      throws IntrospectionException {

    return buildPropertyDescriptor(keys, order, p -> true);
  }

  protected PropertyDescriptor buildPropertyDescriptor(String[] keys, int order, Predicate<CustomPropertyDescriptor> visiblePredicate)
      throws IntrospectionException {

    CustomPropertyDescriptor cpd = new CustomPropertyDescriptor(keys[0], getBeanClazz());
    cpd.setDisplayName(keys[1]);
    cpd.setOrder(order);
    cpd.setVisiblePredicate(visiblePredicate);

    // do not forget to increment order from the caller
    return cpd;
  }
}
