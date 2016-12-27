package fr.perso.nfelix.app.ui.controllers;

import static fr.perso.nfelix.app.ui.config.DatabaseConfig.DBStatus.INVALID;
import static fr.perso.nfelix.app.ui.typedef.Constants.*;
import static fr.perso.nfelix.app.ui.utils.JavaFXUtils.createDialog;
import static fr.perso.nfelix.app.ui.utils.JavaFXUtils.createPasswordDialog;
import static fr.perso.nfelix.app.utils.sgbd.DalConstants.FORMAT_DURATION_HMS;

import com.drew.imaging.FileType;
import com.drew.imaging.FileTypeDetector;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import fr.perso.nfelix.app.DispatcherConfig;
import fr.perso.nfelix.app.ImgDispatcherUIAppPreloader;
import fr.perso.nfelix.app.ui.config.DatabaseConfig;
import fr.perso.nfelix.app.ui.controllers.fragments.JobWorkProgressFragment;
import fr.perso.nfelix.app.ui.services.CancellableService;
import fr.perso.nfelix.app.ui.services.IPreValidateService;
import fr.perso.nfelix.app.ui.services.ServiceSingleRunResult;
import fr.perso.nfelix.app.ui.utils.JavaFXUtils;
import fr.perso.nfelix.app.ui.utils.PasswordInputDialog;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.controlsfx.control.MaskerPane;
import org.controlsfx.control.Notifications;
import org.controlsfx.control.action.ActionMap;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
public class HomeController extends AbstractFxController {

  @FXML
  private MenuItem   exitMenuItem;
  @FXML
  private MenuItem   preferencesMenuItem;
  @FXML
  private MenuItem   aboutMenuItem;
  @FXML
  private MenuItem   userGuideMenuItem;
  @FXML
  private MenuItem   logsMenuItem;
  @FXML
  private MenuItem   modeExpertMenuItem;
  @FXML
  private MenuBar    homeMenuBar;
  @FXML
  private MaskerPane maskPane;
  @FXML
  private StackPane  insideMainPane;

  @FXML
  private Label  databaseStatus;
  @FXML
  private Button statusDatabaseBut;

  //thos will contain job-in-progress panel
  @FXML
  private Parent                  jobProgressFragment;
  @FXML
  private JobWorkProgressFragment jobProgressFragmentController;

  @FXML
  private Button runButton;

  private DateTimeFormatter FULL_DAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
  private DateTimeFormatter YEAR_FORMATTER     = DateTimeFormatter.ofPattern("yyyy");
  private DateTimeFormatter MONTH_FORMATTER    = DateTimeFormatter.ofPattern("MM - MMMM");
  private DateTimeFormatter DAY_FORMATTER      = DateTimeFormatter.ofPattern("dd");

  private final static String[] MOVIE_EXT = { "mov", "avi", "mp4" };

  @Override
  public void postInit() {
    super.postInit();
    ActionMap.register(this);

    jobProgressFragmentController.setParentController(this);

    // do not put image into css, as with shortcut declaration, icon will be duplicated
    exitMenuItem.setGraphic(new ImageView(new Image(EXIT_MENU_ICON)));
    preferencesMenuItem.setGraphic(new ImageView(new Image(TOOLS_MENU_ICON)));
    aboutMenuItem.setGraphic(new ImageView(new Image(HELP_MENU_ICON)));
    modeExpertMenuItem.setGraphic(new ImageView(new Image(EXPERT_MENU_ICON)));
    userGuideMenuItem.setGraphic(new ImageView(new Image(USERGUIDE_MENU_ICON)));
    logsMenuItem.setGraphic(new ImageView(new Image(LOGS_MENU_ICON)));
  }

