package com.quirkshop.nuisancemaps.service.dataparser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

import com.quirkshop.nuisancemaps.config.ParserStrategy;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.MappingField;
import com.quirkshop.nuisancemaps.model.Source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MapFieldExtractor implements FieldExtractor<Map<String, String>> {

    private final Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctionsMap;

    @Autowired
    public MapFieldExtractor(Map<ParserStrategy, Function<Map<String, String>, String>> parsingFunctionsMap) {
        this.parsingFunctionsMap = parsingFunctionsMap;
    }

    @Override
    public String extract(Function<Mapping, ?> mapper, Source source, Map<String, String> row)
            throws NoSuchMethodException, SecurityException {
        // parseMapping(mapper, source, row);
        Method method = mapper.getClass().getMethod("apply", Object.class);
        Class<?> returnType = method.getReturnType();

        Object mappedValue = mapper.apply(source.getMapping());

        // Vanilla String (e.g. orderKey: ":id")
        if (returnType == String.class) {
            return (String) mappedValue;
        }

        // Parsing Strategy method if exists, otherwise use Pointer expression
        MappingField result = (MappingField) mappedValue;

        if (result == null)
            return null;

        // TODO: replace
        // value = baseParser.parse(item, result);

        if (result.getParsingStrategy() != null) {
            ParserStrategy strategy = ParserStrategy.valueOf(result.getParsingStrategy());
            return parseRow((Map<String, String>) row, strategy);
        }

        return ((Map<String, String>) row).get(result.getField());
    }

    public String parseRow(Map<String, String> row, ParserStrategy strategy) {
        Function<Map<String, String>, String> parser = parsingFunctionsMap.get(strategy);
        if (parser != null) {
            return parser.apply(row);
        }
        return null;
    }

}
