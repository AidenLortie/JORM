package dev.alortie.jorm.utils;

import dev.alortie.jorm.metadata.TableMeta;

import java.util.List;

public interface DBInterface {

    void connect();
    void disconnect();

    void createTable(TableMeta<?> tableMeta);

    void dropTable(TableMeta<?> tableMeta);

    void insert(TableMeta<?> tableMeta, Object entity);

    void update(TableMeta<?> tableMeta, Object entity);

    void delete(TableMeta<?> tableMeta, Object entity);

    Object select(TableMeta<?> tableMeta, Object entity);

    <T> List<T> selectAll(TableMeta<T> tableMeta);

    Object selectById(TableMeta<?> tableMeta, Object id);

    Object selectByField(TableMeta<?> tableMeta, String fieldName, Object value);

    Object selectByFields(TableMeta<?> tableMeta, String[] fieldNames, Object[] values);

    Object selectByCondition(TableMeta<?> tableMeta, String condition, Object[] params);
}
