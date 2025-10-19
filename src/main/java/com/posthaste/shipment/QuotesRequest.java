package com.posthaste.shipment;

public record QuotesRequest(
        String recipient,
        String postcode,
        String city,
        String address,
        String state,
        String country,
        String weight,
        String length,
        String width,
        String height,
        String phone,
        String email,
        String item) {}
