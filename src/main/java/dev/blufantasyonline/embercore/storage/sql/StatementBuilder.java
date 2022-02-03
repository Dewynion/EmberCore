package dev.blufantasyonline.embercore.storage.sql;

import dev.blufantasyonline.embercore.EmberCore;

import java.util.Map;

public final class StatementBuilder {
    private StringBuilder statement;
    private Statement previous;

    public StatementBuilder() {
        statement = new StringBuilder();
    }

    public StatementBuilder select() {
        return select("*");
    }

    public StatementBuilder select(String... columnNames) {
        if (!checkPrevious(Statement.SELECT))
            return this;

        statement.append(Statement.SELECT.textValue).append(" ");
        for (String column : columnNames)
            statement.append(column).append(" ");
        previous = Statement.SELECT;
        return this;
    }

    public StatementBuilder from(String tableName) {
        if (!checkPrevious(Statement.FROM))
            return this;

        statement.append(Statement.FROM.textValue).append(" ").append(tableName).append(" ");
        previous = Statement.FROM;
        return this;
    }

    public StatementBuilder whereAnd(Map<String, Object> params) {
        return _where(params, Statement.AND);
    }

    public String toString() {
        return statement.toString().trim();
    }

    private boolean checkPrevious(Statement current) {
        if (previous != current.previous) {
            EmberCore.warn("StatementBuilder: %s must be proceeded by %s.",
                    current.textValue, previous == null ? "nothing" : previous.textValue);
            return false;
        }
        return true;
    }

    private StatementBuilder _where(Map<String, Object> params, Statement conjunction) {
        if (!checkPrevious(Statement.WHERE))
            return this;
        statement.append(Statement.WHERE.textValue);
        params.entrySet().forEach(entry -> {
            String col = entry.getKey();
            Object value = entry.getValue();
            String valueStr;
            if (value instanceof Number)
                valueStr = String.valueOf(value);
            else
                valueStr = "'" + value + "'";

            statement.append(col).append(" = ").append(valueStr).append(" ").append(conjunction.textValue).append(" ");
        });
        statement.delete(statement.length() - (conjunction.textValue.length() + 1), statement.length());
        previous = Statement.WHERE;
        return this;
    }

    private enum Statement {
        SELECT("SELECT", null),
        FROM("FROM", SELECT),
        WHERE("WHERE", FROM),
        AND("AND", WHERE),
        OR("OR", WHERE),
        NOT("NOT", WHERE);

        String textValue;
        Statement previous;

        Statement(String textValue, Statement previous) {
            this.textValue = textValue;
            this.previous = previous;
        }
    }
}
