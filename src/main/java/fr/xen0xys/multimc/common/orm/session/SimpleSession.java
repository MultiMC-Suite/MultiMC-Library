package fr.xen0xys.multimc.common.orm.session;

import fr.xen0xys.multimc.common.orm.annotations.field.Column;
import fr.xen0xys.multimc.common.orm.annotations.field.PrimaryKey;
import fr.xen0xys.multimc.common.orm.annotations.type.Table;
import fr.xen0xys.multimc.common.orm.enums.DatabaseType;
import fr.xen0xys.multimc.common.orm.enums.SqlKeywords;
import fr.xen0xys.multimc.common.orm.enums.SqlTypes;

import java.lang.reflect.*;
import java.sql.*;
import java.util.*;

public class SimpleSession<T> {

    private final Connection connection;

    public SimpleSession(Connection connection){
        this.connection = connection;
    }

    /**
     * Create a table in the database
     * @param tableClass The class of the table to create
     * @param databaseType The type of the database
     * @throws SQLException If an error occurs while creating the table
     */
    public void createTable(Class<?> tableClass, DatabaseType databaseType) throws SQLException {
        StringBuilder queryBuilder = new StringBuilder();
        Table tableAnnotation = tableClass.getAnnotation(Table.class);
        queryBuilder.append("CREATE TABLE IF NOT EXISTS ")
                .append(tableAnnotation.name().isEmpty() ? tableClass.getSimpleName() : tableAnnotation.name())
                .append(" (");
        for (Field field : tableClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(Column.class)) {
                Column columnAnnotation = field.getAnnotation(Column.class);
                String columnName = columnAnnotation.name().isEmpty() ? field.getName() : columnAnnotation.name();
                SqlTypes columnType = SqlTypes.fromString(field.getType());
                queryBuilder.append(columnName)
                        .append(" ")
                        .append(columnType.get(databaseType));
                if (!columnAnnotation.nullable()) {
                    queryBuilder.append(" NOT NULL");
                }
                if (columnAnnotation.unique()) {
                    queryBuilder.append(" UNIQUE");
                }
            }
            if(field.isAnnotationPresent(PrimaryKey.class)) {
                PrimaryKey primaryKeyAnnotation = field.getAnnotation(PrimaryKey.class);
                if (primaryKeyAnnotation.autoIncrement()){
                    if(!field.getType().getSimpleName().toLowerCase().contains("int"))
                        throw new IllegalArgumentException("Auto increment primary key must be an integer");
                    queryBuilder.append(" PRIMARY KEY %s".formatted(SqlKeywords.AUTO_INCREMENT.get(databaseType)));
                }else
                    queryBuilder.append(" PRIMARY KEY");
            }
            queryBuilder.append(", ");
        }
        queryBuilder.delete(queryBuilder.length() - 2, queryBuilder.length()).append(")");
        try (Statement statement = connection.createStatement()) {
            statement.execute(queryBuilder.toString());
        }
    }

    /**
     * Save an object in the database
     * @param object The object to save
     * @throws SQLException If an error occurs while saving the object
     * @throws IllegalAccessException If an error occurs while accessing the object's fields
     */
    public void save(T object) throws SQLException, IllegalAccessException {
        Table tableAnnotation = object.getClass().getAnnotation(Table.class);
        Field autoIncrementField = null;
        StringBuilder columnsBuilder = new StringBuilder();
        List<Object> values = new ArrayList<>();
        StringBuilder valuesBuilder = new StringBuilder();
        for(Field field : object.getClass().getDeclaredFields()){
            if(!field.isAnnotationPresent(Column.class))
                continue;
            Column columnAnnotation = field.getAnnotation(Column.class);
            String columnName = columnAnnotation.name().isEmpty() ? field.getName() : columnAnnotation.name();
            columnsBuilder.append(columnName).append(", ");
            if(field.isAnnotationPresent(PrimaryKey.class)){
                PrimaryKey primaryKeyAnnotation = field.getAnnotation(PrimaryKey.class);
                if(!primaryKeyAnnotation.autoIncrement())
                    continue;
                valuesBuilder.append("NULL, ");
                autoIncrementField = field;
                continue;
            }
            field.setAccessible(true);
            values.add(field.get(object));
            valuesBuilder.append("?, ");
        }
        columnsBuilder.setLength(columnsBuilder.length() - 2);
        valuesBuilder.setLength(valuesBuilder.length() - 2);
        String insertQuery = "INSERT INTO %s (%s) VALUES (%s)".formatted(tableAnnotation.name().isEmpty() ? object.getClass().getSimpleName() : tableAnnotation.name(), columnsBuilder, valuesBuilder);
        try(PreparedStatement preparedStatement = connection.prepareStatement(insertQuery)){
            for(int i = 0; i < values.size(); i++)
                preparedStatement.setObject(i + 1, values.get(i));
            preparedStatement.executeUpdate();
        }
        // Set id if auto increment
        if(Objects.isNull(autoIncrementField))
            return;
        Column columnAnnotation = autoIncrementField.getAnnotation(Column.class);
        String selectQuery = "SELECT MAX(%s) FROM %s".formatted(columnAnnotation.name().isEmpty() ? autoIncrementField.getName() : columnAnnotation.name(), tableAnnotation.name().isEmpty() ? object.getClass().getSimpleName() : tableAnnotation.name());
        ResultSet rs = connection.createStatement().executeQuery(selectQuery);
        if(!rs.next())
            throw new SQLException("No result found");
        autoIncrementField.setAccessible(true);
        autoIncrementField.set(object, rs.getInt(1));
    }

    /**
     * Update an object in the database
     * @param object The object to update
     * @throws SQLException If an error occurs while updating the object
     * @throws IllegalAccessException If an error occurs while accessing the object's fields
     */
    public void update(T object) throws SQLException, IllegalAccessException {
        Table tableAnnotation = object.getClass().getAnnotation(Table.class);
        Field primaryKeyField = Arrays.stream(object.getClass().getDeclaredFields()).filter(field -> field.isAnnotationPresent(PrimaryKey.class)).findFirst().orElse(null);
        if(Objects.isNull(primaryKeyField))
            throw new IllegalArgumentException("No primary key found");
        Column columnAnnotation = primaryKeyField.getAnnotation(Column.class);
        primaryKeyField.setAccessible(true);
        StringBuilder setBuilder = new StringBuilder();
        List<Object> values = new ArrayList<>();
        for(Field field : object.getClass().getDeclaredFields()){
            if(!field.isAnnotationPresent(Column.class))
                continue;
            Column column = field.getAnnotation(Column.class);
            String columnName = column.name().isEmpty() ? field.getName() : column.name();
            if(field.isAnnotationPresent(PrimaryKey.class))
                continue;
            field.setAccessible(true);
            values.add(field.get(object));
            setBuilder.append(columnName).append(" = ?, ");
        }
        setBuilder.setLength(setBuilder.length() - 2);
        String query = "UPDATE %s SET %s WHERE %s = ?".formatted(tableAnnotation.name().isEmpty() ? object.getClass().getSimpleName() : tableAnnotation.name(), setBuilder, columnAnnotation.name().isEmpty() ? primaryKeyField.getName() : columnAnnotation.name());
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            for (int i = 0; i < values.size(); i++)
                preparedStatement.setObject(i + 1, values.get(i));
            preparedStatement.setObject(values.size() + 1, primaryKeyField.get(object));
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Delete an object from the database
     * @param object The object to delete
     * @throws IllegalAccessException If an error occurs while accessing the object's fields
     * @throws SQLException If an error occurs while deleting the object
     */
    public void delete(T object) throws IllegalAccessException, SQLException {
        Table tableAnnotation = object.getClass().getAnnotation(Table.class);
        Field field = Arrays.stream(object.getClass().getDeclaredFields()).filter(_field -> _field.isAnnotationPresent(PrimaryKey.class)).findFirst().orElse(null);
        if(Objects.isNull(field))
            throw new IllegalArgumentException("No primary key found");
        field.setAccessible(true);
        Column columnAnnotation = field.getAnnotation(Column.class);
        Object value = field.get(object);
        String query = "DELETE FROM %s WHERE %s = ?".formatted(tableAnnotation.name().isEmpty() ? object.getClass().getSimpleName() : tableAnnotation.name(), columnAnnotation.name().isEmpty() ? field.getName() : columnAnnotation.name());
        try(PreparedStatement preparedStatement = connection.prepareStatement(query)){
            preparedStatement.setObject(1, value);
            preparedStatement.executeUpdate();
        }
    }

    /**
     * Get an object from the database
     * @param tableClass The class of the object to get
     * @param primaryKeyValue The primary key value of the object to get
     * @return The object from the database
     * @throws SQLException If an error occurs while getting the object
     */
    public T get(Class<T> tableClass, Integer primaryKeyValue) throws SQLException {
        Table tableAnnotation = tableClass.getAnnotation(Table.class);
        Field pkField = Arrays.stream(tableClass.getDeclaredFields()).filter(_field -> _field.isAnnotationPresent(PrimaryKey.class)).findFirst().orElse(null);
        if(Objects.isNull(pkField))
            throw new IllegalArgumentException("No primary key found");
        String query = "SELECT * FROM %s WHERE %s = '%s'".formatted(tableAnnotation.name().isEmpty() ? tableClass.getSimpleName() : tableAnnotation.name(), pkField.getName(), primaryKeyValue);
        ResultSet rs = connection.createStatement().executeQuery(query);
        if(!rs.next()){
            rs.close();
            return null;
        }
        T object = this.getObjectFromRS(tableClass, rs);
        rs.close();
        return object;
    }

    /**
     * Get a list of objects from the database
     * @param tableClass The class of the objects to get
     * @param column The column to filter
     * @param value The value to filter
     * @return The list of objects from the database
     * @throws SQLException If an error occurs while getting the objects
     */
    public List<T> get(Class<T> tableClass, String column, Object value) throws SQLException {
        Table tableAnnotation = tableClass.getAnnotation(Table.class);
        String query = "SELECT * FROM %s WHERE %s = '%s'".formatted(tableAnnotation.name().isEmpty() ? tableClass.getSimpleName() : tableAnnotation.name(), column, value);
        ResultSet rs = connection.createStatement().executeQuery(query);
        List<T> objects = new ArrayList<>();
        while(rs.next())
            objects.add(this.getObjectFromRS(tableClass, rs));
        rs.close();
        return objects;
    }

    /**
     * Get all objects from the database
     * @param tableClass The class of the objects to get
     * @return The list of objects from the database
     * @throws SQLException If an error occurs while getting the objects
     */
    public List<T> getAll(Class<T> tableClass) throws SQLException {
        Table tableAnnotation = tableClass.getAnnotation(Table.class);
        String query = "SELECT * FROM %s".formatted(tableAnnotation.name().isEmpty() ? tableClass.getSimpleName() : tableAnnotation.name());
        ResultSet rs = connection.createStatement().executeQuery(query);
        List<T> objects = new ArrayList<>();
        while(rs.next())
            objects.add(this.getObjectFromRS(tableClass, rs));
        rs.close();
        return objects;
    }

    @SuppressWarnings("unchecked")
    private T getObjectFromRS(Class<T> tableClass, ResultSet rs) throws RuntimeException{
        Constructor<T> constructor = (Constructor<T>) Arrays.stream(tableClass.getConstructors()).filter(_constructor -> _constructor.getParameterCount() == 0).findFirst().orElse(null);
        if(Objects.isNull(constructor))
            throw new IllegalArgumentException("No empty constructor found");
        constructor.setAccessible(true);
        T object;
        try {
            object = constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        for(Field field : tableClass.getDeclaredFields()){
            if(!field.isAnnotationPresent(Column.class))
                continue;
            Column columnAnnotation = field.getAnnotation(Column.class);
            field.setAccessible(true);
            try {
                Object _value = this.cast(field.getType(), rs.getObject(columnAnnotation.name().isEmpty() ? field.getName() : columnAnnotation.name()));
                field.set(object, _value);
            } catch (SQLException | IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return object;
    }

    private Object cast(Class<?> type, Object value){
        if (type == Integer.class || type == int.class)
            return Integer.parseInt(value.toString());
        else if (type == Double.class || type == double.class)
            return Double.parseDouble(value.toString());
        else if (type == Float.class || type == float.class)
            return Float.parseFloat(value.toString());
        else if (type == Long.class || type == long.class)
            return Long.parseLong(value.toString());
        else if (type == Short.class || type == short.class)
            return Short.parseShort(value.toString());
        else if (type == Byte.class || type == byte.class)
            return Byte.parseByte(value.toString());
        else if (type == Boolean.class || type == boolean.class)
            return Boolean.parseBoolean(value.toString());
        else if (type == String.class)
            return value.toString();
        else if (type == UUID.class)
            return UUID.fromString(value.toString());
        return type.cast(value);
    }
}
