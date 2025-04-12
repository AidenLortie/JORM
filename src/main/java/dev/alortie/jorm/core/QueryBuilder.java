package dev.alortie.jorm.core;

import dev.alortie.jorm.metadata.TableMeta;
import dev.alortie.jorm.utils.Order;

import java.util.ArrayList;
import java.util.List;

public class QueryBuilder<T>  {
    private TableMeta<T> table;
    private List<String> conditions = new ArrayList<>();
    private List<Object> params = new ArrayList<>();
    private String orderBy = null;
    private int limit = -1;
    private int offset = -1;

    public QueryBuilder(TableMeta<T> table){
        this.table = table;
    }

    public QueryBuilder<T> where(String field, String op, Object value) {
        conditions.add(field + " " + op + " ?");
        params.add(value);
        return this;
    }

    public QueryBuilder<T> and(String field, String op, Object value){
        return where(field, op, value);
    }

    public QueryBuilder<T> orderBy(String field, Order order){
        this.orderBy = field + " " + order.name();
        return this;
    }

    public QueryBuilder<T> limit(int limit){
        this.limit = limit;
        return this;
    }

    public QueryBuilder<T> offset(int offset){
        this.offset = offset;
        return this;
    }

    public List<T> findAll() {
        return JORM.getInstance().getAdapter().selectWhere(table, buildWhereClause(), params.toArray());
    }

    private String buildWhereClause() {
        if (conditions.isEmpty()) return "1=1";
        String whereClause = String.join(" AND ", conditions);
        if (orderBy != null) {
            whereClause += " ORDER BY " + orderBy;
        }
        if (limit != -1) {
            whereClause += " LIMIT " + limit;
        }
        if (offset != -1) {
            whereClause += " OFFSET " + offset;
        }
        return whereClause;
    }

}
