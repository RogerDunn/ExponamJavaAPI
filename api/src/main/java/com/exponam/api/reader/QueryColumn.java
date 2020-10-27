package com.exponam.api.reader;

import com.exponam.api.reader.filters.Filter;

import java.lang.reflect.Type;
import java.util.Objects;
import java.util.Optional;

public class QueryColumn {
    private final boolean project;
    private final Type desiredType;
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    private final Optional<Filter> columnFilter;

    public QueryColumn(Type desiredType) {
        this(true, desiredType, Optional.empty());
    }

    public QueryColumn(@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
                               Optional<Filter> columnFilter) {
        this(false, Object.class, Optional.empty());
    }

    public QueryColumn(Type desiredType, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Filter> columnFilter) {
        this(true, desiredType, columnFilter);
    }

    public QueryColumn(boolean project, Type desiredType, @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
            Optional<Filter> columnFilter) {
        this.project = project;
        this.desiredType = this.project ? Objects.requireNonNull(desiredType, "desiredType") : Object.class;
        this.columnFilter = Objects.requireNonNull(columnFilter, "columnFilter");
    }

    public boolean getProject() {
        return this.project;
    }

    public Type getDesiredType() {
        return this.desiredType;
    }

    public Optional<Filter> getColumnFilter() {
        return this.columnFilter;
    }

    public String toString() {
        return this.toDebugString();
    }

    private String toDebugString() {
        return String.format("project=%s, desiredType=%s, columnFilter=%s", Boolean.valueOf(this.project).toString(), this.desiredType.getTypeName(), this.columnFilter.toString());
    }}
