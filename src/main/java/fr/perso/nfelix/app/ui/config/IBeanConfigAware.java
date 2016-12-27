package fr.perso.nfelix.app.ui.config;

/**
 * Make a bean config aware of its bean
 *
 * @author N.FELIX
 */
public interface IBeanConfigAware {

  /**
   * sets parent bean
   *
   * @param parentBean parent bean
   */
  void setParentBean(Object parentBean);

  /**
   * gets parent bean
   *
   * @return bean if exists/defined
   */
  Object getParentBean();
}
