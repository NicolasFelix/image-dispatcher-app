package fr.perso.nfelix.app.utils.fx;

import fr.perso.nfelix.app.ui.config.editor.FolderNavigationPropertyEditor;
import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import java.io.File;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Properties;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.configuration2.INIConfiguration;
import org.apache.commons.configuration2.SubnodeConfiguration;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

/**
 * AbstractCategoryBean class
 *
 * @author N.FELIX
 */
@Slf4j
@EqualsAndHashCode(doNotUseGetters = true, exclude = { "jobConfigParameterAllowed" })
public abstract class AbstractPropertySheetBean implements IPropertySheetBean, Cloneable, Serializable {

  @Setter(AccessLevel.PROTECTED)
  @Getter
  private String category;

  protected boolean jobConfigParameterAllowed = true;

  protected transient ResourceBundle resources;

  /**
   * default hidden constructor
   */
  protected AbstractPropertySheetBean() {
    super();
  }

  /**
   * constructor
   *
   * @param category  category
   * @param resources resource
   */
  public AbstractPropertySheetBean(final String category, final ResourceBundle resources) {
    super();
    this.category = category;
    this.resources = resources;
  }

  public abstract String[] getPropertyNames();

  /**
   * get property type
   *
   * @param propName property name
   * @return property type
   */
  public Class getPropertyType(final String propName) {
    return String.class;
  }

  /**
   * job parameters to              != null
   *
   * @param props properties
   */
  public void addJobConfigParameters(Properties props) {
    // nothing
  }

  protected void addSafeConfigParameter(Properties props, final String key, final Object value) {
    if(value != null) {
      props.put(key, value.toString());
    }
  }

  @Override
  public void refresh() {
    // nada
  }

  /**
   * event when folder selection is asked for...
   * called from input {@link FolderNavigationPropertyEditor}
   */
  public void chooseFolder(ActionEvent ae, CustomBeanProperty beanProp) {

    Button sourceButton = (Button) ae.getSource();
    if(FolderNavigationPropertyEditor.CLEAN_BUTTON_ID.equals(sourceButton.getId())) {
      try {
        beanProp.getPropertyDescriptor().getWriteMethod().invoke(beanProp.getBean(), (Object) null);

        emptyFolderUI(ae, sourceButton);
      }
      catch(IllegalAccessException | InvocationTargetException e) {
        LOGGER.error("error while calling method '{}' on bean '{}' ({})", beanProp.getPropertyDescriptor().getWriteMethod().getName(),
            beanProp.getBean().getClass().getSimpleName(), e.getLocalizedMessage());
      }
    }
    else {
      DirectoryChooser directoryChooser = new DirectoryChooser();
      directoryChooser.setInitialDirectory(getInitialFolder(beanProp));

      File selectedDirectory = directoryChooser.showDialog(null);

      if(selectedDirectory != null) {
        LOGGER.debug("selected folder: '{}'", selectedDirectory.getAbsolutePath());

        String folderPath = FilenameUtils.normalizeNoEndSeparator(selectedDirectory.getAbsolutePath()) + File.separatorChar;

        try {
          beanProp.getPropertyDescriptor().getWriteMethod().invoke(beanProp.getBean(), folderPath);
        }
        catch(IllegalAccessException | InvocationTargetException e) {
          LOGGER.error("error while calling method '{}' on bean '{}' ({})", beanProp.getPropertyDescriptor().getWriteMethod().getName(),
              beanProp.getBean().getClass().getSimpleName(), e.getLocalizedMessage());
          return;
        }

        TextField tf = JavaFXUtils.getFirstNodeFromComponent(((Button) ae.getSource()).getParent(), TextField.class);
        if(tf != null) {
          tf.setText(folderPath);
        }
        Node cleanButton = JavaFXUtils.getNodeFromID(((Button) ae.getSource()).getParent(), FolderNavigationPropertyEditor.CLEAN_BUTTON_ID);
        if(cleanButton != null) {
          cleanButton.setVisible(StringUtils.isNotBlank(folderPath));
        }
      }
    }
  }

