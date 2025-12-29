package com.posthaste.ai;

import java.math.BigDecimal;

public record PredictionResponse(
        Address shipper,
        Address recipient,
        MeasuredValueWeight weight,
        Dimensions dimensions,
        String item,
        Integer quantity,
        BigDecimal value) {
    public record MeasuredValueWeight(BigDecimal value, WeightUnit unit) {
    }

    public record MeasuredValueLength(BigDecimal value, LengthUnit unit) {
    }

    public record Dimensions(MeasuredValueLength length, MeasuredValueLength width, MeasuredValueLength height) {
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

    public enum WeightUnit {
        kg, lbs
    }

    public enum LengthUnit {
        cm, in
    }
}
