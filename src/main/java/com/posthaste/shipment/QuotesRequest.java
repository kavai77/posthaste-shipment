package com.posthaste.shipment;

import java.math.BigDecimal;

public record QuotesRequest(
        Address shipper,
        Address recipient,
        MeasuredValue weight,
        Dimensions dimensions,
        String item) {
    public record Address(String name,
                          String postcode,
                          String city,
                          String address,
                          String state,
                          String country,
                          String phone,
                          String email) {
    }

    public record MeasuredValue(BigDecimal value, String unit) {
    }

    public record Dimensions(MeasuredValue length, MeasuredValue width, MeasuredValue height) {
    }
}
