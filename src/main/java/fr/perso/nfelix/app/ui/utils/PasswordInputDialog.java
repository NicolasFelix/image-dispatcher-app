package fr.perso.nfelix.app.ui.utils;

import com.sun.javafx.scene.control.skin.resources.ControlResources;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.StringUtils;

/**
 * PasswordInputDialog
 */
public class PasswordInputDialog extends Dialog<String> {

  private final GridPane      grid;
  private final Label         label;
  private final PasswordField passwordField;

  /**
   * Creates a new PasswordInputDialog with the default value entered into the
   * dialog {@link PasswordField}.
   */
  public PasswordInputDialog() {
    final DialogPane dialogPane = getDialogPane();

    // -- textfield
    this.passwordField = new PasswordField();
    this.passwordField.setMaxWidth(Double.MAX_VALUE);
    GridPane.setHgrow(passwordField, Priority.ALWAYS);
    GridPane.setFillWidth(passwordField, true);

    // -- label
    label = createContentLabel(dialogPane.getContentText());
    label.setPrefWidth(Region.USE_COMPUTED_SIZE);
    label.textProperty().bind(dialogPane.contentTextProperty());

    this.grid = new GridPane();
    this.grid.setHgap(10);
    this.grid.setMaxWidth(Double.MAX_VALUE);
    this.grid.setAlignment(Pos.CENTER_LEFT);

    dialogPane.contentTextProperty().addListener(o -> updateGrid());

    setTitle(ControlResources.getString("Dialog.confirm.title"));
    dialogPane.setHeaderText(ControlResources.getString("Dialog.confirm.header"));
    dialogPane.getStyleClass().add("text-input-dialog");
    dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
    getDialogPane().lookupButton(ButtonType.OK).setDisable(true);

    updateGrid();

    setResultConverter((dialogButton) -> {
      ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
      return data == ButtonBar.ButtonData.OK_DONE ? passwordField.getText() : null;
    });
  }

  /**
   * Returns the {@link TextField} used within this dialog.
   */
  public final TextField getEditor() {
    return passwordField;
  }

  private void updateGrid() {
    grid.getChildren().clear();

    grid.add(label, 0, 0);
    grid.add(passwordField, 1, 0);
    getDialogPane().setContent(grid);

    passwordField.textProperty()
        .addListener((observable, oldValue, newValue) -> getDialogPane().lookupButton(ButtonType.OK).setDisable(StringUtils.isBlank(newValue)));

    Platform.runLater(passwordField::requestFocus);
  }

  private static Label createContentLabel(String text) {
    Label label = new Label(text);
    label.setMaxWidth(Double.MAX_VALUE);
    label.setMaxHeight(Double.MAX_VALUE);
    label.getStyleClass().add("content");
    label.setWrapText(true);
    label.setPrefWidth(360);
    return label;
  }
}
