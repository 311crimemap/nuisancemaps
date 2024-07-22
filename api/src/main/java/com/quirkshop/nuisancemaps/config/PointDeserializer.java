package com.quirkshop.nuisancemaps.config;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;

public class PointDeserializer extends JsonDeserializer<Point> {
    private final int SRID = 4326;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(PrecisionModel.FLOATING),
                                                                        SRID);
    @Override
    public Point deserialize(JsonParser p, DeserializationContext ctxt)
            throws IOException, JsonProcessingException {

        JsonNode node = p.getCodec().readTree(p);

        if (node.isArray() && node.size() == 2) {
            double x = node.get(0).asDouble();
            double y = node.get(1).asDouble();
            return geometryFactory.createPoint(new Coordinate(x, y));
        }

        throw new IllegalArgumentException("Invalid format for Point");
    }
}
