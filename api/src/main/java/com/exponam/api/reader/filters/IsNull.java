package com.exponam.api.reader.filters;

public final class IsNull implements Filter {

    public static Filter of() {
        return new IsNull();
    }

    private IsNull() {
    }
}
