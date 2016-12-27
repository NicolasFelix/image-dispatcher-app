package fr.perso.nfelix.app.utils;

import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File utilities.
 *
 * @author n.felix
 * @version %I%, %G% %U%
 */
public final class DFileUtils {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DFileUtils.class);

  /**
   * delete file or directory (quietly, no exception thrown)
   *
   * @param file the file to delete
   * @return true if delete has been succssfully done
   * @see FileUtils#forceDelete(File)
   */
  public static boolean forceDeleteQuietly(File file) {

    try {
      if(file != null) {
        FileUtils.forceDelete(file);
      }
      return true;
    }
    catch(IOException e) {
      return false;
    }
  }

  /**
   * Makes a directory, including any necessary but nonexistent parent directories
   *
   * @param file the file to delete
   * @see FileUtils#forceMkdir(File)
   */
  public static void forceMkdirQuietly(File file) {

    try {
      if(file != null) {
        FileUtils.forceMkdir(file);
      }
    }
    catch(IOException e) {
      LOGGER.warn(e.getLocalizedMessage(), e);
    }
  }
}
