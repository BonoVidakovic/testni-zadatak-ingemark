package com.ingemark.movies.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;
import java.math.BigDecimal;

public class ComaBigDecimalDeserializer extends StdDeserializer<BigDecimal> {
    public ComaBigDecimalDeserializer() {
        this(null);
    }

    public ComaBigDecimalDeserializer(Class<BigDecimal> vc) {
        super(vc);
    }

    @Override
    public BigDecimal deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        var number = p.getCodec().readValue(p, String.class);

        return new BigDecimal(number.replace(",", "."));
    }
}
