package fr.perso.nfelix.app.ui.services;

import static fr.perso.nfelix.app.ui.typedef.Constants.FORMAT_DURATION_HMS;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import fr.perso.nfelix.app.DispatcherConfig;
import fr.perso.nfelix.app.exception.ConfigurationException;
import fr.perso.nfelix.app.ui.config.ImportConfig;
import fr.perso.nfelix.app.ui.controllers.IUpdatableUI;
import fr.perso.nfelix.app.ui.services.utils.CallbackByteChannel;
import fr.perso.nfelix.app.ui.services.utils.HugeCopyCallback;
import fr.perso.nfelix.app.utils.ApplicationHolder;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import javafx.concurrent.Task;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;

/**
 * xml Extraction report geneeration service
 *
 * @author N.FELIX
 */
@SuppressWarnings("ALL")
@Slf4j
public class FindAndDispatchImgService extends AbstractThreadedService<Boolean> implements IPreValidateService {

  private final static int    HUGE_FILE_SIZELIMIT = 1024 * 1024 * 15;
  private final static int    QUEUE_CAPACITY      = 10000;
  private final static Object LOCK                = new Object();

  private final static DateTimeFormatter FULL_DAY_FORMATTER      = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final static DateTimeFormatter YEAR_FORMATTER          = DateTimeFormatter.ofPattern("yyyy");
  private final static DateTimeFormatter MONTH_FORMATTER         = DateTimeFormatter.ofPattern("MM - MMMM");
  private final static DateTimeFormatter DAY_FORMATTER           = DateTimeFormatter.ofPattern("dd");
  private final static DateTimeFormatter FULL_DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

  private final static String[] MOVIE_EXT = { "mov", "avi", "mp4", "3gp" };

  @Setter
  private ResourceBundle resources;

  private ExecutorService             hugeFileThreadPool = Executors.newFixedThreadPool(4);
  private JobRejectedExecutionHandler rejectedHanlder    = new JobRejectedExecutionHandler();

  private DispatcherConfig config = null;
  private ZoneId           zoneId = null;

  public FindAndDispatchImgService(IUpdatableUI updatableUI) {
    super(updatableUI);
  }

  /**
   * constructor
   *
   * @param resources   i18n resources
   * @param taskData    thread/task data, see {@link TaskProgressData}
   * @param updatableUI {@link IUpdatableUI}
   */
  public FindAndDispatchImgService(ResourceBundle resources, TaskProgressData taskData, IUpdatableUI updatableUI) {
    super(taskData, updatableUI);

    this.resources = resources;
  }

  private void clear() {
    config = null;
    zoneId = null;
  }

  private void init() {
    config = ApplicationHolder.getINSTANCE().getConfig();
    getZoneId();
  }

