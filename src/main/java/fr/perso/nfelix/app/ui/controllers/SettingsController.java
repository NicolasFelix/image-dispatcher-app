package fr.perso.nfelix.app.ui.controllers;

import static fr.perso.nfelix.app.ui.typedef.Constants.*;
import static fr.perso.nfelix.app.ui.utils.JavaFXUtils.createDialog;

import fr.perso.nfelix.app.DispatcherConfig;
import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import fr.perso.nfelix.app.utils.fx.BeanPropertyToSheetItemConverter;
import java.io.IOException;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.MenuItem;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.apache.commons.lang3.SerializationUtils;
import org.controlsfx.control.PropertySheet;

@Slf4j
public class SettingsController extends AbstractFxController {

  @FXML
  private MenuItem quitButton;
  @FXML
  private MenuItem saveButton;

  public void initUI() {

    try {
      LOGGER.debug(">>> initUI");
      DispatcherConfig config = getMainApp().getConfig();

      quitButton.setGraphic(new ImageView(new Image(EXIT_MENU_ICON, 16, 16, true, true)));
      saveButton.setGraphic(new ImageView(new Image(CHECK_MENU_ICON, 16, 16, true, true)));

      // customize propertyListItem list
      propertyListItem.setMode(PropertySheet.Mode.CATEGORY);
      propertyListItem.setModeSwitcherVisible(false);

      // add items to whole property list
      final ObservableList<PropertySheet.Item> items = propertyListItem.getItems();
      items.clear();
      config.getSubConfigs().forEach((code, bean) -> items.addAll(BeanPropertyToSheetItemConverter.getProperties(bean)));

      try {
        savedConfig = SerializationUtils.clone(config);
      }
      catch(Exception e) {
        LOGGER.warn("cloning configuration failed. Spring context will be resfreshed, even if no change was applied...", e);
      }
    }
    catch(Throwable e) {
      LOGGER.error("error while initializing SettingsController: " + e.getLocalizedMessage(), e);
    }
    finally {
      LOGGER.debug("<<< initUI");
    }
  }

  @FXML
  private PropertySheet propertyListItem;

  private DispatcherConfig savedConfig = null;

  public void saveAndQuit(ActionEvent ae) {
    doAction(true, true);
  }

  public void quitWithoutSaving(ActionEvent ae) {

    Alert dlg = createDialog(resources, Alert.AlertType.CONFIRMATION, "confirm.title", "confirm.header", "confirm.content");
    dlg.showAndWait().ifPresent(buttonType -> doAction(ButtonType.OK.equals(buttonType), false));
  }

  private void doAction(boolean goToHome, boolean save) {
    final DispatcherConfig config = getMainApp().getConfig();
    if(save) {
      try {
        // if any setting has changed, refresh spring context in order to inject (again) properties
        if(!config.equals(savedConfig)) {
          LOGGER.info("spring context is refreshed...");
          // force refresh just with Birt profile...
        }

        config.save();
        config.getGlobalConfig().resetLogLevel();
      }
      catch(IOException | ConfigurationException e) {
        Alert dialog = JavaFXUtils
            .createDialog(getMainApp().getMainResources(), Alert.AlertType.ERROR, "error.global.title", "error.save.ini.header", "error.save.ini.content", null,
                null, new Object[] { e.getLocalizedMessage() }, e);
        dialog.showAndWait();
      }
    }
    else {
      // in order to cancel local changes
      config.load();
    }

    if(goToHome) {
      switchToView(FXML_HOME);
    }
  }

  @Override
  public void enableOrDisableUI(boolean enable) {
  }
}