  /**
   * event when file selection is asked for...
   * called from input {@link FolderNavigationPropertyEditor}
   */
  public void chooseFile(ActionEvent ae, CustomBeanProperty beanProp) {

    Button sourceButton = (Button) ae.getSource();
    if(FolderNavigationPropertyEditor.CLEAN_BUTTON_ID.equals(sourceButton.getId())) {
      try {
        beanProp.getPropertyDescriptor().getWriteMethod().invoke(beanProp.getBean(), (Object) null);

        emptyFolderUI(ae, sourceButton);
      }
      catch(IllegalAccessException | InvocationTargetException e) {
        LOGGER.error("error while calling method '{}' on bean '{}' ({})", beanProp.getPropertyDescriptor().getWriteMethod().getName(),
            beanProp.getBean().getClass().getSimpleName(), e.getLocalizedMessage());
      }
    }
    else {

      FileChooser fileChooser = new FileChooser();
      File initialFolder = getInitialFolder(beanProp);
      fileChooser.setInitialDirectory((initialFolder != null && initialFolder.isDirectory()) ? initialFolder : null);

      fileChooser.getExtensionFilters()
          .addAll(new FileChooser.ExtensionFilter("Script groovy", "*.groovy"), new FileChooser.ExtensionFilter("Tous les fichiers", "*.*"));
      File selectFile = fileChooser.showOpenDialog(null);

      if(selectFile != null) {
        final String absolutePath = selectFile.getAbsolutePath();
        LOGGER.debug("selected files: '{}'", absolutePath);

        try {
          beanProp.getPropertyDescriptor().getWriteMethod().invoke(beanProp.getBean(), absolutePath);
        }
        catch(IllegalAccessException | InvocationTargetException e) {
          LOGGER.error("error while calling method '{}' on bean '{}' ({})", beanProp.getPropertyDescriptor().getWriteMethod().getName(),
              beanProp.getBean().getClass().getSimpleName(), e.getLocalizedMessage());
          return;
        }

        TextField tf = JavaFXUtils.getFirstNodeFromComponent(((Button) ae.getSource()).getParent(), TextField.class);
        if(tf != null) {
          tf.setText(absolutePath);
        }
        Node cleanButton = JavaFXUtils.getNodeFromID(((Button) ae.getSource()).getParent(), FolderNavigationPropertyEditor.CLEAN_BUTTON_ID);
        if(cleanButton != null) {
          cleanButton.setVisible(StringUtils.isNotBlank(absolutePath));
        }
      }
    }
  }

  private void emptyFolderUI(ActionEvent ae, Button sourceButton) {
    TextField tf = JavaFXUtils.getFirstNodeFromComponent(((Button) ae.getSource()).getParent(), TextField.class);
    if(tf != null) {
      tf.setText("");
    }
    sourceButton.setVisible(false);
  }

  private File getInitialFolder(CustomBeanProperty beanProp) {
    try {
      String initFolder = (String) beanProp.getPropertyDescriptor().getReadMethod().invoke(beanProp.getBean());
      if(StringUtils.isNotBlank(initFolder)) {
        File ret = new File(initFolder);
        if(!ret.isDirectory()) {
          ret = ret.getParentFile();
        }
        return ret;
      }
    }
    catch(IllegalAccessException | InvocationTargetException e) {
      LOGGER.error("error while calling method '{}' on bean '{}' ({})", beanProp.getPropertyDescriptor().getReadMethod().getName(),
          beanProp.getBean().getClass().getSimpleName(), e);
    }
    return null;
  }

  /**
   * read additional properties file
   *
   * @param iniFile node
   */
  public void writeAdditionalProperties(INIConfiguration iniFile) {
  }

  /**
   * read additional properties from node
   *
   * @param iniFile node
   */
  public void readAdditionalProperties(SubnodeConfiguration iniFile) {
  }

}
