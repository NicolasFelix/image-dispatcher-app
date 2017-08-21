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
import fr.perso.nfelix.app.ui.services.utils.ProgressCallBack;
import fr.perso.nfelix.app.utils.ApplicationHolder;
import java.io.*;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
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

  private final static int    QUEUE_CAPACITY = 10000;
  private final static Object LOCK           = new Object();

  private final static DateTimeFormatter FULL_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private final static DateTimeFormatter YEAR_FORMATTER     = DateTimeFormatter.ofPattern("yyyy");
  private final static DateTimeFormatter MONTH_FORMATTER    = DateTimeFormatter.ofPattern("MM - MMMM");
  private final static DateTimeFormatter DAY_FORMATTER      = DateTimeFormatter.ofPattern("dd");

  private final static String[] MOVIE_EXT = { "mov", "avi", "mp4", "3gp" };

  @Setter
  private ResourceBundle resources;

  private JobRejectedExecutionHandler rejectedHanlder = new JobRejectedExecutionHandler();

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

  @Override
  protected Task<Boolean> createTask() {
    return new Task<Boolean>() {
      @Override
      protected Boolean call()
          throws Exception {

        LOGGER.debug(">>> call");
        final DispatcherConfig config = ApplicationHolder.getINSTANCE().getConfig();

        ServiceSingleRunResult ssrr = new ServiceSingleRunResult(updatableUI);
        ssrr.setStepFilter(config.getGlobalConfig().getDumpStep());

        try {
          final ImportConfig importConfig = config.getImportConfig();
          String inPath = importConfig.getImportFolder();
          String outPath = importConfig.getScanFolder();
          String duplicatePath = importConfig.getDuplicateFolder();
          final Path sourceDir = Paths.get(inPath);
          final Path targetDir = Paths.get(outPath);

          final Map<LocalDate, List<String>> imgsPerDay = getFileList(sourceDir);
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
            imgsPerDay.keySet().parallelStream().forEach(day -> {

              // if(isOperationCancelled()) {
              //   bre
              // };
              List<String> imgs = imgsPerDay.get(day);
              LOGGER.info(">>> starting group '{}' ({} items)", day, imgs.size());

              Path subOut = Paths.get(outPath, YEAR_FORMATTER.format(day), MONTH_FORMATTER.format(day), DAY_FORMATTER.format(day));
              Path duplicateOut = null;
              if(StringUtils.isNotBlank(duplicatePath)) {
                duplicateOut = Paths.get(duplicatePath, YEAR_FORMATTER.format(day), MONTH_FORMATTER.format(day), DAY_FORMATTER.format(day));
              }
              try {
                Files.createDirectories(subOut);

                for(String img : imgs) {

                  Path source = Paths.get(img);
                  Path destination = Paths.get(subOut.toString(), FilenameUtils.getName(img));

                  if(Files.exists(destination)) {
                    try {
                      destination = getFinalPath(subOut, img, duplicateOut);
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
              catch(IOException e) {
                LOGGER.warn("error while dealing with files from day '{}'", FULL_DAY_FORMATTER.format(day));
                ssrr.incrementFailed();
              }
              catch(Exception e) {
                LOGGER.warn("error while dealing with files from day '{}'", FULL_DAY_FORMATTER.format(day));
                ssrr.incrementFailed();
              }
              finally {
                LOGGER.info("<<< group '{}' done ({} items)", day, imgs.size());
              }
            });

            final long timeMillis = System.currentTimeMillis();
            long totalTimeMs = (timeMillis - ssrr.getStartDate().getTime());
            LOGGER.info("{} request(s) treated in {} ms ({} ms/req) // ({} skipped, {} error(s)) ", ssrr.getTotalRead(),
                DurationFormatUtils.formatDuration(totalTimeMs, FORMAT_DURATION_HMS), totalTimeMs / Math.max(1, ssrr.getTotalRead()), ssrr.getSkipped(),
                ssrr.getFailed());
          }
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

  private void copyFile(Path source, Path destination, ServiceSingleRunResult ssrr)
      throws IOException {

    // moree than 20Mo
    if(Files.size(source) > 1024 * 1024 * 20) {

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
    ProgressCallBack progressCallBack = new ProgressCallBack() {

      double latestProgress = -1d;
      private final int STEP = 5;

      @Override
      public void callback(CallbackByteChannel rbc, double progress) {
        // System.out.println(rbc.getReadSoFar());
        if(latestProgress == -1) {
          latestProgress = progress;
        }
        if((progress - latestProgress) > STEP) {
          latestProgress = progress;
          System.out.println(latestProgress);
        }
      }
    };

    // huge copy with callaback

    try(FileInputStream fis = new FileInputStream(source.toFile());
        FileChannel srcChannel = fis.getChannel();
        FileOutputStream fos = new FileOutputStream(destination.toFile());
        FileChannel dstChannel = fos.getChannel()) {

      ReadableByteChannel rbc = new CallbackByteChannel(srcChannel, Files.size(source), progressCallBack);
      dstChannel.transferFrom(rbc, 0, Long.MAX_VALUE);
    }
    catch(IOException ioe) {
      LOGGER.warn("error while copying file '{}' to '{}'", source, destination);
      // ssrr.incrementFailed(); // done later
      throw ioe;
    }

    ssrr.incrementSucceeded();
  }

  @Override
  public void preValidate()
      throws ConfigurationException {

  }

  private boolean isMovie(String fPath) {
    String fileExt = FilenameUtils.getExtension(fPath);
    return Arrays.stream(MOVIE_EXT).anyMatch(movExt -> StringUtils.equalsIgnoreCase(movExt, fileExt));
  }

  private LocalDate getFileOriginalDateTime(String fPath)
      throws ImageProcessingException, IOException {
    Metadata metadata = null;
    try {
      metadata = ImageMetadataReader.readMetadata(new File(fPath));
    }
    catch(ImageProcessingException ipe) {
      LOGGER.warn("error while getting '" + fPath + "' metadata. " + ipe.getLocalizedMessage(), ipe);
      return getFileSystemDateTime(fPath);
    }
    ExifSubIFDDirectory directory = (metadata != null) ? metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class) : null;
    if(directory == null) {
      LOGGER.warn("no EXIF directory found for file '{}'", fPath);
      return getFileSystemDateTime(fPath);
    }

    // query the tag's value
    Date dateOri = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
    if(dateOri == null) {
      LOGGER.warn("no date found for file '{}'", fPath);
      return null;
    }
    final String dayString = dateOri.toInstant().atZone(ZoneId.systemDefault()).toLocalDate().format(FULL_DAY_FORMATTER);
    return LocalDate.parse(dayString, FULL_DAY_FORMATTER);
  }

  private LocalDate getFileSystemDateTime(String fPath) {

    try {
      final BasicFileAttributes bfAtt = Files.readAttributes(Paths.get(fPath), BasicFileAttributes.class);
      final FileTime lastModifiedFT = bfAtt.lastModifiedTime();
      final FileTime creationFT = bfAtt.creationTime();
      if(creationFT.compareTo(lastModifiedFT) < 0) {
        return LocalDate.parse(FULL_DAY_FORMATTER.withLocale(Locale.FRENCH).withZone(ZoneId.systemDefault()).format(creationFT.toInstant()));
      }
      return LocalDate.parse(FULL_DAY_FORMATTER.withLocale(Locale.FRENCH).withZone(ZoneId.systemDefault()).format(lastModifiedFT.toInstant()));
    }
    catch(Throwable ioe) {
      LOGGER.error("error while getting File attribut: " + ioe.getLocalizedMessage(), ioe);
    }
    return null;
  }

  private Map<LocalDate, List<String>> getFileList(final Path sourceDir)
      throws IOException {
    LOGGER.debug(">>> getFileList");
    Map<LocalDate, List<String>> files = new TreeMap<>();

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
          LocalDate dayImg = null;
          if(fType == FileType.Unknown) {
            boolean movie = isMovie(fPath);
            if(movie) {
              dayImg = getLocalDateFromDateCreation(fPath);
            }
            else {
              LOGGER.warn("file '{}' unknown !", fPath);
              return FileVisitResult.CONTINUE;
            }
          }

          // else {
          // get date  obtain the Exif directory
          if(dayImg == null) {
            dayImg = getFileOriginalDateTime(fPath);
            if(dayImg == null) {
              dayImg = getLocalDateFromDateCreation(fPath);
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

  private LocalDate getLocalDateFromDateCreation(String fPath)
      throws IOException {
    LocalDate dayImg;
    final BasicFileAttributes basicFileAttributes = Files.readAttributes(Paths.get(fPath), BasicFileAttributes.class);
    final FileTime lastModifiedTime = basicFileAttributes.lastModifiedTime();
    final Instant instant = lastModifiedTime.toInstant();
    dayImg = instant.atZone(ZoneId.systemDefault()).toLocalDate();
    return dayImg;
  }

  /**
   * get final path, if image is different from existing one
   *
   * @param subOut       folder
   * @param img          image name
   * @param duplicateOut duplicated path
   * @return final image path, or null if image is already present...
   */
  private Path getFinalPath(Path subOut, String img, Path duplicateOut)
      throws ImageProcessingException, IOException {

    Path destination = Paths.get(subOut.toString(), FilenameUtils.getName(img));
    Path source = Paths.get(img);

    final long szDest = Files.size(destination);
    final long szSrc = Files.size(source);
    if(szSrc == szDest) {
      final LocalDate sourceDateTime = getFileOriginalDateTime(img);
      final LocalDate destinaltionDateTime = getFileOriginalDateTime(img);
      if(sourceDateTime != null && destinaltionDateTime != null && sourceDateTime.equals(destinaltionDateTime)) {

        if(duplicateOut != null) {
          Files.createDirectories(duplicateOut);
          destination = Paths.get(duplicateOut.toString(), FilenameUtils.getName(img));
          try {
            Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
          }
          catch(IOException ioe) {
            LOGGER.warn("error while copying file '" + source + "' to '" + destination + "': " + ioe.getLocalizedMessage(), ioe);
          }
          // copy it to external folder...
          return null;
        }
        LOGGER.info("file '{}' and '{}' hold same orgin dates", source, destination);
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
}
