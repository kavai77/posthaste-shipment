package com.posthaste.model;

import lombok.Builder;

import java.math.BigDecimal;
import java.util.List;

@Builder
public record PosthasteShipment(
        Address shipper,
        Address recipient,
        List<Package> packages,
        List<Commodity> commodities) {

    @Builder
    public record Package(Weight weight, Dimensions dimensions) {
    }

    @Builder
    public record Weight(BigDecimal value, WeightUnit unit) {
    }

    @Builder
    public record Dimensions(BigDecimal length, BigDecimal width, BigDecimal height, DimensionUnit unit) {
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

    @Builder
    public record Commodity(
            String harmonizedTariffCode,
            Integer quantity,
            String description,
            BigDecimal value,
            String valueCurrency,
            String countryOfOrigin
    ) {
    }

    public enum WeightUnit {
        kg, lbs
    }

    public enum DimensionUnit {
        cm, in
    }
}
