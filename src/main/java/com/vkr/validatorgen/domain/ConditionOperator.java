package com.vkr.validatorgen.domain;

public enum ConditionOperator {
    EQ("=="),
    NE("!=");

    private final String symbol;

    ConditionOperator(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}