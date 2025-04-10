package dev.alortie.jorm.utils;

import dev.alortie.jorm.annotations.Column;
import dev.alortie.jorm.annotations.Entity;
import dev.alortie.jorm.metadata.ColumnMeta;
import dev.alortie.jorm.metadata.TableMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ReflectionUtils {

    public static ColumnMeta generateColumnMeta(Field field) {
        if (!field.isAnnotationPresent(dev.alortie.jorm.annotations.Column.class)) {
            throw new IllegalArgumentException("Field " + field.getName() + " is not a column");
        }

        Column column = field.getAnnotation(Column.class);
        String columnName = column.name();
        if (columnName.isEmpty()) {
            columnName = field.getName();
        }
        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        boolean nullable = column.nullable();
        boolean isPrimaryKey = field.isAnnotationPresent(dev.alortie.jorm.annotations.PrimaryKey.class);
        boolean autoIncrement = column.autoIncrement();
        boolean unique = column.unique();


        return new ColumnMeta(
                columnName,
                fieldName,
                fieldType,
                nullable,
                isPrimaryKey,
                autoIncrement,
                unique
        );
    }

    public static <T> TableMeta<T> generateTableMeta(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(dev.alortie.jorm.annotations.Entity.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not an entity");
        }

        ArrayList<ColumnMeta> columns = new ArrayList<>();
        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName = entity.tableName().isEmpty() ? clazz.getSimpleName().toLowerCase() : entity.tableName();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                ColumnMeta columnMeta = generateColumnMeta(field);
                columns.add(columnMeta);
            }
        }

        return new TableMeta<T>(tableName, columns.toArray(new ColumnMeta[0]), clazz);
    }

    public static Boolean entityMatchesTableMeta(TableMeta<?> tableMeta, Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not an entity");
        }

        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName = entity.tableName().isEmpty() ? clazz.getSimpleName().toLowerCase() : entity.tableName();

        return tableMeta.getTableName().equals(tableName) && tableMeta.getColumns().length == clazz.getDeclaredFields().length;
    }


}
