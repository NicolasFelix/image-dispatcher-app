package fr.perso.nfelix.app.utils.sgbd;

import java.util.HashMap;
import java.util.Map;

/**
 * Enum representing a database type, such as DB2 or oracle. The type also contains a product name, which is expected to
 * be the same as the product name provided by the database driver's metadata.
 *
 * @author N.FELIX
 */
public enum DatabaseType {
  /** Unknown */
  UNKNOWN("Unknown"), /** mysql */
  MYSQL("MySQL"), /** oracle */
  ORACLE("Oracle"), /** db2 */
  DB2("DB2"), /** derby */
  DERBY("Apache Derby"), /** db2 zos */
  DB2ZOS("DB2ZOS"), /** hsql */
  HSQL("HSQL Database Engine"), /** sqlserver */
  SQLSERVER("Microsoft SQL Server"), /** poqstgreSql */
  POSTGRES("PostgreSQL"), /** sybase */
  SYBASE("Sybase"), /** h2 */
  H2("H2");

  private static final String DATABASE_PRODUCT_VERSION_METHOD = "getDatabaseProductVersion";
  private static final String DATABASE_PRODUCT_NAME_METHOD    = "getDatabaseProductName";

  private static final Map<String, DatabaseType> nameMap;

  static {
    nameMap = new HashMap<>();
    for(DatabaseType type : values()) {
      nameMap.put(type.getProductName(), type);
    }
  }

  // A description is necessary due to the nature of database descriptions
  // in metadata.
  private final String productName;

  DatabaseType(String productName) {
    this.productName = productName;
  }

  /**
   * gets product name
   *
   * @return product name
   */
  public String getProductName() {
    return productName;
  }

  public String getValue() {
    return getProductName();
  }

  /**
   * Static method to obtain a DatabaseType from the provided product name.
   *
   * @param productName product name
   * @return DatabaseType for given product name.
   * @throws IllegalArgumentException if none is found.
   */
  public static DatabaseType fromProductName(String productName) {
    if(!nameMap.containsKey(productName)) {
      throw new IllegalArgumentException("DatabaseType not found for product name: [" + productName + "]");
    }
    return nameMap.get(productName);
  }

  private static String commonDatabaseName(String source) {
    String name = source;
    if(source != null && source.startsWith("DB2")) {
      name = "DB2";
    }
    else if("Sybase SQL Server".equals(source) || "Adaptive Server Enterprise".equals(source) || "ASE".equals(source) || "sql server"
        .equalsIgnoreCase(source)) {
      name = "Sybase";
    }
    return name;
  }
}