  @Override
  public void initUI() {
    super.initUI();

    try {
      LOGGER.debug(">>> initUI");

      statusDatabaseBut.setOnAction(this::goToSettings);
      final DispatcherConfig config = getMainApp().getConfig();
      modeExpertMenuItem.setDisable(DispatcherConfig.isExpertMode());

      DatabaseConfig.DBStatus dbStatus = INVALID; //config.getDbConfig() != null ? config.getDbConfig().getStatus() : INVALID;
      statusDatabaseBut.setGraphic(new ImageView(getIcon(dbStatus)));
      statusDatabaseBut.setTooltip(new Tooltip(getTooltipText(dbStatus)));

      databaseStatus.setText(getSafeResourceValue("label.database.status", config.getDbConfig().getInfoString()));

      // final boolean databasesOK = OK.equals(databaseStatus);

      // enableDisableCtrl(runButton, OK.equals(cityWebStatus) && DCollectionUtils.isNotEmpty(jobList.getCheckModel().getCheckedItems()));

      // work in progress part...
      jobProgressFragmentController.initUI();
    }
    catch(Throwable th) {
      LOGGER.error(th.getLocalizedMessage(), th);
    }
    finally {
      getMainApp().notifyPreloader(new ImgDispatcherUIAppPreloader.HideNotification());
      LOGGER.debug("<<< initUI");
    }
  }

  private String getTooltipText(DatabaseConfig.DBStatus dbStatus) {
    switch( dbStatus ) {
    case OK:
      return resources.getString("database.status.ok");
    case KO:
      return resources.getString("database.status.ko");

    default:
    case INVALID:
      return resources.getString("database.status.invalid");
    }
  }

  private String getIcon(DatabaseConfig.DBStatus dbStatus) {

    switch( dbStatus ) {
    case OK:
      return STATUS_OK_ICON;
    case KO:
      return STATUS_KO_ICON;

    default:
    case INVALID:
      return STATUS_WARN_ICON;
    }
  }

  public void quitApp(ActionEvent actionEvent) {
    quitApp();
  }

  public void goToSettings(ActionEvent ae) {
    switchToView(FXML_SETTINGS);
  }

