package com.exponam.api.reader.filters;

import java.util.Objects;

public final class IsGreaterThan implements Filter {
    private final Object operand;

    public static Filter of(Object operand) {
        return new IsGreaterThan(operand);
    }

    private IsGreaterThan(Object operand) {
        this.operand = Objects.requireNonNull(operand, "operand");
    }

    public Object getOperand() {
        return operand;
    }
}
