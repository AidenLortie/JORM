package dev.alortie.jorm.utils;

import dev.alortie.jorm.core.SchemaManager;
import dev.alortie.jorm.metadata.ColumnMeta;
import dev.alortie.jorm.metadata.TableMeta;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SQLUtils implements DBInterface{

    String mapJavaTypeToSQLType(Class<?> clazz) {
        if (clazz == String.class) return "VARCHAR(255)";
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

    public SQLUtils(String dbUrl, String username, String password, String databaseName) {
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
                    .append(mapJavaTypeToSQLType(column.getType()));

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
        System.out.println("Dropping table: " + tableMeta.getTableName());
    }

    @Override
    public void insert(TableMeta<?> tableMeta, Object entity) {
        Log.d("SQLUtils", "Inserting entity into table: " + tableMeta.getTableName());

        String tableName = tableMeta.getTableName();
        List<String> columnNames = new ArrayList<>();
        List<Object> values = new ArrayList<>();

        for(ColumnMeta column: tableMeta.getColumns()){
            if(column.isAutoIncrement()) continue; // Skip auto-increment columns
            columnNames.add(column.getName());

            try{
                Field field = entity.getClass().getDeclaredField(column.getFieldName());
                field.setAccessible(true);
                values.add(field.get(entity));
            } catch (NoSuchFieldException | IllegalAccessException e) {
                throw new RuntimeException("Failed to access field: " + e.getMessage());
            }
        }

        String joinedColumns = String.join(", ", columnNames);
        String placeholders = String.join(", ", columnNames.stream().map(col -> "?").toArray(String[]::new));
        String sql = "INSERT INTO " + databaseName + "." + tableName + " (" + joinedColumns + ") VALUES (" + placeholders + ")";


        Log.d("SQLUtils", "Generated SQL: " + sql);
        try{
            connect();
            PreparedStatement stmt = connection.prepareStatement(sql);
            for(int i = 0; i < values.size(); i++){
                stmt.setObject(i + 1, values.get(i));
            }
            stmt.executeUpdate();
        } catch (SQLException e){
            throw new RuntimeException("Failed to insert entity: " + e.getMessage());
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
    public Object select(TableMeta<?> tableMeta, Object entity) {
        return null;
    }

    @SuppressWarnings("unchecked")
    public <T> List<T> selectAll(TableMeta<T> tableMeta) {
        Log.d("SQLUtils", "Selecting all from table: " + tableMeta.getTableName());

        String sql = "SELECT * FROM " + databaseName + "." + tableMeta.getTableName();


        List<T> results = new ArrayList<>();

        try{
            connect();
            PreparedStatement stmt = connection.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();


            while (rs.next()) {
                T entity = (T) tableMeta.getEntityClass().getDeclaredConstructor().newInstance();
                for (ColumnMeta column : tableMeta.getColumns()) {
                    Field field = tableMeta.getEntityClass().getDeclaredField(column.getFieldName());
                    field.setAccessible(true);
                    field.set(entity, rs.getObject(column.getName()));
                }
                    results.add(entity);
                }

        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data", e);
        }
        finally {
            disconnect();
        }

        return results;
    }

    @Override
    public Object selectById(TableMeta<?> tableMeta, Object id) {
        return null;
    }

    @Override
    public Object selectByField(TableMeta<?> tableMeta, String fieldName, Object value) {
        return null;
    }

    @Override
    public Object selectByFields(TableMeta<?> tableMeta, String[] fieldNames, Object[] values) {
        return null;
    }

    @Override
    public Object selectByCondition(TableMeta<?> tableMeta, String condition, Object[] params) {
        return null;
    }
}
