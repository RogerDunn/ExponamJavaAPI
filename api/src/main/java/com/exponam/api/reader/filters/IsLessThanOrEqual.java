package com.exponam.api.reader.filters;

import java.util.Objects;

public final class IsLessThanOrEqual implements Filter {
    private final Object operand;

    public static Filter of(Object operand) {
        return new IsLessThanOrEqual(operand);
    }

    private IsLessThanOrEqual(Object operand) {
        this.operand = Objects.requireNonNull(operand, "operand");
    }

    public Object getOperand() {
        return operand;
    }
}
