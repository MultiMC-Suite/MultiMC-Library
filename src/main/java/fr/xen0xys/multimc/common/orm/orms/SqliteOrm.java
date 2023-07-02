package fr.xen0xys.multimc.common.orm.orms;

import fr.xen0xys.multimc.common.orm.Orm;
import fr.xen0xys.multimc.common.orm.enums.DatabaseType;

import java.sql.SQLException;

public class SqliteOrm extends Orm {
    public SqliteOrm(String databaseName) throws SQLException {
        super(DatabaseType.SQLITE, "jdbc:sqlite:%s".formatted(databaseName));
    }
}
