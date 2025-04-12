package dev.alortie.jorm.utils;

import dev.alortie.jorm.annotations.Column;
import dev.alortie.jorm.annotations.Entity;
import dev.alortie.jorm.annotations.ManyToOne;
import dev.alortie.jorm.annotations.PrimaryKey;
import dev.alortie.jorm.metadata.ColumnMeta;
import dev.alortie.jorm.metadata.RelationshipMeta;
import dev.alortie.jorm.metadata.TableMeta;

import java.lang.reflect.Field;
import java.util.ArrayList;

public class ReflectionUtils {
    /**
     * Get the type of the primary key field in the entity class.
     *
     * @param entityClass The entity class to inspect.
     * @return The type of the primary key field.
     */
    private static Class<?> getPrimaryKeyType(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                return field.getType();
            }
        }
        throw new RuntimeException("No primary key found in " + entityClass.getSimpleName());
    }


    /**
     * Generate a ColumnMeta object from a field.
     *
     * @param field The field to inspect.
     * @return A ColumnMeta object representing the field.
     */
    public static ColumnMeta generateColumnMeta(Field field) {
        // Ensure the field is a column
        if (!field.isAnnotationPresent(dev.alortie.jorm.annotations.Column.class)) {
            Log.i("ReflectionUtils", "Field " + field.getName() + " is not a column - skipping");
            return null;
        }

        // Get the column annotation
        Column column = field.getAnnotation(Column.class);

        // Get column data
        String columnName = column.name();
        if (columnName.isEmpty()) { // if no name is provided, use the field name
            columnName = field.getName();
        }

        String fieldName = field.getName();
        Class<?> fieldType = field.getType();
        boolean nullable = column.nullable();
        boolean isPrimaryKey = field.isAnnotationPresent(dev.alortie.jorm.annotations.PrimaryKey.class);
        boolean autoIncrement = column.autoIncrement();
        boolean unique = column.unique();
        int length = column.length();

        boolean isForeignKey = false;
        Class<?> referencedEntity = null;

        // Check if the field is a foreign key
        if(field.isAnnotationPresent(ManyToOne.class)) {
            Log.d("ReflectionUtils-generateColumnMeta", "Field " + field.getName() + " is a foreign key - skipping");
            isForeignKey = true;
            referencedEntity = field.getType();
            fieldType = getPrimaryKeyType(referencedEntity);
        }


        return new ColumnMeta(
                columnName,
                fieldName,
                fieldType,
                nullable,
                isPrimaryKey,
                isForeignKey,
                autoIncrement,
                unique,
                length,
                referencedEntity
        );
    }

    /**
     * Generate a TableMeta object from an entity class.
     *
     * @param clazz The entity class to inspect.
     * @return A TableMeta object representing the entity class.
     */
    public static <T> TableMeta<T> generateTableMeta(Class<T> clazz) {
        if (!clazz.isAnnotationPresent(dev.alortie.jorm.annotations.Entity.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not an entity");
        }

        ArrayList<ColumnMeta> columns = new ArrayList<>();
        ArrayList<RelationshipMeta> relationships = new ArrayList<>();
        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName = entity.tableName().isEmpty() ? clazz.getSimpleName().toLowerCase() : entity.tableName();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.isAnnotationPresent(Column.class)) {
                ColumnMeta columnMeta = generateColumnMeta(field);

                if(columnMeta == null) {
                    continue; // Skip if the column is not valid
                }

                columns.add(columnMeta);
            }
            if (field.isAnnotationPresent(ManyToOne.class)) {
                ManyToOne rel = field.getAnnotation(ManyToOne.class);
                RelationshipMeta relMeta = new RelationshipMeta(
                        RelationshipMeta.Type.MANY_TO_ONE,
                        rel.foreignKey(),
                        field,
                        field.getType()
                );
                relationships.add(relMeta);
            }
        }

        return new TableMeta<T>(tableName, columns.toArray(new ColumnMeta[0]), clazz, relationships);
    }

    /**
     * Check if the entity class matches the table metadata.
     *
     * @param tableMeta The table metadata to check against.
     * @param clazz     The entity class to check.
     * @return True if the entity class matches the table metadata, false otherwise.
     */
    public static Boolean entityMatchesTableMeta(TableMeta<?> tableMeta, Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not an entity");
        }

        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName = entity.tableName().isEmpty() ? clazz.getSimpleName().toLowerCase() : entity.tableName();

        return tableMeta.getTableName().equals(tableName) && tableMeta.getColumns().length == clazz.getDeclaredFields().length;
    }

    /**
     * Get the name of the primary key column in the entity class.
     *
     * @param entityClass The entity class to inspect.
     * @return The name of the primary key column.
     */
    public static String getPrimaryKeyColumnName(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                Column col = field.getAnnotation(Column.class);
                if (col != null && !col.name().isEmpty()) return col.name();
                return field.getName(); // fallback if no @Column or no name
            }
        }
        throw new RuntimeException("No primary key found in " + entityClass.getSimpleName());
    }

    public static Object getPrimaryKeyValue(Object entity){
        for(Field field : entity.getClass().getDeclaredFields()){
            if(field.isAnnotationPresent(PrimaryKey.class)){
                field.setAccessible(true);
                try {
                    return field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Failed to access primary key", e);
                }
            }
        }
        throw new RuntimeException("No primary key found in " + entity.getClass().getSimpleName());
    }



}
