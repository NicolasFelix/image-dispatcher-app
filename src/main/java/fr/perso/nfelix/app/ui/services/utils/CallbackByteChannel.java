package fr.perso.nfelix.app.ui.services.utils;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

/**
 * CallbackByteChannel
 */
public class CallbackByteChannel implements ReadableByteChannel {
  ProgressCallBack    delegate;
  ReadableByteChannel rbc;

  long size;
  long sizeRead;

  /**
   * constructor
   * @param rbc           {@link ReadableByteChannel}
   * @param expectedSize expected size
   * @param delegate delegate
   */
  public CallbackByteChannel(ReadableByteChannel rbc, long expectedSize, ProgressCallBack delegate) {
    this.delegate = delegate;
    this.size = expectedSize;
    this.rbc = rbc;
  }

  public void close()
      throws IOException {
    rbc.close();
  }

  public long getReadSoFar() {
    return sizeRead;
  }

  public boolean isOpen() {
    return rbc.isOpen();
  }

  /**
   * read
   * @param bb byte byffer
   * @return read byte number
   * @throws IOException in case of...
   */
  public int read(ByteBuffer bb)
      throws IOException {
    int n;
    double progress;
    if((n = rbc.read(bb)) > 0) {
      sizeRead += n;
      progress = size > 0 ? (double) sizeRead / (double) size * 100.0 : -1.0;
      delegate.callback(this, progress);
    }
    return n;
  }
}