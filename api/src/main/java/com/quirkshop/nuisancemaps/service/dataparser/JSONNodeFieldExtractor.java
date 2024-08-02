package com.quirkshop.nuisancemaps.service.dataparser;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.function.Function;

import com.fasterxml.jackson.databind.JsonNode;
import com.quirkshop.nuisancemaps.config.ParserStrategy;
import com.quirkshop.nuisancemaps.model.Mapping;
import com.quirkshop.nuisancemaps.model.MappingField;
import com.quirkshop.nuisancemaps.model.Source;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JSONNodeFieldExtractor implements FieldExtractor<JsonNode> {

    private final Map<ParserStrategy, Function<JsonNode, String>> parsingFunctionsJSON;

    @Autowired
    public JSONNodeFieldExtractor(Map<ParserStrategy, Function<JsonNode, String>> parsingFunctionsJSON) {
        this.parsingFunctionsJSON = parsingFunctionsJSON;
    }

    @Override
    public String extract(Function<Mapping, ?> mapper, Source source, JsonNode item)
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
            return parseNode((JsonNode) item, strategy);
        }

        return ((JsonNode) item).at(result.getPointer()).asText();
    }

    public String parseNode(JsonNode item, ParserStrategy strategy) {
        Function<JsonNode, String> parser = parsingFunctionsJSON.get(strategy);
        if (parser != null) {
            return parser.apply(item);
        }
        return null;
    }

}
