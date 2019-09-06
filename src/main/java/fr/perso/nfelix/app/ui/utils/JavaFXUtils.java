package fr.perso.nfelix.app.ui.utils;

import fr.perso.nfelix.app.ui.typedef.Constants;
import fr.perso.nfelix.app.utils.ApplicationHolder;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Dialog;
import javafx.scene.control.TextArea;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.web.WebView;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.controlsfx.control.Notifications;

/**
 * JavaFXUtils clazz
 *
 * @author N.FELIX
 */
@Slf4j
public abstract class JavaFXUtils {

  /**
   * create dialog
   *
   * @param type       dialog type
   * @param titleKey   tile key
   * @param headerKey  header key
   * @param contentKey content key
   * @return {@link Alert}
   */
  public static Alert createDialog(Alert.AlertType type, final String titleKey, final String headerKey, final String contentKey) {
    return createDialog(null, type, titleKey, headerKey, contentKey);
  }

  /**
   * create dialog
   *
   * @param resources  resource bundle
   * @param type       dialog type
   * @param titleKey   tile key
   * @param headerKey  header key
   * @param contentKey content key
   * @return {@link Alert}
   */
  public static Alert createDialog(final ResourceBundle resources, Alert.AlertType type, final String titleKey, final String headerKey,
                                   final String contentKey) {
    return createDialog(resources, type, titleKey, headerKey, contentKey, null, null, null, null);
  }

  /**
   * create dialog
   *
   * @param resources   resource bundle
   * @param type        dialog type
   * @param titleKey    title key
   * @param headerKey   header key
   * @param contentKey  content key
   * @param argsTitle   title message arguments
   * @param argsHeader  header message arguments
   * @param argsContent content message arguments
   * @param th          exception
   * @return {@link Alert}
   */
  public static Alert createDialog(final ResourceBundle resources, Alert.AlertType type, final String titleKey, final String headerKey, final String contentKey,
                                   Object[] argsTitle, Object[] argsHeader, Object[] argsContent, Throwable th) {

    String titleMsg = StringUtils.isNotBlank(titleKey) ? getSafeResourceValue(resources, titleKey, argsTitle) : null;
    String headerMsg = StringUtils.isNotBlank(headerKey) ? getSafeResourceValue(resources, headerKey, argsHeader) : null;
    String contentMsg = StringUtils.isNotBlank(contentKey) ? getSafeResourceValue(resources, contentKey, argsContent) : null;
    return createDialog(type, titleMsg, headerMsg, contentMsg, th);
  }

  /**
   * create dialog
   *
   * @param type       dialog type
   * @param titleMsg   title message
   * @param headerMsg  header message
   * @param contentMsg conentn message
   * @param th         exception
   * @return {@link Alert}
   */
  public static Alert createDialog(Alert.AlertType type, final String titleMsg, final String headerMsg, final String contentMsg, Throwable th) {
    Alert dlg = new Alert(type);
    buildDialog(dlg, titleMsg, headerMsg, contentMsg, th);
    return dlg;
  }

  private static void buildDialog(Dialog dlg, String titleMsg, String headerMsg, String contentMsg, Throwable th) {

    if(StringUtils.isNotBlank(titleMsg)) {
      dlg.setTitle(titleMsg);
    }
    final DialogPane dialogPane = dlg.getDialogPane();
    if(ApplicationHolder.getINSTANCE().getMainApp().getMainCss() != null) {
      final ObservableList<String> stylesheets = dialogPane.getScene().getStylesheets();
      stylesheets.clear();
      stylesheets.add(ApplicationHolder.getINSTANCE().getMainApp().getMainCss().toExternalForm());
    }

    if(StringUtils.isNotBlank(headerMsg)) {
      dialogPane.setHeaderText(headerMsg);
    }
    if(StringUtils.isNotBlank(contentMsg)) {
      if(contentMsg.startsWith("<html>")) {
        WebView webView = new WebView();
        webView.getEngine().loadContent(contentMsg);
        webView.setPrefSize(150, 200);
        dialogPane.setContent(webView);
      }
      else {
        dialogPane.setContentText(contentMsg);
      }
    }

    if(th != null) {
      StringWriter sw = new StringWriter();
      PrintWriter pw = new PrintWriter(sw);
      th.printStackTrace(pw);
      String exceptionText = sw.toString();

      TextArea textArea = new TextArea(exceptionText);
      textArea.setEditable(false);
      textArea.setWrapText(true);

      textArea.setMaxWidth(Double.MAX_VALUE);
      textArea.setMaxHeight(Double.MAX_VALUE);
      GridPane.setVgrow(textArea, Priority.ALWAYS);
      GridPane.setHgrow(textArea, Priority.ALWAYS);

      final Node originalContent = dialogPane.getExpandableContent();

      GridPane root = new GridPane();
      root.setMaxWidth(Double.MAX_VALUE);
      if(originalContent != null) {
        root.add(originalContent, 0, 0);
      }
      root.add(textArea, 0, 1);

      dialogPane.setExpandableContent(root);
    }

    dlg.initOwner(null);
  }

