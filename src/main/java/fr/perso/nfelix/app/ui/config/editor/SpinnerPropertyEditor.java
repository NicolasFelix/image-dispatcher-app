package fr.perso.nfelix.app.ui.config.editor;

import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import fr.perso.nfelix.app.utils.fx.CustomBeanProperty;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.util.StringConverter;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.PropertySheet;

/**
 * Custom property editor enabling to select a date from picker
 *
 * @author N.FELIX
 */
@Slf4j
public class SpinnerPropertyEditor extends CustomAbstractPropertyEditor<Integer, HBox> {

  /** separator */
  public static final String RANGE_KEY = "range";

  public SpinnerPropertyEditor(PropertySheet.Item property) {
    this(property, buildBox(property));
  }

  @SuppressWarnings("unchecked")
  private static HBox buildBox(PropertySheet.Item property) {
    final HBox hBox = new HBox();

    int minValue = 1;
    int maxValue = 10;
    List<Integer> range = (List<Integer>) ((CustomBeanProperty) property).getProperty(RANGE_KEY);
    if(range != null && range.size() == 2) {
      minValue = range.get(0);
      maxValue = range.get(1);
      if(maxValue < minValue) {
        // swap them
        minValue = range.get(1);
        maxValue = range.get(0);
      }
    }

    SpinnerAutoCommit<Integer> spinner = new SpinnerAutoCommit<>(minValue, maxValue, (Integer) property.getValue());
    spinner.setEditable(true);
    spinner.setPrefWidth(80.0);

    // Value factory.
    // SpinnerValueFactory<Integer> valueFactory = //
    //     new SpinnerValueFactory.IntegerSpinnerValueFactory(minValue, maxValue, (Integer) property.getValue());
    //
    // spinner.setValueFactory(valueFactory);

    final ObservableList<Node> children = hBox.getChildren();
    children.add(spinner);
    return hBox;
    // return spinner;
  }

  public SpinnerPropertyEditor(PropertySheet.Item property, HBox control) {
    this(property, control, true);
  }

  public SpinnerPropertyEditor(PropertySheet.Item property, HBox control, boolean readonly) {
    super(property, control, readonly);
  }

  @SuppressWarnings("unchecked")
  @Override
  protected ObservableValue<Integer> getObservableValue() {
    final Spinner<Integer> spinner = JavaFXUtils.getFirstNodeFromComponent(getEditor(), SpinnerAutoCommit.class);
    if(spinner != null) {
      return spinner.valueProperty();
      // return new SimpleObjectProperty(spinner.getValue());
    }
    return null;
  }

  // @SuppressWarnings("unchecked")
  // @Override
  // public Integer getValue() {
  //   final Spinner<Integer> spinner = JavaFXUtils.getFirstNodeFromComponent(getEditor(), SpinnerAutoCommit.class);
  //   if(spinner != null) {
  //     return spinner.getValue();
  //   }
  //   return null;
  // }

  // @Override
  // public Spinner<Integer> getEditor() {
  //   return spinner;
  // }

  @SuppressWarnings("unchecked")
  @Override
  public void setValue(Integer value) {
    final Spinner spinner = JavaFXUtils.getFirstNodeFromComponent(getEditor(), SpinnerAutoCommit.class);
    if(spinner != null) {
      spinner.getValueFactory().setValue(value);
    }
  }

  private static class SpinnerAutoCommit<T> extends Spinner<T> {

    public SpinnerAutoCommit() {
      super();
      addListenerKeyChange();
    }

    public SpinnerAutoCommit(int min, int max, int initialValue) {
      super(min, max, initialValue);
      addListenerKeyChange();
    }

    public SpinnerAutoCommit(int min, int max, int initialValue, int amountToStepBy) {
      super(min, max, initialValue, amountToStepBy);
      addListenerKeyChange();
    }

    public SpinnerAutoCommit(double min, double max, double initialValue) {
      super(min, max, initialValue);
      addListenerKeyChange();
    }

    public SpinnerAutoCommit(double min, double max, double initialValue, double amountToStepBy) {
      super(min, max, initialValue, amountToStepBy);
      addListenerKeyChange();
    }

    public SpinnerAutoCommit(ObservableList<T> items) {
      super(items);
      addListenerKeyChange();
    }

    public SpinnerAutoCommit(SpinnerValueFactory<T> valueFactory) {
      super(valueFactory);
      addListenerKeyChange();
    }

    private void addListenerKeyChange() {
      getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
        commitEditorText();
      });
    }

    private void commitEditorText() {
      if(!isEditable()) {
        return;
      }
      String text = getEditor().getText();
      SpinnerValueFactory<T> valueFactory = getValueFactory();
      if(valueFactory != null) {
        StringConverter<T> converter = valueFactory.getConverter();
        if(converter != null) {
          T value = converter.fromString(text);
          valueFactory.setValue(value);
        }
      }
    }

  }
}