  @Override
  protected Task<Boolean> createTask() {
    return new Task<Boolean>() {
      @Override
      protected Boolean call()
          throws Exception {

        LOGGER.debug(">>> call");
        clear();
        init();

        ServiceSingleRunResult ssrr = new ServiceSingleRunResult(updatableUI);
        ssrr.setStepFilter(config.getGlobalConfig().getDumpStep());

        try {
          final ImportConfig importConfig = config.getImportConfig();
          String inPath = importConfig.getImportFolder();
          String outPath = importConfig.getScanFolder();
          final Path sourceDir = Paths.get(inPath);
          final Path targetDir = Paths.get(outPath);

          final Map<Temporal, List<String>> imgsPerDay = getFileList(sourceDir);
          LOGGER.info(">>> parsing result <<<");
          AtomicLong total = new AtomicLong();
          imgsPerDay.forEach((day, imgs) -> {
            LOGGER.info("{} : {} images", day, imgs.size());
            total.addAndGet(imgs.size());
          });

          final long totalImgFound = total.get();
          LOGGER.info(">>> results parsed : {} images", totalImgFound);

          if(totalImgFound > 0) {
            ssrr.setExpectedNumber(totalImgFound);
            Files.createDirectories(targetDir);
            // copy them to target
            ForkJoinPool customThreadPool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
            customThreadPool.submit(() -> imgsPerDay.keySet().parallelStream().forEach(day -> {

              List<String> imgs = imgsPerDay.get(day);
              LOGGER.info(">>> starting group '{}' ({} items)", day, imgs.size());

              Path subOut = Paths.get(outPath, YEAR_FORMATTER.format(day), MONTH_FORMATTER.format(day), DAY_FORMATTER.format(day));
              try {
                Files.createDirectories(subOut);

                for(String img : imgs) {
                  if(isOperationCancelled()) {
                    break;
                  }

                  Path source = Paths.get(img);
                  final String name = computeFileName(img);
                  Path destination = Paths.get(subOut.toString(), name);

                  if(Files.exists(destination)) {
                    try {
                      destination = getFinalPath(subOut, img);
                      if(destination == null) {
                        ssrr.incrementSkipped();
                        continue;
                      }
                    }
                    catch(ImageProcessingException e) {
                      LOGGER.error("getFinalPath: " + e.getLocalizedMessage(), e);
                    }
                  }

                  copyFile(source, destination, ssrr);
                }
              }
              catch(Exception e) {
                LOGGER.warn("error while dealing with files from day '{}'", FULL_DAY_FORMATTER.format(day));
                LOGGER.error(e.getLocalizedMessage(), e);
                ssrr.incrementFailed();
              }
              finally {
                LOGGER.info("<<< group '{}' done ({} items)", day, imgs.size());
              }
            }));

            awaitTerminationAfterShutdown(customThreadPool);

            final long timeMillis = System.currentTimeMillis();
            long totalTimeMs = (timeMillis - ssrr.getStartDate().getTime());
            LOGGER.info("{} request(s) treated in {} ms ({} ms/req) // ({} skipped, {} error(s)) ", ssrr.getTotalRead(),
                DurationFormatUtils.formatDuration(totalTimeMs, FORMAT_DURATION_HMS), totalTimeMs / Math.max(1, ssrr.getTotalRead()), ssrr.getSkipped(),
                ssrr.getFailed());
          }
          awaitTerminationAfterShutdown(hugeFileThreadPool);
        }
        catch(IOException e) {
          LOGGER.error(e.getLocalizedMessage(), e);
        }
        finally {
          LOGGER.info("<<< runJobs (service started)");
        }

        return true;
      }
    };
  }

