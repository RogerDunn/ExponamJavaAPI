package com.exponam.api.reader.filters;

public final class Or implements Filter {
    private final Filter[] filters;

    public static Filter of(Filter ... filters) {
        return new Or(filters);
    }

    private Or(Filter[] filters) {
        this.filters = filters;
    }

    public Filter[] getFilters() {
        return filters;
    }
}
