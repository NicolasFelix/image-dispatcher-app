package fr.perso.nfelix.app.exception;

/**
 * Exception throwed for a configuration error.
 *
 * @author J.DeBouillanne
 * @version %I%, %G% %U%
 */
public class ConfigurationException extends RuntimeException {
  private static final long serialVersionUID = 920533634722782526L;

  /**
   * Constructs a new ConfigurationException exception with the specified detail message. *
   *
   * @param message the detail message.
   */
  public ConfigurationException(String message) {
    super(message);
  }

  /**
   * Constructs a new ConfigurationException exception with the specified detail message and cause.
   * Note that the detail message associated with <code>cause</code> is <i>not</i> automatically incorporated in this runtime exception's detail message.
   *
   * @param message the detail message
   * @param cause   the cause (which is saved for later retrieval by the {@link #getCause()} method). (A <tt>null</tt> value is permitted, and indicates that
   *                the
   *                cause is nonexistent or unknown.)
   */
  public ConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }
}
