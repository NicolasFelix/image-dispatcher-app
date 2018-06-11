package fr.perso.nfelix.app.ui.config.editor;

import fr.perso.nfelix.app.utils.fx.CustomBeanProperty;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PropertySheet;

/**
 * Custom property editor enabling to add an action button
 *
 * @author N.FELIX
 */
@Slf4j
public class ActionButtonPropertyEditor extends CustomAbstractPropertyEditor<Boolean, Button> {

  public ActionButtonPropertyEditor(PropertySheet.Item property) {
    this(property, new Button());
  }

  public ActionButtonPropertyEditor(PropertySheet.Item property, Button control) {
    this(property, control, true);
  }

  /**
   * constructor
   * @param property property item
   * @param control control linked to this propoerty
   * @param readonly read-only mode ?
   */
  public ActionButtonPropertyEditor(PropertySheet.Item property, Button control, boolean readonly) {
    super(property, control, readonly);

    initButton(property);
  }

  private void initButton(PropertySheet.Item property) {
    getEditor().setText(property.getDescription());

    if(property instanceof CustomBeanProperty) {
      final CustomBeanProperty parentBean = (CustomBeanProperty) property;
      String icon = (String) parentBean.getProperty(ICON_KEY);
      if(StringUtils.isNotBlank(icon)) {
        getEditor().setGraphic(new ImageView(icon));
      }

      addActionHandler(getEditor(), parentBean);
    }
  }

  @Override
  protected ObservableValue<Boolean> getObservableValue() {
    return null;
  }

  @Override
  public void setValue(Boolean value) {
    // osef
  }

  /**
   * action handler definition
   * @param button source button
   * @param beanProperty bean properties
   */
  public static void addActionHandler(ButtonBase button, CustomBeanProperty beanProperty) {

    String eh = (String) beanProperty.getProperty(EVENT_HANDLER_KEY);
    if(StringUtils.isNotBlank(eh) && beanProperty.getBean() != null) {

      try {
        final Method eventHandlerMethod = beanProperty.getBean().getClass().getMethod(eh, ActionEvent.class, CustomBeanProperty.class);
        button.setOnAction(event -> {
          try {
            eventHandlerMethod.invoke(beanProperty.getBean(), event, beanProperty);
          }
          catch(IllegalAccessException | InvocationTargetException e) {
            LOGGER.error("error while invoking method '" + eh + "': " + e.getLocalizedMessage(), e);
          }
        });
      }
      catch(NoSuchMethodException e) {
        LOGGER.error("error while getting method '" + eh + "': " + e.getLocalizedMessage(), e);
      }
    }
  }
}