  private void awaitTerminationAfterShutdown(ExecutorService threadPool) {
    LOGGER.debug("shuting down tread pool !!");
    threadPool.shutdown();
    try {
      if(!threadPool.awaitTermination(10, TimeUnit.MINUTES)) {
        LOGGER.info("shuting down tread pool NOW (1) !!");
        threadPool.shutdownNow();
      }
      else {
        LOGGER.info("Thread pool terminated !!");
      }
    }
    catch(InterruptedException ex) {
      LOGGER.warn("shuting down tread pool NOW (1) !!");;
      threadPool.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  private String computeFileName(String inputFilePath) {
    ImportConfig ic = this.config.getImportConfig();
    if(ic.isRenameFile()) {
      try {
        Temporal fileOriginalDate = null;
        if(isMovie(inputFilePath)) {
          fileOriginalDate = getLocalDateFromDateCreation(inputFilePath, true);
        }
        else {
          fileOriginalDate = getFileOriginalDateTime(inputFilePath, true);
        }
        return FULL_DATETIME_FORMATTER.format(fileOriginalDate) + "." + FilenameUtils.getExtension(inputFilePath);
      }
      catch(ImageProcessingException | IOException e) {
        LOGGER.error("Error while getting file date");
      }
    }
    return FilenameUtils.getName(inputFilePath);
  }

  private void copyFile(Path source, Path destination, ServiceSingleRunResult ssrr)
      throws IOException {

    // more than 15Mo

    if(Files.size(source) > HUGE_FILE_SIZELIMIT) {
      copyHugeFile(source, destination, ssrr);
    }
    else {
      try {
        Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
        ssrr.incrementSucceeded();
      }
      catch(IOException e) {
        LOGGER.warn("error while copying file '{}' to '{}'", source, destination);
        ssrr.incrementFailed();
      }
    }
  }

  private void copyHugeFile(Path source, Path destination, ServiceSingleRunResult ssrr)
      throws IOException {

    // huge copy with callaback
    CancellableService service = this;

    LOGGER.info("copying huge file {}", source.getFileName().toString());

    hugeFileThreadPool.submit(() -> {
      try(FileInputStream fis = new FileInputStream(source.toFile());
          FileChannel srcChannel = fis.getChannel();
          FileOutputStream fos = new FileOutputStream(destination.toFile());
          FileChannel dstChannel = fos.getChannel()) {

        ReadableByteChannel rbc = new CallbackByteChannel(srcChannel, Files.size(source), new HugeCopyCallback(source, service));
        dstChannel.transferFrom(rbc, 0, Long.MAX_VALUE);
        ssrr.incrementSucceeded();
      }
      catch(IOException ioe) {
        LOGGER.warn("error while copying file '{}' to '{}'", source, destination);
        ssrr.incrementFailed(); // done later
        // throw ioe;
      }
      // catch(InterruptedException ie) {
      //   LOGGER.warn("error while copying file '{}' to '{}'", source, destination);
      //   ssrr.incrementSkipped(); // done
      // }
    });
    // worker.start();

    if(!isOperationCancelled()) {
      ssrr.incrementSucceeded();
    }
  }

  @Override
  public void preValidate()
      throws ConfigurationException {

  }

  private boolean isMovie(String fPath) {
    String fileExt = FilenameUtils.getExtension(fPath);
    return Arrays.stream(MOVIE_EXT).anyMatch(movExt -> StringUtils.equalsIgnoreCase(movExt, fileExt));
  }

  private Temporal getFileOriginalDateTime(String fPath, boolean fullDate)
      throws ImageProcessingException, IOException {
    Metadata metadata = null;
    try {
      metadata = ImageMetadataReader.readMetadata(new File(fPath));
    }
    catch(ImageProcessingException ipe) {
      LOGGER.warn("error while getting '" + fPath + "' metadata. " + ipe.getLocalizedMessage(), ipe);
      return getFileSystemDateTime(fPath, fullDate);
    }

    ExifSubIFDDirectory directory = (metadata != null) ? metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class) : null;
    if(directory == null) {
      LOGGER.warn("no EXIF directory found for file '{}'", fPath);
      return getFileSystemDateTime(fPath, fullDate);
    }

    // query the tag's value
    Date dateOri = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
    if(dateOri == null) {
      LOGGER.warn("no date found for file '{}'", fPath);
      return getFileSystemDateTime(fPath, fullDate);
    }

    if(fullDate) {
      final String dayString = dateOri.toInstant().atZone(getZoneId()).toLocalDateTime().format(FULL_DATETIME_FORMATTER);
      return LocalDateTime.parse(dayString, FULL_DATETIME_FORMATTER);
    }

    final String dayString = dateOri.toInstant().atZone(getZoneId()).toLocalDate().format(FULL_DAY_FORMATTER);
    return LocalDate.parse(dayString, FULL_DAY_FORMATTER);
  }

  private Temporal getFileSystemDateTime(String fPath, boolean fullDate) {

    try {
      final BasicFileAttributes bfAtt = Files.readAttributes(Paths.get(fPath), BasicFileAttributes.class);
      final FileTime fileFT = (bfAtt.creationTime().compareTo(bfAtt.lastModifiedTime()) < 0) ? bfAtt.creationTime() : bfAtt.lastModifiedTime();

      if(fullDate) {
        return LocalDateTime.parse(FULL_DATETIME_FORMATTER.withLocale(Locale.FRENCH).withZone(getZoneId()).format(fileFT.toInstant()));
      }
      return LocalDate.parse(FULL_DAY_FORMATTER.withLocale(Locale.FRENCH).withZone(getZoneId()).format(fileFT.toInstant()));
    }
    catch(Throwable ioe) {
      LOGGER.error("error while getting File attribut: " + ioe.getLocalizedMessage(), ioe);
    }
    return null;
  }

  private Map<Temporal, List<String>> getFileList(final Path sourceDir)
      throws IOException {
    LOGGER.debug(">>> getFileList");
    Map<Temporal, List<String>> files = new TreeMap<>();

    Files.walkFileTree(sourceDir, EnumSet.of(FileVisitOption.FOLLOW_LINKS), Integer.MAX_VALUE, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
          throws IOException {

        final String fPath = file.toString();

        FileType fType;
        try(FileInputStream fis = new FileInputStream(fPath);
            BufferedInputStream bis = new BufferedInputStream(fis)) {
          fType = FileTypeDetector.detectFileType(bis);
        }
        catch(IOException ioe) {
          fType = FileType.Unknown;
        }

        try {
          Temporal dayImg = null;
          if(fType == FileType.Unknown) {
            boolean movie = isMovie(fPath);
            if(movie) {
              dayImg = getLocalDateFromDateCreation(fPath, false);
            }
            else {
              LOGGER.warn("file '{}' unknown !", fPath);
              return FileVisitResult.CONTINUE;
            }
          }

          // else {
          // get date  obtain the Exif directory
          if(dayImg == null) {
            dayImg = getFileOriginalDateTime(fPath, false);
            if(dayImg == null) {
              dayImg = getLocalDateFromDateCreation(fPath, false);
            }
            if(dayImg == null) {
              return FileVisitResult.CONTINUE;
            }
          }

          List<String> subFiles = files.get(dayImg);
          if(subFiles == null) {
            subFiles = new ArrayList<>();
            files.put(dayImg, subFiles);
            LOGGER.info("new group added ('{}')", dayImg);
            if(updatableUI != null) {
              updatableUI.onUpdateText("new list created, day '" + dayImg + "'");
            }
          }
          subFiles.add(fPath);
        }
        catch(ImageProcessingException e) {
          LOGGER.error("ImageProcessingException on " + fPath, e);
        }
        catch(IOException e) {
          LOGGER.error("ioe on " + fPath, e);
        }
        return isOperationCancelled() ? FileVisitResult.TERMINATE : FileVisitResult.CONTINUE;
      }
    });

    LOGGER.debug("<<< getFileList ({} files)", files.size());
    return files;
  }

