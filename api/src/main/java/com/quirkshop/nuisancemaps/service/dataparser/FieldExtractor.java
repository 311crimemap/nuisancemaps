package com.quirkshop.nuisancemaps.service.dataparser;

import java.util.function.Function;

import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.Source;

/*
 * Hierarchy of parse methods:
 *
 * 1. vanilla string (simple getter method returns String; not MappingField)
 * 2. ParserStrategy method: if this is defined, prioritize it's use
 * 3. default MappingField pointer (json pointer)
 *
 * NB: baseParser is a lambda w/ typed parameters above - used to switch between
 * JsonNode or CSV Map<String, String> (which are cast in the lambda)
 */

public interface FieldExtractor<T> {
    String extract(Function<Mapping, ?> mapper, Source source, T item) throws NoSuchMethodException, SecurityException;
}
