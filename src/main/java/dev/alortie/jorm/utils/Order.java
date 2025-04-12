package dev.alortie.jorm.utils;

public enum Order {
    ASC("ASC"),
    DESC("DESC");

    private final String sql;

    Order(String sql) {
        this.sql = sql;
    }

    public String getSql() {
        return sql;
    }
}
