package fr.xen0xys.multimc.common.orm;

import fr.xen0xys.multimc.common.orm.annotations.field.PrimaryKey;
import fr.xen0xys.multimc.common.orm.annotations.type.Table;
import fr.xen0xys.multimc.common.orm.enums.DatabaseType;
import fr.xen0xys.multimc.common.orm.session.SimpleSession;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public abstract class Orm {

    private final DatabaseType databaseType;
    private final Connection connection;

    public Orm(@NotNull final DatabaseType databaseType, @NotNull final String url) throws SQLException {
        this.databaseType = databaseType;
        connection = DriverManager.getConnection(url);
    }

    /**
     * Register and create a table in the database
     * @param tableClass The class of the table to register
     * @throws SQLException If an error occurs while creating the table
     */
    public <T> void registerTable(@NotNull final Class<T> tableClass) throws SQLException {
        if(!tableClass.isAnnotationPresent(Table.class))
            throw new IllegalArgumentException("Class " + tableClass.getName() + " is not an entity");
        if(!this.hasPrimaryKey(tableClass))
            throw new IllegalArgumentException("Class " + tableClass.getName() + " has no primary key");
        SimpleSession<T> session = this.createSession();
        session.createTable(tableClass);
    }

    private boolean hasPrimaryKey(@NotNull final Class<?> tableClass){
        for(final Field field: tableClass.getDeclaredFields())
            if(field.isAnnotationPresent(PrimaryKey.class))
                return true;
        return false;
    }

    /**
     * Create a session to interact with the database
     * @return The created session
     * @param <T> The type of the session
     */
    public <T> SimpleSession<T> createSession() {
        return new SimpleSession<>(this.connection, this.databaseType);
    }
}
