package fr.perso.nfelix.app.ui.config.editor;

import java.util.Arrays;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.util.Callback;
import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.CheckComboBox;
import org.controlsfx.control.IndexedCheckModel;

/**
 * <pre>
 * TableView column editable and hosting a {@link CheckComboBox}.
 * This is inspired from {@link javafx.scene.control.cell.ComboBoxTableCell}.
 * Note: this class only deals with String data/values
 * </pre>
 *
 * @param <S> The type of the TableView
 * @author N.FELIX
 */
@Slf4j
public class CheckComboBoxTableCell<S> extends TableCell<S, String> {

  /** separator */
  public static final String ITEM_SEP = ",";

  // --- converter
  private ObjectProperty<StringConverter<String>> converter = new SimpleObjectProperty<>(this, "converter");

  private final ObservableList<String> items;
  private       CheckComboBox<String>  ckComboBox;

  /**
   * Creates a default ComboBoxTableCell with an empty items list.
   */
  public CheckComboBoxTableCell() {
    this(FXCollections.observableArrayList());
  }

  /**
   * Creates a default {@link CheckComboBoxTableCell} instance with the given items
   * being used to populate the {@link ComboBox} when it is shown.
   *
   * @param items The items to show in the ComboBox popup menu when selected
   *              by the user.
   */
  public CheckComboBoxTableCell(String... items) {
    this(FXCollections.observableArrayList(items));
  }

  /**
   * Creates a {@link CheckComboBoxTableCell} instance with the given items
   * being used to populate the {@link ComboBox} when it is shown, and the
   * {@link StringConverter} being used to convert the item in to a
   * user-readable form.
   *
   * @param converter A {@link StringConverter} that can convert an item of type T
   *                  into a user-readable string so that it may then be shown in the
   *                  ComboBox popup menu.
   * @param items     The items to show in the ComboBox popup menu when selected
   *                  by the user.
   */
  public CheckComboBoxTableCell(StringConverter<String> converter, String... items) {
    this(converter, FXCollections.observableArrayList(items));
  }

  /**
   * Creates a default {@link CheckComboBoxTableCell} instance with the given items
   * being used to populate the {@link ComboBox} when it is shown.
   *
   * @param items The items to show in the ComboBox popup menu when selected
   *              by the user.
   */
  public CheckComboBoxTableCell(ObservableList<String> items) {
    this(null, items);
  }

  /**
   * Creates a {@link CheckComboBoxTableCell} instance with the given items
   * being used to populate the {@link ComboBox} when it is shown, and the
   * {@link StringConverter} being used to convert the item in to a
   * user-readable form.
   *
   * @param converter A {@link StringConverter} that can convert an item of type T
   *                  into a user-readable string so that it may then be shown in the
   *                  ComboBox popup menu.
   * @param items     The items to show in the ComboBox popup menu when selected
   *                  by the user.
   */
  public CheckComboBoxTableCell(StringConverter<String> converter, ObservableList<String> items) {
    this.getStyleClass().add("combo-box-table-cell");
    this.items = items;
    setConverter(converter != null ? converter : new DefaultStringConverter());
  }

  /**
   * The {@link StringConverter} property.
   */
  public final ObjectProperty<StringConverter<String>> converterProperty() {
    return converter;
  }

  /**
   * Sets the {@link StringConverter} to be used in this cell.
   */
  public final void setConverter(StringConverter<String> value) {
    converterProperty().set(value);
  }

  /**
   * Returns the {@link StringConverter} used in this cell.
   */
  public final StringConverter<String> getConverter() {
    return converterProperty().get();
  }

  /**
   * Returns the items to be displayed in the ChoiceBox when it is showing.
   */
  public ObservableList<String> getItems() {
    return items;
  }

  @Override
  public void startEdit() {
    if(!isEditable() || !getTableView().isEditable() || !getTableColumn().isEditable()) {
      return;
    }

    if(ckComboBox == null) {
      ckComboBox = createCheckComboBox(this, items, converterProperty());
    }

    // initialize checked items
    final IndexedCheckModel<String> ckModel = ckComboBox.getCheckModel();
    if(StringUtils.isNotBlank(this.getItem())) {
      final String[] split = this.getItem().split(ITEM_SEP);
      Arrays.stream(split).forEach(ckModel::check);
    }

    super.startEdit();
    setText(null);
    setGraphic(ckComboBox);
  }

