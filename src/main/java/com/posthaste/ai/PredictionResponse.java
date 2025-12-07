package com.posthaste.ai;

import java.math.BigDecimal;

public record PredictionResponse(
        Address shipper,
        Address recipient,
        MeasuredValue weight,
        Dimensions dimensions,
        String item) {
    public record MeasuredValue(BigDecimal value, String unit) {
    }

    public record Dimensions(MeasuredValue length, MeasuredValue width, MeasuredValue height) {
    }

    public record Address(String name,
                          String postcode,
                          String city,
                          String address,
                          String state,
                          String country,
                          String phone,
                          String email) {
    }
}
