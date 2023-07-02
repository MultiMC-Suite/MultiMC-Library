package fr.xen0xys.multimc.common.orm.enums;

@SuppressWarnings("unused")
public enum SqlKeywords {
    AUTO_INCREMENT("AUTOINCREMENT", "AUTO_INCREMENT", "SERIAL");

    private final String sqliteType;
    private final String mysqlType;
    private final String mariadbType;
    private final String postgresqlType;

    SqlKeywords(String sqliteType, String mysqlType, String mariadbType, String postgresqlType) {
        this.sqliteType = sqliteType;
        this.mysqlType = mysqlType;
        this.mariadbType = mariadbType;
        this.postgresqlType = postgresqlType;
    }

    SqlKeywords(String sqliteType, String mysqlType, String postgresqlType) {
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
}
