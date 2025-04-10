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

    private static Class<?> getPrimaryKeyType(Class<?> entityClass) {
        for (Field field : entityClass.getDeclaredFields()) {
            if (field.isAnnotationPresent(PrimaryKey.class)) {
                return field.getType();
            }
        }
        throw new RuntimeException("No primary key found in " + entityClass.getSimpleName());
    }


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
        boolean isForeignKey = false;
        Class<?> referencedEntity = null;

        if(field.isAnnotationPresent(ManyToOne.class)) {
            isForeignKey = true;
            referencedEntity = field.getType();
            fieldType = getPrimaryKeyType(referencedEntity);
        }

        boolean autoIncrement = column.autoIncrement();
        boolean unique = column.unique();


        return new ColumnMeta(
                columnName,
                fieldName,
                fieldType,
                nullable,
                isPrimaryKey,
                isForeignKey,
                autoIncrement,
                unique,
                referencedEntity
        );
    }

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

    public static Boolean entityMatchesTableMeta(TableMeta<?> tableMeta, Class<?> clazz) {
        if (!clazz.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class " + clazz.getName() + " is not an entity");
        }

        Entity entity = clazz.getAnnotation(Entity.class);
        String tableName = entity.tableName().isEmpty() ? clazz.getSimpleName().toLowerCase() : entity.tableName();

        return tableMeta.getTableName().equals(tableName) && tableMeta.getColumns().length == clazz.getDeclaredFields().length;
    }

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



}
