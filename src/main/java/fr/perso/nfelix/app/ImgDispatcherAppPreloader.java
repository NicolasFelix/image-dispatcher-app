package fr.perso.nfelix.app;

import fr.perso.nfelix.app.ui.typedef.Constants;
import java.awt.*;
import javafx.application.Preloader;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * {@link javafx.application.Preloader}
 *
 * @author N.FELIX
 */
@Slf4j
public class ImgDispatcherAppPreloader extends Preloader {

  private Stage preloaderStage;

  private Label progressText;

  @Override
  public void start(Stage primaryStage)
      throws Exception {
    this.preloaderStage = primaryStage;

    ImageView splash = new ImageView(new Image(Constants.SPLASH_IMAGE));
    progressText = new Label("Démarrage de l'application...");
    Pane splashLayout = new VBox();
    splashLayout.getChildren().addAll(splash, progressText);
    progressText.setAlignment(Pos.CENTER);
    splashLayout.setStyle(
        "-fx-padding: 5; " + "-fx-background-color: cornsilk; " + "-fx-border-width:5; " + "-fx-border-color: " + "linear-gradient(" + "to bottom, " + "chocolate, " + "derive(chocolate, 50%)" + ");");
    splashLayout.setEffect(new DropShadow());

    Rectangle2D bounds = Screen.getPrimary().getBounds();
    try {
      Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
      bounds = new Rectangle2D(0, 0, dim.getWidth(), dim.getHeight());
    }
    catch(HeadlessException ignored) {
    }
    Scene splashScene = new Scene(splashLayout, Color.TRANSPARENT);
    preloaderStage.setScene(splashScene);
    preloaderStage.setX(bounds.getMinX() + bounds.getWidth() / 2 - 300);    // 300 = splash width / 2
    preloaderStage.setY(bounds.getMinY() + bounds.getHeight() / 2 - 100);   // 100 = splash height / 2
    preloaderStage.initStyle(StageStyle.TRANSPARENT);
    preloaderStage.setAlwaysOnTop(true);
    preloaderStage.show();
  }

  @Override
  public void handleApplicationNotification(PreloaderNotification info) {
    if(info instanceof ExProgressNotification) {
      progressText.setText(((ExProgressNotification) info).getMsg());
    }
    else if(info instanceof HideNotification) {
      preloaderStage.hide();
    }
    else {
      LOGGER.warn("class {} not managed", info.getClass().getSimpleName());
    }
  }

  @Getter
  public static class ExProgressNotification extends ProgressNotification {
    private String msg;

    public ExProgressNotification(final String msg) {
      super(0.0);
      this.msg = msg;
    }
  }

  public static class HideNotification implements PreloaderNotification {
  }
}
