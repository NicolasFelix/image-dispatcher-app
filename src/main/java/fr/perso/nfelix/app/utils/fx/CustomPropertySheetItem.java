package fr.perso.nfelix.app.utils.fx;

import java.util.Optional;
import javafx.beans.value.ObservableValue;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.PropertySheet;

/**
 * @author N.FELIX
 */
@Getter
@Setter
public class CustomPropertySheetItem implements PropertySheet.Item {

  private String   category;
  private String   label;
  private Object   value;
  private Class<?> type;

  private boolean editable = true;

  public CustomPropertySheetItem(final String label, final Class<?> type) {
    super();
    setLabel(label);
    setType(type);
  }

  public CustomPropertySheetItem(final String category, final String label, final Object value, final Class<?> type) {
    super();
    setCategory(category);
    setLabel(label);
    setValue(value);
    setType(type);
  }

  @Override
  public Class<?> getType() {
    return (type != null ? type : (value != null ? value.getClass() : null));
  }

  @Override
  public String getCategory() {
    return category;
  }

  @Override
  public String getName() {
    return label;
  }

  @Override
  public String getDescription() {
    return null;
  }

  @Override
  public Object getValue() {
    return value;
  }

  @Override
  public void setValue(Object value) {
    this.value = value;
  }

  @Override
  public Optional<ObservableValue<?>> getObservableValue() {
    return Optional.empty();
  }
}
