package fr.perso.nfelix.app.ui.utils;

import static fr.perso.nfelix.app.ui.typedef.Constants.*;
import static fr.perso.nfelix.app.ui.utils.JavaFXUtils.getSafeResourceValue;
import static fr.perso.nfelix.app.ui.utils.JavaFXUtils.loadResourceBundle;

import fr.perso.nfelix.app.ImgDispatcherUIApp;
import fr.perso.nfelix.app.ui.controllers.IFxController;
import fr.perso.nfelix.app.ui.controllers.MainContainer;
import fr.perso.nfelix.app.utils.ApplicationHolder;
import java.io.IOException;
import java.util.HashMap;
import java.util.ResourceBundle;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.fxml.JavaFXBuilderFactory;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * FXViewManager
 *
 * @author N.FELIX
 */
@Slf4j
public class FXViewManager extends StackPane implements IRootController {

  private static final JavaFXBuilderFactory JAVAFX_BUILDER      = new JavaFXBuilderFactory();
  private static final Duration             TRANSITION_DURATION = new Duration(100);

  private HashMap<String, ViewCacheItem> screens = new HashMap<>();

  @Getter
  private ImgDispatcherUIApp mainApp = null;

  @Getter
  private MainContainer mainContainer;

  private static class ViewCacheItem {
    Node          view;
    IFxController controller;
  }

  /**
   * default constructor
   */
  public FXViewManager(ImgDispatcherUIApp app) {
    super();
    mainApp = app;
  }

  public void switchToView(final String fxmlKey)
      throws Exception {
    LOGGER.debug(">>> switchToView('{}')", fxmlKey);

    Service<Void> switchViewService = new Service<Void>() {

      @Override
      protected Task<Void> createTask() {
        return new Task<Void>() {
          @Override
          protected Void call()
              throws Exception {
            try {
              mainContainer.showMaskPane(getSafeResourceValue(mainApp.getMainResources(), "maskpane.view.loading", fxmlKey));
              if(screens.get(fxmlKey) != null) {
                displayScreen(fxmlKey);
              }
              else {
                try {
                  LOGGER.debug("loading view '{}' as it doesn't exist yet", fxmlKey);

                  final ResourceBundle resourceBundle = loadResourceBundle(fxmlKey);

                  final String viewFullPath = FXML_ROOT + fxmlKey + FXML_EXTENSION;
                  final FXMLLoader fxmlLoader = new FXMLLoader(FXViewManager.class.getResource(viewFullPath), resourceBundle, JAVAFX_BUILDER);
                  final Pane view = fxmlLoader.load();

                  IFxController controller = fxmlLoader.getController();
                  controller.setRootController(FXViewManager.this);

                  ViewCacheItem vci = new ViewCacheItem();
                  vci.view = view;
                  vci.controller = controller;
                  screens.put(fxmlKey, vci);

                  // display it
                  displayScreen(fxmlKey);
                }
                catch(Exception e) {
                  LOGGER.error("error while switching to view '" + fxmlKey + "': " + e.getLocalizedMessage(), e);
                  throw e;
                }
              }
            }
            finally {
              LOGGER.debug("<<< switchToView");
            }
            return null;
          }
        };
      }
    };

    switchViewService.start();
  }

  private void displayScreen(final String fxmlKey) {

    if(screens.get(fxmlKey) == null) {
      throw new NullPointerException("View '" + fxmlKey + "' not found in cache");
    }

    final DoubleProperty opacity = opacityProperty();

    // wait a little...
    Timeline fade = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(opacity, 1.0)), new KeyFrame(TRANSITION_DURATION, t -> {
      // change scene...
      final ViewCacheItem vci = screens.get(fxmlKey);
      mainContainer.setScene(vci.view);
      vci.controller.initUI();

      // reset style
      final ObservableList<String> stylesheets = mainApp.getAppStage().getScene().getStylesheets();
      stylesheets.clear();
      stylesheets.add(STYLES_ROOT + fxmlKey + CSS_EXTENSION);
      if(ApplicationHolder.getINSTANCE().getMainApp().getMainCss() != null) {
        stylesheets.add(ApplicationHolder.getINSTANCE().getMainApp().getMainCss().toExternalForm());
      }

      // new fade event ^^
      Timeline fadeIn = new Timeline(new KeyFrame(Duration.ZERO, new KeyValue(opacity, 0.0)), new KeyFrame(TRANSITION_DURATION, new KeyValue(opacity, 1.0)));
      fadeIn.play();
    }, new KeyValue(opacity, 0.0)));

    // hide maskPane when fade is out, not the 'switchViewService'
    fade.setOnFinished(event -> mainContainer.hideMaskPane());
    fade.play();
  }

  public boolean resetView(final String fxmlKey) {
    return (screens.remove(fxmlKey) != null);
  }

  public Scene loadMainScene(Stage stage)
      throws IOException {

    LOGGER.debug(">>> loadMainScene");
    try {
      final String viewFullPath = FXML_ROOT + FXML_MAIN + FXML_EXTENSION;
      final FXMLLoader fxmlLoader = new FXMLLoader(FXViewManager.class.getResource(viewFullPath));
      final Pane mainPane = fxmlLoader.load();

      // init main controlelr
      mainContainer = fxmlLoader.getController();

      return new Scene(mainPane);
    }
    finally {
      LOGGER.debug("<<< loadMainScene");
    }
  }
}
