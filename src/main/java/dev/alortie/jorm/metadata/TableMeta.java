package dev.alortie.jorm.metadata;

import dev.alortie.jorm.core.ORMEngine;
import dev.alortie.jorm.utils.DBInterface;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
        DBInterface dbInterface = ORMEngine.getInstance().getDbInterface();
        dbInterface.insert(this, object);
    }

    public void update(Object object){
        DBInterface dbInterface = ORMEngine.getInstance().getDbInterface();
        dbInterface.update(this, object);
    }

    public void delete(Object object){
        DBInterface dbInterface = ORMEngine.getInstance().getDbInterface();
        dbInterface.delete(this, object);
    }


    @SuppressWarnings("unchecked")
    public List<T> selectAll() {
        DBInterface dbInterface = ORMEngine.getInstance().getDbInterface();
        return dbInterface.selectAll(this);
    }



}
