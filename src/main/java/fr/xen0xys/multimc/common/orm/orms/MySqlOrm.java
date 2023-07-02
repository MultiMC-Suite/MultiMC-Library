package fr.xen0xys.multimc.common.orm.orms;

import fr.xen0xys.multimc.common.orm.Orm;
import fr.xen0xys.multimc.common.orm.enums.DatabaseType;

import java.sql.SQLException;

public class MySqlOrm extends Orm {
    public MySqlOrm(String host, int port, String username, String password, String database) throws SQLException {
        super(DatabaseType.MYSQL, createUrl(host, port, database, username, password));
    }

    private static String createUrl(String host, int port, String database, String username, String password) {
        return String.format("jdbc:mariadb://%s:%d/%s?user=%s&password=%s", host, port, database, username, password);
    }
}