  /**
   * createPasswordDialog
   *
   * @param resources  resource bundle
   * @param titleKey   tile key
   * @param headerKey  header key
   * @param contentKey content key
   * @return {@link Alert}
   */
  public static PasswordInputDialog createPasswordDialog(final ResourceBundle resources, final String titleKey, final String headerKey,
                                                         final String contentKey) {

    return (PasswordInputDialog) createDialog(new PasswordInputDialog(), resources, titleKey, headerKey, contentKey);
  }

  /**
   * createInputTextDialog
   *
   * @param resources  resource bundle
   * @param titleKey   tile key
   * @param headerKey  header key
   * @param contentKey content key
   * @return {@link Alert}
   */
  public static TextInputDialog createInputTextDialog(final ResourceBundle resources, final String titleKey, final String headerKey, final String contentKey) {

    return (TextInputDialog) createDialog(new TextInputDialog(), resources, titleKey, headerKey, contentKey);
  }

  private static Dialog createDialog(Dialog dlg, final ResourceBundle resources, final String titleKey, final String headerKey, final String contentKey) {
    String titleMsg = StringUtils.isNotBlank(titleKey) ? getSafeResourceValue(resources, titleKey) : null;
    String headerMsg = StringUtils.isNotBlank(headerKey) ? getSafeResourceValue(resources, headerKey) : null;
    String contentMsg = StringUtils.isNotBlank(contentKey) ? getSafeResourceValue(resources, contentKey) : null;

    buildDialog(dlg, titleMsg, headerMsg, contentMsg, null);
    return dlg;
  }

  public static String getSafeResourceValue(final ResourceBundle resources, final String key, Object... arguments) {
    if(resources == null) {
      LOGGER.warn("resourceBundle is undefined");
      return key;
    }

    String keyValue;
    try {
      keyValue = resources.getString(key);
    }
    catch(MissingResourceException mre) {
      return ">>> resource '" + key + "' not found <<<";
    }
    // ok format....
    return MessageFormat.format(keyValue, arguments);
  }

  /**
   * load resource bundle
   *
   * @param fxmlKey fxml key
   * @return Resource Bundle, if any
   */
  public static ResourceBundle loadResourceBundle(String fxmlKey) {
    LOGGER.debug(">>> loadResourceBundle('{}')", fxmlKey);
    ResourceBundle bundle = null;
    try {
      bundle = ResourceBundle.getBundle(Constants.I18N_ROOT + fxmlKey);
    }
    catch(MissingResourceException mre) {
      LOGGER.warn("Resource '{}' not found", fxmlKey);
    }
    finally {
      LOGGER.debug("<<< loadResourceBundle");
    }
    return bundle;
  }

  /**
   * get first child item of expected class type from a component
   *
   * @param startComponent component to look from
   * @param expectedClass  look up class
   * @param <T>            look up class
   * @return first component found (if any)
   */
  @SuppressWarnings("unchecked")
  public static <T> T getFirstNodeFromComponent(final Parent startComponent, Class<T> expectedClass) {
    T res = null;
    if(startComponent != null) {
      for(Node node : startComponent.getChildrenUnmodifiable()) {
        if(node != null && expectedClass.equals(node.getClass())) {
          res = (T) node;
          break;
        }

        if(node instanceof Parent) {
          res = getFirstNodeFromComponent((Parent) node, expectedClass);
        }

        if(res != null) {
          break;
        }
      }
    }
    return res;
  }

  /**
   * get first child item of expected ID
   *
   * @param startComponent component to look from
   * @param componentID    look up class
   * @return first component found (if any)
   */
  public static Node getNodeFromID(final Parent startComponent, String componentID) {
    Node res = null;
    if(startComponent != null) {
      for(Node node : startComponent.getChildrenUnmodifiable()) {
        if(componentID.equalsIgnoreCase(node.getId())) {
          res = node;
          break;
        }

        if(node instanceof Parent) {
          res = getNodeFromID((Parent) node, componentID);
        }

        if(res != null) {
          break;
        }
      }
    }
    return res;
  }

  /**
   * display desktop notification
   *
   * @param type       type of notification ({@link javafx.scene.control.Alert.AlertType}
   * @param titleMsg   title
   * @param contentMsg message
   */
  public static void displayNotification(Alert.AlertType type, final String titleMsg, final String contentMsg) {
    Notifications notificationBuilder = Notifications.create().title(titleMsg).text(contentMsg).hideAfter(Duration.seconds(5.0)).position(Pos.BOTTOM_RIGHT);

    switch( type ) {
      case INFORMATION:
        notificationBuilder.showInformation();
        break;
      case ERROR:
        notificationBuilder.showError();
        break;
      case WARNING:
      default:
        notificationBuilder.showWarning();
        break;
    }
  }

  /**
   * make the file be opened by the OS
   *
   * @param fileToOpen file to be opened
   */
  public static void openFile(File fileToOpen) {
    if(fileToOpen != null && Desktop.isDesktopSupported()) {
      Desktop desktop = Desktop.getDesktop();
      if(desktop.isSupported(Desktop.Action.OPEN)) {
        try {
          desktop.open(fileToOpen);
        }
        catch(IOException ioe) {
          LOGGER.error("error while opening file: " + fileToOpen.getAbsolutePath(), ioe);
        }
      }
    }
  }
}
