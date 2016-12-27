package fr.perso.nfelix.app.ui.config.editor;

import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import lombok.extern.slf4j.Slf4j;
import org.controlsfx.control.PropertySheet;

/**
 * Custom property editor enabling to select a date from picker
 *
 * @author N.FELIX
 */
@Slf4j
public class SpinnerPropertyEditor extends CustomAbstractPropertyEditor<Integer, HBox> {

  public SpinnerPropertyEditor(PropertySheet.Item property) {
    this(property, buildBox(property));
  }

  private static HBox buildBox(PropertySheet.Item property) {
    final HBox hBox = new HBox();
    final Spinner spinner = new Spinner();
    // Value factory.
    SpinnerValueFactory<Integer> valueFactory = //
        new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10, (Integer) property.getValue());

    spinner.setValueFactory(valueFactory);

    final ObservableList<Node> children = hBox.getChildren();
    children.add(spinner);
    return hBox;
  }

  public SpinnerPropertyEditor(PropertySheet.Item property, HBox control) {
    this(property, control, true);
  }

  public SpinnerPropertyEditor(PropertySheet.Item property, HBox control, boolean readonly) {
    super(property, control, readonly);
  }

  @Override
  protected ObservableValue<Integer> getObservableValue() {
    return null;
  }

  @Override
  public void setValue(Integer value) {
    final Spinner spinner = JavaFXUtils.getFirstNodeFromComponent(getEditor(), Spinner.class);
    if(spinner != null) {
      spinner.getValueFactory().setValue(value);
    }
  }
}
