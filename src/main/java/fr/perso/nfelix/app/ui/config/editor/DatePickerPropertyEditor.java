package fr.perso.nfelix.app.ui.config.editor;

import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import fr.perso.nfelix.app.utils.sgbd.DalConstants;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.DatePicker;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.PropertySheet;

/**
 * Custom property editor enabling to select a date from picker
 *
 * @author N.FELIX
 */
@Slf4j
public class DatePickerPropertyEditor extends CustomAbstractPropertyEditor<String, HBox> {

  private final static String            DATE_PATTERN = DalConstants.DISPLAY_PATTERN_DATE;
  private final static DateTimeFormatter DT_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN).withLocale(Locale.FRENCH);

  public DatePickerPropertyEditor(PropertySheet.Item property) {
    this(property, buildBox(property));
  }

  private static HBox buildBox(PropertySheet.Item property) {
    final HBox hBox = new HBox();
    final DatePicker dp = new DatePicker();
    final ObservableList<Node> children = hBox.getChildren();
    dp.setMinWidth(200);
    dp.setShowWeekNumbers(false);
    dp.setPromptText(DATE_PATTERN.toLowerCase());
    dp.setConverter(new StringConverter<LocalDate>() {
      @Override
      public String toString(LocalDate inDate) {
        try {
          return ((inDate != null) ? DT_FORMATTER.format(inDate) : "");
        }
        catch(Exception e) {
          LOGGER.error("error while converting inDate to string (date: '" + inDate + "') : " + e.getLocalizedMessage(), e);
        }
        return "";
      }

      @Override
      public LocalDate fromString(String value) {
        try {
          return (StringUtils.isNotBlank(value) ? LocalDate.parse(value, DT_FORMATTER) : null);
        }
        catch(Exception e) {
          LOGGER.error("error while converting date (from string '" + value + "') : " + e.getLocalizedMessage(), e);
        }
        return null;
      }
    });
    dp.valueProperty().addListener((observable, oldValue, newValue) -> property.setValue(dp.getConverter().toString(newValue)));

    children.add(dp);
    return hBox;
  }

  public DatePickerPropertyEditor(PropertySheet.Item property, HBox control) {
    this(property, control, true);
  }

  public DatePickerPropertyEditor(PropertySheet.Item property, HBox control, boolean readonly) {
    super(property, control, readonly);
  }

  @Override
  protected ObservableValue<String> getObservableValue() {
    return null;
  }

  @Override
  public void setValue(String value) {
    final DatePicker dp = JavaFXUtils.getFirstNodeFromComponent(getEditor(), DatePicker.class);
    if(dp != null) {
      dp.setValue(dp.getConverter().fromString(value));
    }
  }
}
