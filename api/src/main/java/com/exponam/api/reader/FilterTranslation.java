package com.exponam.api.reader;

import com.exponam.api.reader.filters.*;
import com.exponam.api.reader.filters.Filter;
import com.exponam.core.internalColumnSegmentFilters.*;
import com.google.common.collect.ImmutableMap;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

class FilterTranslation {
    static Map<Class<? extends Filter>, Function<Filter, FilterDefinition>> mappings =
            ImmutableMap.<Class<? extends Filter>, Function<Filter, FilterDefinition>>builder()
                    .put(And.class, filter -> new AndFilterDefinition(map(((And)filter).getFilters())))
                    .put(IsEqual.class, filter ->
                            new ComparisonFilterDefinition(ComparisonFilterDefinition.Kind.Equal,
                                    ((IsEqual)filter).getOperand()))
                    .put(IsGreaterThan.class, filter ->
                            new ComparisonFilterDefinition(ComparisonFilterDefinition.Kind.GreaterThan,
                                    ((IsGreaterThan)filter).getOperand()))
                    .put(IsGreaterThanOrEqual.class, filter ->
                            new ComparisonFilterDefinition(ComparisonFilterDefinition.Kind.GreaterThanOrEqual,
                                    ((IsGreaterThanOrEqual)filter).getOperand()))
                    .put(IsLessThan.class, filter ->
                            new ComparisonFilterDefinition(ComparisonFilterDefinition.Kind.LessThan,
                                    ((IsLessThan)filter).getOperand()))
                    .put(IsLessThanOrEqual.class, filter ->
                            new ComparisonFilterDefinition(ComparisonFilterDefinition.Kind.LessThanOrEqual,
                                    ((IsLessThanOrEqual)filter).getOperand()))
                    .put(IsNotEqual.class, filter ->
                            new ComparisonFilterDefinition(ComparisonFilterDefinition.Kind.NotEqual,
                                    ((IsNotEqual)filter).getOperand()))
                    .put(IsNotNull.class, filter ->
                            new NullityFilterDefinition(NullityFilterDefinition.Kind.IsNotNull))
                    .put(IsNull.class, filter ->
                            new NullityFilterDefinition(NullityFilterDefinition.Kind.IsNull))
                    .put(Or.class, filter -> new OrFilterDefinition(map(((Or)filter).getFilters())))
                    .put(StringContains.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.Contains,
                                    ((StringContains)filter).getCaseSensitive(),
                                    ((StringContains)filter).getOperand()))
                    .put(StringDoesNotContain.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.DoesNotContain,
                                    ((StringDoesNotContain)filter).getCaseSensitive(),
                                    ((StringDoesNotContain)filter).getOperand()))
                    .put(StringDoesNotEndWith.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.DoesNotEndWith,
                                    ((StringDoesNotEndWith)filter).getCaseSensitive(),
                                    ((StringDoesNotEndWith)filter).getOperand()))
                    .put(StringDoesNotStartWith.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.DoesNotStartWith,
                                    ((StringDoesNotStartWith)filter).getCaseSensitive(),
                                    ((StringDoesNotStartWith)filter).getOperand()))
                    .put(StringEndsWith.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.EndsWith,
                                    ((StringEndsWith)filter).getCaseSensitive(),
                                    ((StringEndsWith)filter).getOperand()))
                    .put(StringIsEqual.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.Equal,
                                    ((StringEndsWith)filter).getCaseSensitive(),
                                    ((StringEndsWith)filter).getOperand()))
                    .put(StringIsGreaterThan.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.GreaterThan,
                                    ((StringIsGreaterThan)filter).getCaseSensitive(),
                                    ((StringIsGreaterThan)filter).getOperand()))
                    .put(StringIsGreaterThanOrEqual.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.GreaterThanOrEqual,
                                    ((StringIsGreaterThanOrEqual)filter).getCaseSensitive(),
                                    ((StringIsGreaterThanOrEqual)filter).getOperand()))
                    .put(StringIsLessThan.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.LessThan,
                                    ((StringIsLessThan)filter).getCaseSensitive(),
                                    ((StringIsLessThan)filter).getOperand()))
                    .put(StringIsLessThanOrEqual.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.LessThanOrEqual,
                                    ((StringIsLessThanOrEqual)filter).getCaseSensitive(),
                                    ((StringIsLessThanOrEqual)filter).getOperand()))
                    .put(StringIsNotEqual.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.NotEqual,
                                    ((StringIsNotEqual)filter).getCaseSensitive(),
                                    ((StringIsNotEqual)filter).getOperand()))
                    .put(StringStartsWith.class, filter ->
                            new StringFilterDefinition(StringFilterDefinition.Kind.StartsWith,
                                    ((StringStartsWith)filter).getCaseSensitive(),
                                    ((StringStartsWith)filter).getOperand()))
                    .build();

    static FilterDefinition map(Filter filter) {
        if (!mappings.containsKey(filter.getClass()))
            throw new IllegalArgumentException(
                    String.format("Unknown filter: '%s'", filter.getClass().getCanonicalName()));
        return mappings.get(filter.getClass()).apply(filter);
    }

    private static List<FilterDefinition> map(Filter[] filters) {
        return Arrays.stream(filters).map(FilterTranslation::map).collect(Collectors.toList());
     }
}
