package fr.perso.nfelix.app.ui.typedef;

/**
 * define Consistency  constants
 *
 * @author N.FELIX
 */
public abstract class ConsistencyConstants {

  public static final String DUMMY_KEY = "DummyRoot";

  private static final String ROOT_KEY = "consistency.params.";

  public static final String ACTE_ROOT_KEY       = ROOT_KEY + "acte";
  public static final String ACTE_DUPLICATED_KEY = ACTE_ROOT_KEY + ".duplicate.properties";
  public static final String ACTE_CITY_FIELD_KEY = ACTE_ROOT_KEY + ".city.field";
  public static final String ACTE_FILIATION_KEY  = ACTE_ROOT_KEY + ".filiation";

  public static final String MENTION_ROOT_KEY            = ROOT_KEY + "mention";
  public static final String MENTION_ID_EXIST_KEY        = MENTION_ROOT_KEY + ".id.exist";
  public static final String MENTION_EXIST_KEY           = MENTION_ROOT_KEY + ".exist";
  public static final String MENTION_BODY_EXIST_KEY      = MENTION_ROOT_KEY + ".body.exist";
  public static final String MENTION_DUPLICATED_KEY      = MENTION_ROOT_KEY + ".duplicated";
  public static final String MENTION_ID_DUPLICATED_KEY   = MENTION_ROOT_KEY + ".id.duplicated";
  public static final String MENTION_APPOSITION_DATE_KEY = MENTION_ROOT_KEY + ".apposition.date";

  public static final String CRITERIA_ROOT_KEY     = ROOT_KEY + "criteria";
  public static final String CRITERIA_EXTERNAL_KEY = CRITERIA_ROOT_KEY + ".external";
  public static final String CRITERIA_NAI_KEY      = CRITERIA_ROOT_KEY + ".NAI";
  public static final String CRITERIA_DEC_KEY      = CRITERIA_ROOT_KEY + ".DEC";
  public static final String CRITERIA_MAR_KEY      = CRITERIA_ROOT_KEY + ".MAR";
  public static final String CRITERIA_REC_KEY      = CRITERIA_ROOT_KEY + ".REC";
  public static final String CRITERIA_EXTRA_KEY    = CRITERIA_ROOT_KEY + ".extra";

}
