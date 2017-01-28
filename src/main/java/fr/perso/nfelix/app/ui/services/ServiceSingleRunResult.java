package fr.perso.nfelix.app.ui.services;

import static fr.perso.nfelix.app.utils.sgbd.DalConstants.FORMAT_DURATION_HMS;

import fr.perso.nfelix.app.ui.controllers.IUpdatableUI;
import java.text.MessageFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * ServiceSingleRunResult
 *
 * @author N.FELIX
 */
@Slf4j
public class ServiceSingleRunResult {

  private AtomicLong failed    = new AtomicLong();
  private AtomicLong succeeded = new AtomicLong();
  private AtomicLong skipped   = new AtomicLong();

  @Getter
  private Date startDate     = new Date();
  private Date stepStartDate = new Date();

  @Setter
  private IUpdatableUI ui;
  @Setter
  private int stepFilter = 100;

  public ServiceSingleRunResult() {
    this(null);
  }

  public ServiceSingleRunResult(IUpdatableUI ui) {
    super();
    this.ui = ui;
  }

  @Setter
  @Getter

  private long expectedNumber = -1L;

  public void reset() {
    failed = new AtomicLong();
    succeeded = new AtomicLong();
    skipped = new AtomicLong();
    startDate = new Date();
    stepStartDate = new Date();
    expectedNumber = -1L;
  }

  public long incrementFailed() {
    return failed.incrementAndGet();
  }

  public long incrementSucceeded() {
    long ret = succeeded.incrementAndGet();
    dumpProgress(ret + getSkipped());
    return ret;
  }

  public long incrementSkipped() {
    long ret = skipped.incrementAndGet();
    dumpProgress(ret + getSucceeded());
    return ret;
  }

  public long getFailed() {
    return failed.get();
  }

  public long getSucceeded() {
    return succeeded.get();
  }

  public long getSkipped() {
    return skipped.get();
  }

  public long getTotalRead() {
    return getFailed() + getSucceeded() + getSkipped();
  }

  private void dumpProgress(long currentValue) {
    if(ui != null) {
      stepFilter = Math.max(stepFilter, 1);
      if(currentValue > 0 && (currentValue % stepFilter) == 0) {

        final long timeMillis = System.currentTimeMillis();
        long totalTimeMs = (timeMillis - startDate.getTime());
        long stepTimeMs = (timeMillis - stepStartDate.getTime());
        long itemLeft = expectedNumber - currentValue;
        long estimatedTimeLeftMs = ((currentValue > 0) ? ((itemLeft * totalTimeMs) / currentValue) : 0);

        ui.onUpdateText(MessageFormat
            .format("{0} request(s) treated in {1} ms ({2} ms/req, last {3} : {4} ms/req)  // ({5} skipped, {6} error(s)) [Left items:{7}, time:''{8}'']",
                currentValue, DurationFormatUtils.formatDuration(totalTimeMs, FORMAT_DURATION_HMS), totalTimeMs / currentValue, stepFilter,
                stepTimeMs / stepFilter, getSkipped(), getFailed(), itemLeft, DurationFormatUtils.formatDuration(estimatedTimeLeftMs, FORMAT_DURATION_HMS)));

        ui.onUpdateProgress((double) currentValue / Math.max(1.0, expectedNumber));
        ui.onUpdateProgressMessage(currentValue + " / " + expectedNumber);

        stepStartDate = new Date();
      }
    }
  }
}
