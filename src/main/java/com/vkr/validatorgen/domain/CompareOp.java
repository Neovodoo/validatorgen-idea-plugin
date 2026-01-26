package com.vkr.validatorgen.domain;

public enum CompareOp {
    GT(">");

    private final String symbol;

    CompareOp(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }
}
