package fr.xen0xys.multimc.common.orm.enums;

@SuppressWarnings("unused")
public enum SqlTypes {
    TINYINT("TINYINT", "TINYINT", "SMALLINT"),
    SMALLINT("SMALLINT", "SMALLINT", "SMALLINT"),
    INTEGER("INTEGER", "INT", "INT"),
    BIGINT("BIGINT", "BIGINT", "BIGINT"),
    REAL("REAL", "FLOAT", "REAL"),
    DOUBLE("DOUBLE", "DOUBLE", "DOUBLE PRECISION"),
    DECIMAL("DECIMAL", "DECIMAL", "DECIMAL"),
    NUMERIC("NUMERIC", "NUMERIC", "NUMERIC"),
    TEXT("TEXT", "VARCHAR(255)", "TEXT"),
    BOOLEAN("INTEGER", "BIT", "BOOLEAN"),
    TIMESTAMP("TEXT", "DATETIME", "TIMESTAMP"),
    UUID("TEXT", "VARCHAR(36)", "UUID");

    private final String sqliteType;
    private final String mysqlType;
    private final String mariadbType;
    private final String postgresqlType;

    SqlTypes(String sqliteType, String mysqlType, String mariadbType, String postgresqlType) {
        this.sqliteType = sqliteType;
        this.mysqlType = mysqlType;
        this.mariadbType = mariadbType;
        this.postgresqlType = postgresqlType;
    }

    SqlTypes(String sqliteType, String mysqlType, String postgresqlType) {
        this.sqliteType = sqliteType;
        this.mysqlType = mysqlType;
        this.mariadbType = mysqlType;
        this.postgresqlType = postgresqlType;
    }

    public String get(DatabaseType databaseType) {
        return switch (databaseType) {
            case SQLITE -> sqliteType;
            case MYSQL -> mysqlType;
            case MARIADB -> mariadbType;
            case POSTGRESQL -> postgresqlType;
        };
    }

    public static SqlTypes fromString(Class<?> type) {
        return switch (type.getSimpleName()) {
            case "byte", "Byte" -> SqlTypes.TINYINT;
            case "short", "Short" -> SqlTypes.SMALLINT;
            case "int", "Integer" -> SqlTypes.INTEGER;
            case "long", "Long" -> SqlTypes.BIGINT;
            case "float", "Float" -> SqlTypes.REAL;
            case "double", "Double" -> SqlTypes.DOUBLE;
            case "BigDecimal", "BigInteger" -> SqlTypes.DECIMAL;
            case "String" -> SqlTypes.TEXT;
            case "boolean", "Boolean" -> SqlTypes.BOOLEAN;
            case "LocalDateTime" -> SqlTypes.TIMESTAMP;
            case "UUID" -> SqlTypes.UUID;
            default -> throw new IllegalArgumentException("Unsupported type: " + type.getName());
        };
    }
}
