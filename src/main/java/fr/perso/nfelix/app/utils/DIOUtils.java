package fr.perso.nfelix.app.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IO utilities.
 *
 * @author n.felix
 * @version %I%, %G% %U%
 */
public final class DIOUtils {

  /** Logger. */
  private static final Logger LOGGER = LoggerFactory.getLogger(DIOUtils.class);

  /**
   * Makes a directory, including any necessary but nonexistent parent directories
   *
   * @param f1 input file
   * @param f2 input file
   */
  public static void copyFile(File f1, File f2) {

    try(FileInputStream fis = new FileInputStream(f1); FileOutputStream fos = new FileOutputStream(f2);) {
      IOUtils.copy(fis, fos);
    }
    catch(IOException e) {
      LOGGER.warn(e.getLocalizedMessage(), e);
    }
  }
}
