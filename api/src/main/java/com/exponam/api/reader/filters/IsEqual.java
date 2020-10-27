package com.exponam.api.reader.filters;

import java.util.Objects;

public final class IsEqual implements Filter {
    private final String operand;

    public static Filter of(String operand) {
        return new IsEqual(operand);
    }

    private IsEqual(String operand) {
        this.operand = Objects.requireNonNull(operand, "operand");
    }

    public String getOperand() {
        return operand;
    }
}