  /**
   * here, job will be run/launched !
   *
   * @param ae {@link ActionEvent}
   */
  public void runJobs(ActionEvent ae) {

    LOGGER.info(">>> runImport");

    try {
      String inPath = "D:\\01--Mes Documents\\Perso\\photos\\";
      String outPath = "D:\\zDownload\\zzImages\\";
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
        ServiceSingleRunResult ssrs = new ServiceSingleRunResult();
        Files.createDirectories(targetDir);
        // copy them to target
        imgsPerDay.forEach((day, imgs) -> {

          LOGGER.info("treating date '{}' ({} items)", day, imgs.size());

          Path subOut = Paths.get(outPath, YEAR_FORMATTER.format(day), MONTH_FORMATTER.format(day), DAY_FORMATTER.format(day));
          try {
            Files.createDirectories(subOut);

            for(String img : imgs) {

              Path source = Paths.get(img);
              Path destination = Paths.get(subOut.toString(), FilenameUtils.getName(img));

              if(Files.exists(destination)) {
                try {
                  destination = getFinalPath(subOut, img);
                  if(destination == null) {
                    ssrs.incrementSkipped();
                    continue;
                  }
                }
                catch(ImageProcessingException e) {
                  LOGGER.error("getFinalPath: " + e.getLocalizedMessage(), e);
                }
              }

              try {
                Files.copy(source, destination, StandardCopyOption.REPLACE_EXISTING);
                ssrs.incrementSucceeded();
              }
              catch(IOException e) {
                LOGGER.warn("error while copying file '{}' to '{}'", source, destination);
                ssrs.incrementFailed();
              }
            }
          }
          catch(IOException e) {
            LOGGER.warn("error while dealing with files from day '{}'", FULL_DAY_FORMATTER.format(day));
            ssrs.incrementFailed();
          }
        });

        final long timeMillis = System.currentTimeMillis();
        long totalTimeMs = (timeMillis - ssrs.getStartDate().getTime());
        LOGGER.info("{} request(s) treated in {} ms ({} ms/req) // ({} skipped, {} error(s)) ", ssrs.getTotalRead(),
            DurationFormatUtils.formatDuration(totalTimeMs, FORMAT_DURATION_HMS), totalTimeMs / Math.max(1, ssrs.getTotalRead()), ssrs.getSkipped(),
            ssrs.getFailed());
      }
    }
    catch(IOException e) {
      LOGGER.error(e.getLocalizedMessage(), e);
    }
    finally {
      LOGGER.info("<<< runImport (service started)");
    }
  }

  /**
   * get final path, if image is different from existing one
   *
   * @param subOut folder
   * @param img    image name
   * @return final image path, or null if image is already present...
   */
  private Path getFinalPath(Path subOut, String img)
      throws ImageProcessingException, IOException {

    Path destination = Paths.get(subOut.toString(), FilenameUtils.getName(img));
    Path source = Paths.get(img);

    final long szDest = Files.size(destination);
    final long szSrc = Files.size(source);
    if(szSrc == szDest) {
      final LocalDate sourceDateTime = getFileOriginalDateTime(img);
      final LocalDate destinaltionDateTime = getFileOriginalDateTime(img);
      if(sourceDateTime != null && destinaltionDateTime != null && sourceDateTime.equals(destinaltionDateTime)) {
        LOGGER.info("file '{}' and '{}' hold same orgin dates", source, destination);
        return null;
      }
    }

    LOGGER.info("File '{}' already exists but are different ! Renaming it...", FilenameUtils.getName(img));
    String baseName = FilenameUtils.getBaseName(img);
    String extension = "." + FilenameUtils.getExtension(img);
    int counter = 0;
    String finalName;
    do {
      finalName = baseName + "_" + (++counter) + extension;
      destination = Paths.get(subOut.toString(), finalName);
    }
    while(Files.exists(destination));
    return destination;
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
        try(FileInputStream fis = new FileInputStream(fPath); BufferedInputStream bis = new BufferedInputStream(fis)) {
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
              final BasicFileAttributes basicFileAttributes = Files.readAttributes(Paths.get(fPath), BasicFileAttributes.class);
              final FileTime lastModifiedTime = basicFileAttributes.creationTime();
              final Instant instant = lastModifiedTime.toInstant();
              // LOGGER.info("{}", instant);
              // LocalDateTime ldt = LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
              // LOGGER.info("{}", ldt);
              dayImg = instant.atZone(ZoneId.systemDefault()).toLocalDate();
              // LOGGER.info("{}", localDate);
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
              return FileVisitResult.CONTINUE;
            }
          }

          List<String> subFiles = files.get(dayImg);
          if(subFiles == null) {
            subFiles = new ArrayList<>();
            files.put(dayImg, subFiles);
            LOGGER.info("list added for day '{}'", dayImg);
          }
          subFiles.add(fPath);
        }
        catch(ImageProcessingException e) {
          LOGGER.error("ImageProcessingException on " + fPath, e);
        }
        catch(IOException e) {
          LOGGER.error("ioe on " + fPath, e);
        }
        return FileVisitResult.CONTINUE;
      }
    });

    LOGGER.debug("<<< getFileList ({} files)", files.size());
    return files;
  }

  private boolean isMovie(String fPath) {
    String fileExt = FilenameUtils.getExtension(fPath);
    return Arrays.stream(MOVIE_EXT).anyMatch(movExt -> StringUtils.equalsIgnoreCase(movExt, fileExt));
  }

  private LocalDate getFileOriginalDateTime(String fPath)
      throws ImageProcessingException, IOException {
    Metadata metadata = ImageMetadataReader.readMetadata(new File(fPath));
    ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
    if(directory == null) {
      LOGGER.warn("no directory found for file '{}'", fPath);
      return null;
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

  private void displayNotification(boolean failed, String msgSuffix) {
    enableDisableUI(true, true);
    jobProgressFragmentController.onUpdateJobName("...");

    Notifications notificationBuilder = Notifications.create().title(getSafeResourceValue("run.job.notification.title." + msgSuffix))
        .text(getSafeResourceValue("run.job.notification.content." + msgSuffix)).hideAfter(Duration.seconds(5.0)).position(Pos.BOTTOM_RIGHT);
    if(failed) {
      notificationBuilder.showError();
    }
    else {
      notificationBuilder.showInformation();
    }
  }

  public void showHideMask(boolean show) {
    maskPane.setVisible(show);
  }

  /**
   * activation du mode expert
   *
   * @param ae {@link ActionEvent}
   */
  public void activateExpertMode(ActionEvent ae) {

    PasswordInputDialog dlg = createPasswordDialog(resources, "mode.expert.title", "mode.expert.header", "mode.expert.content");
    dlg.showAndWait().ifPresent(password -> {
      if(StringUtils.isNotBlank(password)) {
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        if(passwordEncoder.matches(password, ENCODED_PWD)) {
          DispatcherConfig.setExpertMode(true);
          resetView(FXML_SETTINGS);

          JavaFXUtils
              .displayNotification(Alert.AlertType.INFORMATION, getSafeResourceValue("mode.expert.title"), getSafeResourceValue("mode.expert" + ".activated"));
          modeExpertMenuItem.setDisable(true);
        }
        else {
          JavaFXUtils
              .displayNotification(Alert.AlertType.WARNING, getSafeResourceValue("mode.expert.title"), getSafeResourceValue("mode.expert.not.activated"));
        }
      }
    });
  }

  private void launchService(CancellableService<String> service, final String maskKey, final String dialogPrefixKey) {

    LOGGER.debug(">>> launchService({})", service);

    try {
      maskPane.setText(getSafeResourceValue(maskKey));

      if(service instanceof IPreValidateService) {
        ((IPreValidateService) service).preValidate();
      }

      // cancelled
      service.setOnCancelled(event -> {
        maskPane.setVisible(false);
        enableDisableUI(true, false);
      });

      // failed
      service.setOnFailed(event -> {
        String errorMsg = service.getException() != null ? service.getException().getLocalizedMessage() : "";
        final Alert dialog = createDialog(resources, Alert.AlertType.ERROR, dialogPrefixKey + ".title", dialogPrefixKey + ".header",
            dialogPrefixKey + ".content", null, null, new Object[] { errorMsg }, service.getException());
        dialog.showAndWait();
        maskPane.setVisible(false);
        enableDisableUI(true, false);
      });

      // succeeded
      service.setOnSucceeded(event -> {
        enableDisableUI(true, false);
        JavaFXUtils.openFile(new File(service.getValue()));
      });

      enableDisableUI(false, false);
      service.start();
    }
    catch(Exception e) {
      Alert dialog = createDialog(resources, Alert.AlertType.ERROR, dialogPrefixKey + ".title", dialogPrefixKey + ".header", dialogPrefixKey + ".content", null,
          null, new Object[] { e.getLocalizedMessage() }, e);
      dialog.showAndWait();
    }
    finally {
      LOGGER.debug("<<< launchService");
    }
  }

  /**
   * switch storage to Bdd
   *
   * @param ae {@link ActionEvent}
   */
  public void switchToBddStorage(ActionEvent ae) {

    LOGGER.debug(">>> switchToBddStorage");

    Alert dlg = createDialog(resources, Alert.AlertType.CONFIRMATION, "confirm.storage.confirm.title", "confirm.storage.confirm.bdd.header",
        "confirm.storage.confirm.bdd.content");
    dlg.showAndWait().ifPresent(buttonType -> {
      // if(ButtonType.OK.equals(buttonType)) {
      // in case of anything tido
      // ignored
      // }
    });

    LOGGER.debug("<<< switchToBddStorage");
  }

  @Override
  public void enableOrDisableUI(boolean enable) {
    enableDisableMenuBar(homeMenuBar, enable);
  }

  public void enableDisableUI(boolean enable, boolean mainStackToo) {
    enableOrDisableUI(enable);
    if(mainStackToo) {
      insideMainPane.setVisible(enable);
      insideMainPane.setManaged(enable);
    }
  }

}