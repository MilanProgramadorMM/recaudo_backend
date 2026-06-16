package com.recaudo.api.infrastructure.helper.sql;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

import java.util.ArrayList;
import java.util.List;

    public class QueryBuilder {

    private final StringBuilder sql;
    private final MapSqlParameterSource params;
    private final List<Filter> filters;

    public QueryBuilder(String baseSql) {
        this.sql = new StringBuilder(baseSql);
        this.params = new MapSqlParameterSource();
        this.filters = new ArrayList<>();
    }

    public QueryBuilder addFilter(String condition, String param, Object value) {
        filters.add(new Filter(condition, param, value));
        return this;
    }

    public QueryBuilder buildWhere() {
        sql.append(" WHERE 1=1");

        filters.stream()
                .filter(f -> f.value() != null)
                .forEach(f -> {
                    if (!f.condition().equalsIgnoreCase(""))
                        sql.append(" AND ").append(f.condition());
                    params.addValue(f.param(), f.value());
                });

        return this;
    }

    public QueryBuilder append(String extraSql) {
        sql.append(" ").append(extraSql);
        return this;
    }

    public String getSql() {
        return sql.toString();
    }

    public MapSqlParameterSource getParams() {
        return params;
    }

}
