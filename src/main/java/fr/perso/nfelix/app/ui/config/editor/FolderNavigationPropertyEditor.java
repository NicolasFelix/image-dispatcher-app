package fr.perso.nfelix.app.ui.config.editor;

import static fr.perso.nfelix.app.ui.typedef.Constants.EMPTY_ICON;
import static fr.perso.nfelix.app.ui.typedef.Constants.FOLDER_ICON;

import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import fr.perso.nfelix.app.utils.fx.CustomBeanProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PropertySheet;

/**
 * Custom property editor enabling to add an action button
 *
 * @author N.FELIX
 */
@Slf4j
public class FolderNavigationPropertyEditor extends CustomAbstractPropertyEditor<String, HBox> {

  /** default open button id */
  public final static String OPEN_BUTTON_ID  = "openButtonID";
  /** default clean button id */
  public final static String CLEAN_BUTTON_ID = "cleanButtonID";

  public FolderNavigationPropertyEditor(PropertySheet.Item property) {
    this(property, buildBox(property));
  }

  private static HBox buildBox(PropertySheet.Item property) {
    final HBox hBox = new HBox();
    final TextField tf = new TextField();
    final ObservableList<Node> children = hBox.getChildren();
    tf.setDisable(true);
    tf.setMinWidth(400);
    children.add(tf);

    Button openButton = new Button();
    openButton.setGraphic(new ImageView(new Image(FOLDER_ICON)));
    openButton.setId(OPEN_BUTTON_ID);
    openButton.setTooltip(new Tooltip("Parcourir")); // no i18n
    if(property instanceof CustomBeanProperty) {
      ActionButtonPropertyEditor.addActionHandler(openButton, (CustomBeanProperty) property);
    }
    children.add(openButton);

    Button cleanButton = new Button();
    cleanButton.setId(CLEAN_BUTTON_ID);
    openButton.setTooltip(new Tooltip("Vider la valeur")); // no i18n
    cleanButton.setGraphic(new ImageView(new Image(EMPTY_ICON)));
    if(property instanceof CustomBeanProperty) {
      ActionButtonPropertyEditor.addActionHandler(cleanButton, (CustomBeanProperty) property);
    }
    children.add(cleanButton);
    return hBox;
  }

  public FolderNavigationPropertyEditor(PropertySheet.Item property, HBox control) {
    this(property, control, true);
  }

  public FolderNavigationPropertyEditor(PropertySheet.Item property, HBox control, boolean readonly) {
    super(property, control, readonly);
  }

  @Override
  protected ObservableValue<String> getObservableValue() {
    return null;
  }

  @Override
  public void setValue(String value) {
    final TextField tf = JavaFXUtils.getFirstNodeFromComponent(getEditor(), TextField.class);
    if(tf != null) {
      tf.setText(value);
    }

    final Node nodeFromID = JavaFXUtils.getNodeFromID(getEditor(), CLEAN_BUTTON_ID);
    if(nodeFromID != null) {
      nodeFromID.setVisible(StringUtils.isNotBlank(value));
    }
  }
}
