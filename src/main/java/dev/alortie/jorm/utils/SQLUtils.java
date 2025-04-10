package dev.alortie.jorm.utils;

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
        for (int i = 0; i < tableMeta.getColumns().length; i++) {
            sql.append(tableMeta.getColumns()[i].getName()).append(" ").append(mapJavaTypeToSQLType(tableMeta.getColumns()[i].getType()));
            if (tableMeta.getColumns()[i].isPrimaryKey()) {
                sql.append(" PRIMARY KEY");
            }
            if (i < tableMeta.getColumns().length - 1) {
                sql.append(", ");
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
        System.out.println("Updating entity in table: " + tableMeta.getTableName());
        System.out.println("Entity: " + entity);
    }

    @Override
    public void delete(TableMeta<?> tableMeta, Object entity) {
        System.out.println("Deleting entity from table: " + tableMeta.getTableName());
        System.out.println("Entity: " + entity);
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
