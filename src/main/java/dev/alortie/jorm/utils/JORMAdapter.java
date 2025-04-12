package dev.alortie.jorm.utils;

import dev.alortie.jorm.metadata.TableMeta;

import java.util.List;

public interface JORMAdapter {

    void connect();

    void disconnect();

    void createTable(TableMeta<?> tableMeta);

    void dropTable(TableMeta<?> tableMeta);

    void insert(TableMeta<?> tableMeta, Object entity);

    void update(TableMeta<?> tableMeta, Object entity);

    void delete(TableMeta<?> tableMeta, Object entity);


    <T> List<T> selectAll(TableMeta<T> tableMeta);

    <T> T selectById(TableMeta<T> tableMeta, Object primaryKeyValue);

    <T> List<T> selectWhere(TableMeta<T> tableMeta, String whereClause, Object... params);

}