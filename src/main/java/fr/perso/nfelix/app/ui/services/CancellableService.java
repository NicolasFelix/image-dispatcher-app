package fr.perso.nfelix.app.ui.services;

import fr.perso.nfelix.app.DispatcherConfig;
import fr.perso.nfelix.app.exception.ConfigurationException;
import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import java.io.File;
import java.util.Collection;
import java.util.ResourceBundle;
import javafx.concurrent.Service;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOCase;
import org.apache.commons.io.filefilter.FileFilterUtils;

/**
 * Cancellable Service
 *
 * @author N.FELIX
 */
public abstract class CancellableService<V> extends Service<V> {

  @Setter
  protected TaskProgressData taskData = new TaskProgressData();

  /**
   * constructor
   */
  public CancellableService() {
    super();
  }

  /**
   * construtor
   *
   * @param taskData {@link TaskProgressData}
   */
  public CancellableService(TaskProgressData taskData) {
    this();
    this.taskData = taskData;
  }

  /**
   * is opertation cancelled ? has been cancelled ?
   *
   * @return true if cancelled ?
   */
  protected boolean isOperationCancelled() {
    return taskData != null && taskData.isCancelled();
  }

  protected File findFile(final String fileName, final ResourceBundle resources)
      throws ConfigurationException {
    Collection<File> f = FileUtils
        .listFiles(new File(DispatcherConfig.getExecutionPath()), FileFilterUtils.nameFileFilter(fileName, IOCase.SENSITIVE), FileFilterUtils.trueFileFilter());
    if(CollectionUtils.isEmpty(f)) {
      throw new ConfigurationException(JavaFXUtils.getSafeResourceValue(resources, "report.template.not.found", fileName));
    }
    return f.iterator().next();
  }
}
