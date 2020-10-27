package com.exponam.api.reader.filters;

public final class IsNotNull implements Filter {

    public static Filter of() {
        return new IsNotNull();
    }

    private IsNotNull() {
    }
}
