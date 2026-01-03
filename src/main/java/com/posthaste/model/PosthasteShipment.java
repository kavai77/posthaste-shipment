package com.posthaste.model;

import lombok.Builder;

import java.math.BigDecimal;

@Builder
public record PosthasteShipment(
        Address shipper,
        Address recipient,
        MeasuredValueWeight weight,
        Dimensions dimensions,
        String item,
        Integer quantity,
        BigDecimal value) {
    @Builder
    public record MeasuredValueWeight(BigDecimal value, WeightUnit unit) {
    }

    @Builder
    public record MeasuredValueLength(BigDecimal value, LengthUnit unit) {
    }

    @Builder
    public record Dimensions(MeasuredValueLength length, MeasuredValueLength width, MeasuredValueLength height) {
    }

    @Builder
    public record Address(String name,
                          String companyName,
                          String postalCode,
                          String city,
                          String addressLine1,
                          String addressLine2,
                          String addressLine3,
                          String stateProvince,
                          String countryCode,
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
