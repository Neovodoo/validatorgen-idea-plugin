package com.vkr.validatorgen.domain;

import java.util.Arrays;
import java.util.Optional;

public enum CompareOp {
    GT(">"),
    LT("<");

    private final String symbol;

    CompareOp(String symbol) {
        this.symbol = symbol;
    }

    public String getSymbol() {
        return symbol;
    }

    public static Optional<CompareOp> fromInput(String raw) {
        if (raw == null) return Optional.empty();
        String value = raw.trim();
        if (value.isEmpty()) return Optional.empty();

        return Arrays.stream(values())
                .filter(op -> op.symbol.equals(value) || op.name().equalsIgnoreCase(value))
                .findFirst();
    }

    @Override
    public String toString() {
        return symbol;
    }
}
