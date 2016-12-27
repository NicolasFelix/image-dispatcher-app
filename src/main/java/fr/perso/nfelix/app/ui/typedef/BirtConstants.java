package fr.perso.nfelix.app.ui.typedef;

/**
 * define BIRT constants
 *
 * @author N.FELIX
 */
public abstract class BirtConstants {

  // birt engine bean name
  public final static String BIRT_ENGINE_BEAN = "birtEngine";

  public final static String C2_PREFIX = "c2";
  public final static String CW_PREFIX = "cw";

  // parameter names
  // city database
  public final static String C2_DRIVER   = "c2Driver";
  public final static String C2_URL      = "c2User";
  public final static String C2_USER     = "c2User";
  public final static String C2_PASSWORD = "c2Password";
  // cityweb database
  public final static String CW_DRIVER   = "cwDriver";
  public final static String CW_URL      = "cwUser";
  public final static String CW_USER     = "cwUser";
  public final static String CW_PASSWORD = "cwPassword";

  // other criteria filter
  // city2 Office
  public final static String C2_OFFICE_ID           = "c2Ofid"; // default: "select ofid from office"
  // city2 query: enable to fully filter city2 certificates (not sure it is really used...), applied on c2Count (only)
  public final static String C2_QUERY               = "c2Query"; // default: ""
  // additonal critiera applied on c2Count (only)
  public final static String C2_ADDITIONAL_CRITERIA = "c2AddedCriteria"; // default: ""

  // report options
  // skipped mention from which validation date is greater than...
  public final static String MAX_MENTION_VALIDATION_DATE = "maxMentionDateValidation"; // default: ""
  // show missing certificate IDs
  public final static String SHOW_MISSING_CERTIFICATE    = "showMissingCertificate"; // default: "True"
  // show mention differences
  public final static String SHOW_MENTION_DIFFERENCE     = "computeMentionDifference"; // default: "True"

}