  private Temporal getLocalDateFromDateCreation(String fPath, boolean fullDate)
      throws IOException {
    final BasicFileAttributes basicFileAttributes = Files.readAttributes(Paths.get(fPath), BasicFileAttributes.class);
    final FileTime lastModifiedTime = basicFileAttributes.lastModifiedTime();
    final Instant instant = lastModifiedTime.toInstant();
    if(fullDate) {
      return instant.atZone(getZoneId()).toLocalDateTime();
    }
    return instant.atZone(getZoneId()).toLocalDate();
  }

  /**
   * get final path, if image is different from existing one
   *
   * @param subOut folder
   * @param img    image name
   * @return final image path, or null if image is already present...
   */
  private synchronized Path getFinalPath(Path subOut, String img)
      throws ImageProcessingException, IOException {

    Path destination = Paths.get(subOut.toString(), FilenameUtils.getName(img));
    if(Files.notExists(destination)) {
      return destination;
    }
    Path source = Paths.get(img);

    final long szDest = Files.size(destination);
    final long szSrc = Files.size(source);
    if(szSrc == szDest) {
      final Temporal sourceDateTime = getFileOriginalDateTime(img, false);
      final Temporal destinaltionDateTime = getFileOriginalDateTime(img, false);
      if(sourceDateTime != null && destinaltionDateTime != null && sourceDateTime.equals(destinaltionDateTime)) {
        LOGGER.info("file '{}' and '{}' hold same origin dates", source, destination);
        return null;
      }
    }

    LOGGER.info("File '{}' already exists but are different ! Renaming it...", FilenameUtils.getName(img));
    String baseName = FilenameUtils.getBaseName(img);
    String extension = "." + FilenameUtils.getExtension(img);
    synchronized( LOCK ) {
      int counter = 0;
      String finalName;
      do {
        finalName = baseName + "_" + (++counter) + extension;
        destination = Paths.get(subOut.toString(), finalName);
      }
      while(Files.exists(destination));
    }
    return destination;
  }

  private ZoneId getZoneId() {
    if(zoneId == null) {
      zoneId = ZoneId.systemDefault();
      if(config.getImportConfig().getTimeOffset() != 0) {
        return zoneId.ofOffset("GMT", ZoneOffset.ofHours(config.getImportConfig().getTimeOffset()));
      }
    }
    return zoneId;
  }
}
