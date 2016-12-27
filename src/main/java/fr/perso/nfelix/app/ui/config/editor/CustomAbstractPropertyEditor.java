package fr.perso.nfelix.app.ui.config.editor;

import javafx.scene.Node;
import org.controlsfx.control.PropertySheet;
import org.controlsfx.property.editor.AbstractPropertyEditor;

/**
 * {@link org.controlsfx.property.editor.AbstractPropertyEditor}
 *
 * @author N.FELIX
 */
abstract class CustomAbstractPropertyEditor<T, C extends Node> extends AbstractPropertyEditor<T, C> {

  public final static String ICON_KEY          = "icon";
  public final static String EVENT_HANDLER_KEY = "eventAction";

  public CustomAbstractPropertyEditor(PropertySheet.Item property, C control) {
    super(property, control);
  }

  public CustomAbstractPropertyEditor(PropertySheet.Item property, C control, boolean readonly) {
    super(property, control, readonly);
  }
}