  @Override
  public void cancelEdit() {
    super.cancelEdit();

    setText(getConverter().toString(getItem()));
    setGraphic(null);
  }

  @Override
  public void updateItem(String item, boolean empty) {
    super.updateItem(item, empty);
    updateItem(this, getConverter(), null, null, ckComboBox);
  }

  private void updateItem(final Cell<String> cell, final StringConverter<String> converter, final HBox hbox, final Node graphic,
                          final CheckComboBox<String> comboBox) {
    if(cell.isEmpty()) {
      cell.setText(null);
      cell.setGraphic(null);
    }
    else {
      if(cell.isEditing()) {
        if(comboBox != null) {
          final String values = cell.getItem();
          comboBox.getCheckModel().check(values);
        }
        cell.setText(null);

        if(graphic != null) {
          hbox.getChildren().setAll(graphic, comboBox);
          cell.setGraphic(hbox);
        }
        else {
          cell.setGraphic(comboBox);
        }
      }
      else {
        cell.setText(getItemText(cell, converter));
        cell.setGraphic(graphic);
      }
    }
  }

  private CheckComboBox<String> createCheckComboBox(final Cell<String> cell, final ObservableList<String> items,
                                                    final ObjectProperty<StringConverter<String>> converter) {
    CheckComboBox<String> comboBox = new CheckComboBox<>(items);
    comboBox.converterProperty().bind(converter);
    comboBox.setMaxWidth(Double.MAX_VALUE);
    comboBox.getCheckModel().getCheckedItems().addListener((ListChangeListener<? super String>) c -> {
      // LOGGER.info("selection changed : '{}'", c);
      String value = String.join(ITEM_SEP, comboBox.getCheckModel().getCheckedItems());

      cell.commitEdit(value);
    });
    return comboBox;
  }

  private String getItemText(Cell<String> cell, StringConverter<String> converter) {
    return converter == null ? cell.getItem() == null ? "" : cell.getItem() : converter.toString(cell.getItem());
  }

  /**
   * Creates a ComboBox cell factory for use in {@link TableColumn} controls.
   * By default, the ComboBoxCell is rendered as a {@link Label} when not
   * being edited, and as a ComboBox when in editing mode. The ComboBox will,
   * by default, stretch to fill the entire list cell.
   *
   * @param items Zero or more items that will be shown to the user when the
   *              {@link ComboBox} menu is showing. These items must be of the same
   *              type as the TableColumn. Note that it is up to the developer to set
   *              {@link EventHandler event handlers} to listen to edit events in the
   *              TableColumn, and react accordingly. Methods of interest include
   *              {@link TableColumn#setOnEditStart(EventHandler) setOnEditStart},
   *              {@link TableColumn#setOnEditCommit(EventHandler) setOnEditCommit},
   *              and {@link TableColumn#setOnEditCancel(EventHandler) setOnEditCancel}.
   * @return A {@link Callback} that will return a TableCell that is able to
   * work on the type of element contained within the TableColumn.
   */
  public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn(final ObservableList<String> items) {
    return forTableColumn(null, items);
  }

  /**
   * Creates a ComboBox cell factory for use in {@link TableColumn} controls.
   * By default, the ComboBoxCell is rendered as a {@link Label} when not
   * being edited, and as a ComboBox when in editing mode. The ComboBox will,
   * by default, stretch to fill the entire list cell.
   *
   * @param converter A {@link StringConverter} to convert the given item (of
   *                  type T) to a String for displaying to the user.
   * @param items     Zero or more items that will be shown to the user when the
   *                  {@link ComboBox} menu is showing. These items must be of the same
   *                  type as the TableColumn. Note that it is up to the developer to set
   *                  {@link EventHandler event handlers} to listen to edit events in the
   *                  TableColumn, and react accordingly. Methods of interest include
   *                  {@link TableColumn#setOnEditStart(EventHandler) setOnEditStart},
   *                  {@link TableColumn#setOnEditCommit(EventHandler) setOnEditCommit},
   *                  and {@link TableColumn#setOnEditCancel(EventHandler) setOnEditCancel}.
   * @return A {@link Callback} that will return a TableCell that is able to
   * work on the type of element contained within the TableColumn.
   */
  public static <S> Callback<TableColumn<S, String>, TableCell<S, String>> forTableColumn(final StringConverter<String> converter,
                                                                                          final ObservableList<String> items) {
    return list -> new CheckComboBoxTableCell<>(converter, items);
  }
}
