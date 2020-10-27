package com.exponam.api.reader.filters;

public final class And implements Filter {
    private final Filter[] filters;

    public static Filter of(Filter ... filters) {
        return new And(filters);
    }

    private And(Filter[] filters) {
        this.filters = filters;
    }

    public Filter[] getFilters() {
        return filters;
    }
}
