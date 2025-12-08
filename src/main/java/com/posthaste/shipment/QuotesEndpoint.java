package com.posthaste.shipment;

import com.easypost.model.Shipment;
import com.easypost.service.EasyPostClient;
import com.posthaste.shipment.QuotesRequest.Dimensions;
import com.posthaste.shipment.QuotesRequest.MeasuredValue;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@RestController
public class QuotesEndpoint {
    @Value("${easypost.api.key}")
    private String easypostApiKey;


    @PostMapping("/quotes")
    @SneakyThrows
    public Shipment quotes(@RequestBody QuotesRequest quotesRequest) {
        EasyPostClient client = new EasyPostClient(easypostApiKey);

        var fromAddressMap = createAddressMap(quotesRequest.shipper());
        var toAddressMap = createAddressMap(quotesRequest.recipient());

        var parcelMap = new HashMap<String, Object>();
        parcelMap.put("length", getDimension(quotesRequest, Dimensions::length));
        parcelMap.put("width", getDimension(quotesRequest, Dimensions::width));
        parcelMap.put("height", getDimension(quotesRequest, Dimensions::height));
        parcelMap.put("weight", getMeasuredValue(quotesRequest.weight()));
        var customsInfoMap = new HashMap<String, Object>();
        customsInfoMap.put("description", quotesRequest.item());

        var params = new HashMap<String, Object>();
        params.put("to_address", toAddressMap);
        params.put("from_address", fromAddressMap);
        params.put("parcel", parcelMap);
        params.put("customs_info", customsInfoMap);

        return client.shipment.create(params);
    }

    private Map<String, Object> createAddressMap(QuotesRequest.Address address) {
        if (address == null) {
            return Map.of();
        }
        var addressMap = new HashMap<String, Object>();
        addressMap.put("name", address.name());
        addressMap.put("street1", address.address());
        addressMap.put("city", address.city());
        addressMap.put("state", address.state());
        addressMap.put("zip", address.postcode());
        addressMap.put("country", address.country());
        addressMap.put("phone", address.phone());
        addressMap.put("email", address.email());
        return addressMap;
    }

    private Float getDimension(QuotesRequest quotesRequest, Function<Dimensions, MeasuredValue> function) {
        if (quotesRequest.dimensions() == null) {
            return null;
        }
        return getMeasuredValue(function.apply(quotesRequest.dimensions()));
    }

    private Float getMeasuredValue(MeasuredValue measuredValue) {
        return measuredValue != null && measuredValue.value() != null ? measuredValue.value().floatValue() : null;
    }
}
