package fr.perso.nfelix.app.ui.controllers;

import javafx.application.Platform;
import javafx.collections.ObservableMap;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.controlsfx.control.MaskerPane;

/**
 * MainController  class for the entire layout, enabling inner scene to follow layout
 *
 * @author N.FELIX
 */
public class MainContainer extends AbstractFxController {

  /** Holder of a switchable vista. */
  @FXML
  private StackPane sceneHolder;
  @FXML
  private VBox      vboxContainer;

  private MaskerPane mainMaskPane = new MaskerPane();

  /**
   * Replaces the vista displayed in the vista holder with a new vista.
   *
   * @param node the vista node to be swapped in.
   */
  public void setScene(Node node) {
    clearAccelerators();
    sceneHolder.getChildren().clear();
    sceneHolder.getChildren().addAll(node, mainMaskPane);
    StackPane.setAlignment(node, Pos.TOP_LEFT);
  }

  private void clearAccelerators() {
    final Scene scene = sceneHolder.getScene();
    if(scene != null) {
      final ObservableMap<KeyCombination, Runnable> accelerators = scene.getAccelerators();
      if(accelerators != null) {
        accelerators.clear();
      }
    }
  }

  /**
   * show mask pane with specific text
   *
   * @param text text to display
   */
  public void showMaskPane(final String text) {
    Platform.runLater(() -> {
      mainMaskPane.setText(text);
      mainMaskPane.setVisible(true);
    });
  }

  /**
   * hide mask pane
   */
  public void hideMaskPane() {
    Platform.runLater(() -> mainMaskPane.setVisible(false));
  }
}
