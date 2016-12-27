package fr.perso.nfelix.app.utils.sgbd;

/**
 * This class defines all the DAL constants
 *
 * @author J.DeBouillanne
 */
public abstract class DalConstants {

  /** Date separator */
  public static final char DATE_DISPLAY_SEP = '/';
  /** Hour Separator */
  public static final char HOUR_DISPLAY_SEP = ':';

  /** DAY name pattern */
  public final static String DAY_NAME_PATTERN   = "EEEEE";
  /** DAY in month value pattern */
  public final static String DAYINMONTH_PATTERN = "dd";
  /** MONTH name pattern */
  public final static String MONTH_NAME_PATTERN = "MMMMM";
  /** MONTH pattern */
  public final static String MONTH_PATTERN      = "MM";
  /** YEAR pattern */
  public final static String YEAR_PATTERN       = "yyyy";
  /** HOUR pattern */
  public final static String HOUR_PATTERN       = "HH";
  /** MINUTE pattern */
  public final static String MINUTE_PATTERN     = "mm";
  /** SECONDE pattern */
  public final static String SECONDE_PATTERN    = "ss";

  /** approximate date (IN / EN) */
  public final static String DATE_IN            = "00";
  /** approximate date (NEAR / VERS) */
  public final static String DATE_NEAR          = "99";
  /** indeterminate date value */
  public final static String DATE_INDETERMINATE = "99999999";
  /** indeterminate year value */
  public final static String YEAR_INDETERMINATE = "9999";

  /** Empty long date */
  public static final String SAVE_DATETIME_EMPTY = "00000000000000";

  /** Short date save pattern */
  public static final String SAVE_PATTERN_DATE     = "yyyyMMdd";
  /** Long date save pattern */
  public static final String SAVE_PATTERN_DATETIME = "yyyyMMddHHmmss";

  /** Long date save pattern, with millisecond */
  public static final String SAVE_PATTERN_DATETIME_MILLI = "yyyyMMddHHmmssSSS";
  /** Hour save pattern */
  public static final String SAVE_PATTERN_TIME           = "HHmmss";
  /** Hour save pattern */
  public static final String SAVE_PATTERN_SHORT_TIME     = "HHmm";
  /** The min time of a day: 00:00:00 */
  public static final String TIME_MIN                    = "000000";
  /** The max time of a day: 23:59:59 */
  public static final String TIME_MAX                    = "235959";

  /** Date display pattern */
  public static final String DISPLAY_PATTERN_DATE       = "dd/MM/yyyy";
  /** Hour display pattern */
  public static final String DISPLAY_PATTERN_TIME       = "HH:mm:ss";
  /** Short hour display pattern */
  public static final String DISPLAY_PATTERN_SHORT_TIME = "HH:mm";

  public static final String FORMAT_DURATION_HMS            = "H:mm:ss";
  /** Date and hour display pattern */
  public static final String DISPLAY_PATTERN_DATETIME       = DISPLAY_PATTERN_DATE + " " + DISPLAY_PATTERN_TIME;
  /** Short date and hour display pattern */
  public static final String DISPLAY_PATTERN_SHORT_DATETIME = DISPLAY_PATTERN_DATE + " " + DISPLAY_PATTERN_SHORT_TIME;

  /** The input un formatted size. yyyyMMdd */
  public static final int SAVE_DATE_LENGTH       = SAVE_PATTERN_DATE.length();
  /** The input un formatted size. hhmmss */
  public static final int SAVE_TIME_LENGTH       = SAVE_PATTERN_TIME.length();
  /** The input un formatted size. hhmm */
  public static final int SAVE_SHORT_TIME_LENGTH = SAVE_PATTERN_SHORT_TIME.length();
  /** The input un formatted size. yyyyMMddhhmmss */
  public static final int SAVE_DATETIME_LENGTH   = SAVE_DATE_LENGTH + SAVE_TIME_LENGTH;

  /**
   * Defines the date save pattern managed.
   */
  public enum EnumSavePattern {
    /**
     * Short date save pattern
     */
    SAVE_PATTERN_DATE(DalConstants.SAVE_PATTERN_DATE), /**
     * Long date save pattern
     */
    SAVE_PATTERN_DATETIME(DalConstants.SAVE_PATTERN_DATETIME), /**
     * Hour save pattern
     */
    SAVE_PATTERN_TIME(DalConstants.SAVE_PATTERN_TIME);

    private String value;

    EnumSavePattern(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }

  /**
   * Defines the date display pattern managed.
   */
  public enum EnumDisplayPattern {
    /**
     * Date display pattern
     */
    DISPLAY_PATTERN_DATE(DalConstants.DISPLAY_PATTERN_DATE), /**
     * Hour display pattern
     */
    DISPLAY_PATTERN_TIME(DalConstants.DISPLAY_PATTERN_TIME), /**
     * Short hour display pattern
     */
    DISPLAY_PATTERN_SHORT_TIME(DalConstants.DISPLAY_PATTERN_SHORT_TIME), /**
     * Date and hour display pattern
     */
    DISPLAY_PATTERN_DATETIME(DalConstants.DISPLAY_PATTERN_DATETIME), /**
     * Short date and hour display pattern
     */
    DISPLAY_PATTERN_SHORT_DATETIME(DalConstants.DISPLAY_PATTERN_SHORT_DATETIME);

    private String value;

    EnumDisplayPattern(String value) {
      this.value = value;
    }

    public String getValue() {
      return value;
    }
  }
}
