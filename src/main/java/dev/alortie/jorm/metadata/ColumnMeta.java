package dev.alortie.jorm.metadata;

public class ColumnMeta {
    private String name;
    private String fieldName;
    private Class<?> type;
    private boolean nullable;
    private boolean isPrimaryKey;
    private boolean isForeignKey;
    private boolean autoIncrement;
    private boolean unique;
    Class<?> referencedEntity;

    public ColumnMeta(
            String name,
            String fieldName,
            Class<?> type,
            boolean nullable,
            boolean isPrimaryKey,
            boolean isForeignKey,
            boolean autoIncrement,
            boolean unique,
            Class<?> referencedEntity
    ) {
        this.name = name;
        this.fieldName = fieldName;
        this.type = type;
        this.nullable = nullable;
        this.isPrimaryKey = isPrimaryKey;
        this.isForeignKey = isForeignKey;
        this.autoIncrement = autoIncrement;
        this.unique = unique;
        this.referencedEntity = referencedEntity;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public Class<?> getType() {
        return type;
    }

    public void setType(Class<?> type) {
        this.type = type;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public boolean isPrimaryKey() {
        return isPrimaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        isPrimaryKey = primaryKey;
    }

    public boolean isForeignKey() {
        return isForeignKey;
    }

    public void setForeignKey(boolean foreignKey) {
        isForeignKey = foreignKey;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public boolean isUnique() {
        return unique;
    }

    public void setUnique(boolean unique) {
        this.unique = unique;
    }

    public Class<?> getReferencedEntity() {
        return referencedEntity;
    }

    public void setReferencedEntity(Class<?> referencedEntity) {
        this.referencedEntity = referencedEntity;
    }
}
