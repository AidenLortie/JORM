package dev.alortie.jorm.utils;

import dev.alortie.jorm.annotations.PrimaryKey;
import dev.alortie.jorm.core.JORM;
import dev.alortie.jorm.core.SchemaManager;
import dev.alortie.jorm.metadata.ColumnMeta;
import dev.alortie.jorm.metadata.TableMeta;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SQLBuilder implements JORMAdapter {

    String mapJavaTypeToSQLType(ColumnMeta column) {
        Class<?> clazz = column.getType();
        if (clazz == String.class) {
            if (column.getLength() > 0) {
                return "VARCHAR(" + column.getLength() + ")";
            } else {
                return "TEXT";
            }
        }
        if (clazz == int.class || clazz == Integer.class) return "INT";
        if (clazz == boolean.class || clazz == Boolean.class) return "BOOLEAN";
        if (clazz == long.class || clazz == Long.class) return "BIGINT";
        if (clazz == double.class || clazz == Double.class) return "DOUBLE";
        if (clazz == float.class || clazz == Float.class) return "FLOAT";
        if (clazz == short.class || clazz == Short.class) return "SMALLINT";
        if (clazz == byte[].class) return "BLOB";
        if (clazz == java.time.LocalDate.class) return "DATE";
        if (clazz == java.time.LocalDateTime.class) return "DATETIME";

        // Add more as needed
        throw new IllegalArgumentException("Unsupported type: " + clazz.getName());
    }


    Connection connection;

    private final String dbUrl;
    private final String username;
    private final String password;
    private final String databaseName;

    public SQLBuilder(String dbUrl, String username, String password, String databaseName) {
        this.dbUrl = dbUrl;
        this.username = username;
        this.password = password;
        this.databaseName = databaseName;
    }

    private String generateCreateTableSQL(TableMeta<?> tableMeta) {
        StringBuilder sql = new StringBuilder("CREATE TABLE IF NOT EXISTS ");

        sql.append(databaseName).append(".").append(tableMeta.getTableName()).append(" (");

        int i = 0;
        for (ColumnMeta column: tableMeta.getColumns()) {
            sql.append(column.getName())
                    .append(" ")
                    .append(mapJavaTypeToSQLType(column));

            if (column.isPrimaryKey()   )   sql.append(" PRIMARY KEY"   );
            if (column.isAutoIncrement())   sql.append(" AUTO_INCREMENT");
            if (column.isNullable()     )   sql.append(" NOT NULL"      );
            if (column.isUnique()       )   sql.append(" UNIQUE"        );

            if (i++ < tableMeta.getColumns().length - 1)
                sql.append(", ");

        }

        // Add foreign key constraints
        for (ColumnMeta column : tableMeta.getColumns()){
            if (column.isForeignKey()) {
                String pk = ReflectionUtils.getPrimaryKeyColumnName(column.getReferencedEntity());
                String fk = ", FOREIGN KEY (" + column.getName() + ") REFERENCES " +
                        SchemaManager.getTableNameForEntity(column.getReferencedEntity()) +
                        "(" + pk + ")";
                sql.append(fk);
            }
        }

        sql.append(");");

        Log.d("SQLUtils", "Generated SQL: " + sql.toString());

        return sql.toString();
    }


    @Override
    public void connect() {
        try{
            this.connection = DriverManager.getConnection(
                dbUrl,
                username,
                password
            );
        } catch (SQLException e) {
            throw new RuntimeException("Failed to connect to the database: " + e.getMessage());
        }
    }

    @Override
    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                throw new RuntimeException("Failed to disconnect from the database: " + e.getMessage());
            }
        }
    }

    @Override
    public void createTable(TableMeta<?> tableMeta) {
        Log.d("SQLUtils", "Creating table: " + tableMeta.getTableName());
        String sql = generateCreateTableSQL(tableMeta);

        try {
            connect();
            connection.createStatement().execute(sql);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create table: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    @Override
    public void dropTable(TableMeta<?> tableMeta) {
        throw new UnsupportedOperationException("Opperation not supported yet");
    }

    @Override
    public void insert(TableMeta<?> tableMeta, Object entity) {
        Log.d("SQLUtils", "Inserting entity into table: " + tableMeta.getTableName());

        String tableName = tableMeta.getTableName();
        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for (ColumnMeta column : tableMeta.getColumns()) {
            if (column.isAutoIncrement()) continue; // skip auto-increment fields

            try {
                Field field = entity.getClass().getDeclaredField(column.getFieldName());
                field.setAccessible(true);

                if (column.isForeignKey()) {
                    Object relatedEntity = field.get(entity);
                    if (relatedEntity != null) {
                        TableMeta<?> relatedMeta = JORM.getInstance().repository(relatedEntity.getClass());
                        insert(relatedMeta, relatedEntity); // recursively insert FK object

                        Object fkValue = ReflectionUtils.getPrimaryKeyValue(relatedEntity);
                        columnNames.add(column.getName());
                        values.add(fkValue);
                    } else if (!column.isNullable()) {
                        throw new RuntimeException("Non-nullable foreign key is null: " + column.getName());
                    }
                } else {
                    columnNames.add(column.getName());
                    values.add(field.get(entity));
                }

            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + column.getFieldName(), e);
            }
        }

        String joinedColumns = String.join(", ", columnNames);
        String placeholders = String.join(", ", Collections.nCopies(columnNames.size(), "?"));
        String sql = "INSERT INTO " + databaseName + "." + tableName +
                " (" + joinedColumns + ") VALUES (" + placeholders + ")";

        Log.d("SQLUtils", "Generated SQL: " + sql);

        try {
            connect();
            PreparedStatement stmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (int i = 0; i < values.size(); i++) {
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();
            ResultSet rs = stmt.getGeneratedKeys();
            if(rs.next()) {
                Object generatedId = rs.getObject(1);
                for (Field field : entity.getClass().getDeclaredFields()){
                    if (field.isAnnotationPresent(PrimaryKey.class)) {
                        Class<?> pkType = field.getType();

                        field.setAccessible(true);

                        if (pkType == int.class || pkType == Integer.class) {
                            field.set(entity, ((Number) generatedId).intValue());
                        } else if (pkType == long.class || pkType == Long.class) {
                            field.set(entity, ((Number) generatedId).longValue());
                        } else if (pkType == String.class) {
                            field.set(entity, generatedId.toString());
                        } else {
                            throw new RuntimeException("Unsupported primary key type: " + pkType.getName());
                        }
                        break;
                    }
                }
            }
        } catch (SQLException | IllegalAccessException e) {
            throw new RuntimeException("Failed to insert entity into " + tableName, e);
        } finally {
            disconnect();
        }
    }


    @Override
    public void update(TableMeta<?> tableMeta, Object entity) {
        Log.d("SQLUtils", "Updating entity in table: " + tableMeta.getTableName());
        String tableName = tableMeta.getTableName();
        List<String> assignments = new ArrayList<>();
        List<Object> values = new ArrayList<>();
        Object primaryKeyValue = null;

        try {
            for(ColumnMeta column : tableMeta.getColumns()){
                Field field = entity.getClass().getDeclaredField(column.getFieldName());
                field.setAccessible(true);
                Object value = field.get(entity);

                if(column.isPrimaryKey()){
                    primaryKeyValue = value;
                    continue; // Skip primary key column for assignment
                }

                if(!column.isAutoIncrement()) {
                    assignments.add(column.getName() + " = ?");
                    values.add(value);
                }
            }

            if(primaryKeyValue == null) {
                throw new RuntimeException("Primary key value is null");
            }

            String setClause = String.join(", ", assignments);
            String sql = "UPDATE " + databaseName + "." + tableName + " SET " + setClause + " WHERE " + tableMeta.getPrimaryKeyColumn().getName() + " = ?";

                connect();
                PreparedStatement stmt = connection.prepareStatement(sql);
                int i = 1;
                for(Object value : values){
                    stmt.setObject(i++, value);
                }

                stmt.setObject(i, primaryKeyValue);
                stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to update entity: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    @Override
    public void delete(TableMeta<?> tableMeta, Object entity) {
        Log.d("SQLUtils", "Deleting entity from table: " + tableMeta.getTableName());
        Object primaryKeyValue = null;

        try {
            for(ColumnMeta column : tableMeta.getColumns()){
                Field field = entity.getClass().getDeclaredField(column.getFieldName());
                field.setAccessible(true);
                if(column.isPrimaryKey()){
                    primaryKeyValue = field.get(entity);
                    break;
                }
            }

            if(primaryKeyValue == null) {
                throw new RuntimeException("Primary key value is null");
            }

            String sql = "DELETE FROM " + databaseName + "." + tableMeta.getTableName() + " WHERE " + tableMeta.getPrimaryKeyColumn().getName() + " = ?";
            connect();
            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setObject(1, primaryKeyValue);
            stmt.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException("Failed to delete entity: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    @Override
    public <T> T selectById(TableMeta<T> tableMeta, Object primaryKeyValue) {
        Log.d("SQLUtils", "Selecting entity from table: " + tableMeta.getTableName());
        try {
            connect();

            String sql = "SELECT * FROM " + databaseName + "." + tableMeta.getTableName() +
                    " WHERE " + tableMeta.getPrimaryKeyColumn().getName() + " = ?";

            PreparedStatement stmt = connection.prepareStatement(sql);
            stmt.setObject(1, primaryKeyValue);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                T entity = tableMeta.getEntityClass().getDeclaredConstructor().newInstance();

                for (ColumnMeta column : tableMeta.getColumns()) {
                    Field field = tableMeta.getEntityClass().getDeclaredField(column.getFieldName());
                    field.setAccessible(true);

                    if (column.isForeignKey()) {
                        Log.d("SQLUtils-Select-FK", "Processing foreign key: " + column.getName() + " -> " + column.getReferencedEntity().getSimpleName());
                        Object fkValue = rs.getObject(column.getName());
                        if (fkValue != null) {
                            TableMeta<?> relatedMeta = JORM.getInstance().repository(column.getReferencedEntity());
                            Object relatedEntity = JORM.getInstance().getAdapter().selectById(relatedMeta, fkValue);
                            Log.d("SQLUtils-Select-FK", "Related entity: " + relatedEntity.getClass().getSimpleName() + " with ID: " + fkValue);
                            field.set(entity, relatedEntity);
                        } else {
                            Log.e("SQLUtils-Select-FK", "Foreign key value is null for column: " + column.getName() + " in entity: " + tableMeta.getEntityClass().getSimpleName() + ". Setting field to null.");
                            field.set(entity, null);
                        }
                    } else {
                        field.set(entity, rs.getObject(column.getName()));
                    }
                }

                return entity;
            } else {
                return null; // No entity found
            }


        } catch (IllegalAccessException e) {
            throw new RuntimeException("Failed to access primary key field: " + e.getMessage());
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("Primary key field not found: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("Failed to select entity: " + e.getMessage());
        } finally {
            disconnect();
        }
    }

    public <T> List<T> selectAll(TableMeta<T> tableMeta) {
        Log.d("SQLUtils", "Selecting all from table: " + tableMeta.getTableName());

        String sql = "SELECT * FROM " + databaseName + "." + tableMeta.getTableName();
        List<T> results = new ArrayList<>();

        try {
            connect();
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                T entity = tableMeta.getEntityClass().getDeclaredConstructor().newInstance();

                for (ColumnMeta column : tableMeta.getColumns()) {
                    Field field = tableMeta.getEntityClass().getDeclaredField(column.getFieldName());
                    field.setAccessible(true);

                    if (column.isForeignKey()) {
                        Object fkValue = rs.getObject(column.getName());
                        if (fkValue != null) {
                            TableMeta<?> relatedMeta = JORM.getInstance().repository(column.getReferencedEntity());
                            Object relatedEntity = JORM.getInstance().getAdapter().selectById(relatedMeta, fkValue);
                            field.set(entity, relatedEntity);
                        } else {
                            field.set(entity, null);
                        }
                    } else {
                        field.set(entity, rs.getObject(column.getName()));
                    }
                }

                results.add(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        } finally {
            disconnect();
        }

        return results;
    }

    @Override
    public <T> List<T> selectWhere(TableMeta<T> tableMeta, String whereClause, Object... params) {
        Log.d("SQLUtils", "Selecting with where clause: " + whereClause);
        String sql = "SELECT * FROM " + databaseName + "." + tableMeta.getTableName() + " WHERE " + whereClause;
        List<T> results = new ArrayList<>();

        try {
            connect();
            PreparedStatement stmt = connection.prepareStatement(sql);
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                T entity = tableMeta.getEntityClass().getDeclaredConstructor().newInstance();

                for (ColumnMeta column : tableMeta.getColumns()) {
                    Field field = tableMeta.getEntityClass().getDeclaredField(column.getFieldName());
                    field.setAccessible(true);

                    if (column.isForeignKey()) {
                        Object fkValue = rs.getObject(column.getName());
                        if (fkValue != null) {
                            TableMeta<?> relatedMeta = JORM.getInstance().repository(column.getReferencedEntity());
                            Object relatedEntity = JORM.getInstance().getAdapter().selectById(relatedMeta, fkValue);
                            field.set(entity, relatedEntity);
                        } else {
                            field.set(entity, null);
                        }
                    } else {
                        field.set(entity, rs.getObject(column.getName()));
                    }
                }

                results.add(entity);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        } finally {
            disconnect();
        }

        return results.isEmpty() ? null : results;
    }
}
