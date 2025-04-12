package dev.alortie.jorm.metadata;

import dev.alortie.jorm.core.JORM;
import dev.alortie.jorm.core.QueryBuilder;
import dev.alortie.jorm.utils.JORMAdapter;

import java.util.List;

public class TableMeta<T> {
    private final String tableName;
    ColumnMeta[] columns;
    private final Class<T> entityClass;

    final List<RelationshipMeta> relationships;

    public TableMeta(String tableName, ColumnMeta[] columns, Class<T> entityClass, List<RelationshipMeta> relationships) {
        this.tableName = tableName;
        this.columns = columns;
        this.entityClass = entityClass;
        this.relationships = relationships;
    }

    public String getTableName() {
        return tableName;
    }

    public ColumnMeta[] getColumns() {
        return columns;
    }

    public Class<T> getEntityClass() {
        return entityClass;
    }

    public ColumnMeta getPrimaryKeyColumn() {
        for (ColumnMeta column : columns) {
            if (column.isPrimaryKey()) {
                return column;
            }
        }

        throw new IllegalStateException("No primary key found for table " + tableName);
    }

    public void insert(Object object){
        JORMAdapter JORMAdapter = JORM.getInstance().getAdapter();
        JORMAdapter.insert(this, object);
    }

    public void update(Object object){
        JORMAdapter JORMAdapter = JORM.getInstance().getAdapter();
        JORMAdapter.update(this, object);
    }

    public void delete(Object object){
        JORMAdapter JORMAdapter = JORM.getInstance().getAdapter();
        JORMAdapter.delete(this, object);
    }


    public List<T> selectAll() {
        JORMAdapter JORMAdapter = JORM.getInstance().getAdapter();
        return JORMAdapter.selectAll(this);
    }

    public T selectById(Object id) {
        JORMAdapter JORMAdapter = JORM.getInstance().getAdapter();
        return (T) JORMAdapter.selectById(this, id);
    }

    public QueryBuilder<T> query() {
        return new QueryBuilder<>(this);
    }



}
