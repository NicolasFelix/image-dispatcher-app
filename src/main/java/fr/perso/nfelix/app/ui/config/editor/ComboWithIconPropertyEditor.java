package fr.perso.nfelix.app.ui.config.editor;

import fr.perso.nfelix.app.utils.fx.CustomBeanProperty;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.PropertySheet;

/**
 * <pre>
 * Custom property editor enabling to add icon to label
 * see {@link org.controlsfx.property.editor.Editors#createChoiceEditor(PropertySheet.Item, Collection)}
 * </pre>
 *
 * @author N.FELIX
 */
@Slf4j
public class ComboWithIconPropertyEditor extends CustomAbstractPropertyEditor<String, ComboBox> {

  public final static String ITEMS_KEY = "items";

  private List<ComboWithIconStruct> items = null;

  public ComboWithIconPropertyEditor(PropertySheet.Item property) {
    this(property, new ComboBox());
  }

  public ComboWithIconPropertyEditor(PropertySheet.Item property, ComboBox control) {
    this(property, control, false);
  }

  public ComboWithIconPropertyEditor(PropertySheet.Item property, ComboBox control, boolean readonly) {
    super(property, control, readonly);
    initList(property, control);
  }

  private void initList(PropertySheet.Item property, ComboBox control) {
    if(property instanceof CustomBeanProperty) {
      items = (List<ComboWithIconStruct>) ((CustomBeanProperty) property).getProperty(ITEMS_KEY);
      if(items == null || items.size() <= 0) {
        LOGGER.error("property list is null/empy");
      }
      else {

        List<String> itemLabels = items.stream().map(p -> p.label).collect(Collectors.toList());
        control.setItems(FXCollections.observableArrayList(itemLabels));
      }
    }
    else {
      LOGGER.warn("property is not a CustomPropertyDescriptor clazz !");
    }

    final ComboWithIconPropertyEditor _this = this;
    // create custom cell
    control.setCellFactory(list -> new ColorRectCell(_this));
  }

  @Override
  protected ObservableValue<String> getObservableValue() {
    return getEditor().getSelectionModel().selectedItemProperty();
  }

  @Override
  public void setValue(String value) {
    getEditor().getSelectionModel().select(value);
  }

  /**
   * overridden cell clazz enabling to customize Icon
   */
  private static class ColorRectCell extends ListCell<String> {

    private List<ComboWithIconStruct> availableItems = null;

    public ColorRectCell(ComboWithIconPropertyEditor editor) {
      super();
      this.availableItems = editor.items;
    }

    @Override
    public void updateItem(String item, boolean empty) {
      super.updateItem(item, empty);

      if(item != null && availableItems != null) {
        ImageView iv = null;

        for(ComboWithIconStruct ai : availableItems) {
          if(item.contains(ai.searchWork)) {
            iv = new ImageView(new Image(ai.iconPath, 16, 16, true, true));
            break;
          }
        }

        if(iv != null) {
          setGraphic(iv);
        }
      }
      setText(item);
    }
  }

  @Getter
  @Setter
  @AllArgsConstructor
  public static class ComboWithIconStruct {
    private String label;
    private String searchWork;
    private String iconPath;
  }
}
