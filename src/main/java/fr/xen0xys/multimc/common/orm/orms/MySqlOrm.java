package fr.xen0xys.multimc.common.orm.orms;

import fr.xen0xys.multimc.common.orm.Orm;
import fr.xen0xys.multimc.common.orm.enums.DatabaseType;

import java.sql.SQLException;

@SuppressWarnings("unused")
public class MySqlOrm extends Orm {
    public MySqlOrm(String host, int port, String username, String password, String database) throws SQLException {
        super(DatabaseType.MYSQL, String.format("jdbc:mysql://%s:%d/%s?user=%s&password=%s", host, port, database, username, password));
    }
}
