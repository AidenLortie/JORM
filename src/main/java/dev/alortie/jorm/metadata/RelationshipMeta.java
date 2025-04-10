package dev.alortie.jorm.metadata;

import java.lang.reflect.Field;

public class RelationshipMeta {
    public enum Type {
        MANY_TO_ONE,
        ONE_TO_MANY,
        MANY_TO_MANY
    }

    private final Type type;
    private final String foreignKeyFieldName;
    private final Field field;
    private final Class<?> targetEntityClass;

    public RelationshipMeta(Type type, String foreignKeyFieldName, Field field, Class<?> targetEntityClass) {
        this.type = type;
        this.foreignKeyFieldName = foreignKeyFieldName;
        this.field = field;
        this.targetEntityClass = targetEntityClass;
    }

    public Type getType() {
        return type;
    }

    public String getForeignKeyFieldName() {
        return foreignKeyFieldName;
    }

    public Field getField() {
        return field;
    }

    public Class<?> getTargetEntityClass() {
        return targetEntityClass;
    }

}
