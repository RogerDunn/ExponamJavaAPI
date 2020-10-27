package com.exponam.api.reader.filters;

import java.util.Objects;

public final class StringIsLessThan implements Filter {
    private final String operand;
    private final boolean caseSensitive;

    public static Filter of(String operand, boolean caseSensitive) {
        return new StringIsLessThan(operand, caseSensitive);
    }

    private StringIsLessThan(String operand, boolean caseSensitive) {
        this.operand = Objects.requireNonNull(operand, "operand");
        this.caseSensitive = caseSensitive;
    }

    public String getOperand() {
        return operand;
    }

    public boolean getCaseSensitive() {
        return caseSensitive;
    }
}
