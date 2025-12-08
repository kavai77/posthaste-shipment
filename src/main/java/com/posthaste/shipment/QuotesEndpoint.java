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
import java.util.function.Function;


@RestController
public class QuotesEndpoint {
    @Value("${easypost.api.key}")
    private String easypostApiKey;


    @PostMapping("/quotes")
    @SneakyThrows
    public Shipment quotes(@RequestBody QuotesRequest quotesRequest) {
        EasyPostClient client = new EasyPostClient(easypostApiKey);

        var toAddressMap = new HashMap<String, Object>();
        toAddressMap.put("name", quotesRequest.recipient().name());
        toAddressMap.put("street1", quotesRequest.recipient().address());
        toAddressMap.put("city", quotesRequest.recipient().city());
        toAddressMap.put("state", quotesRequest.recipient().state());
        toAddressMap.put("zip", quotesRequest.recipient().postcode());
        toAddressMap.put("country", quotesRequest.recipient().country());
        toAddressMap.put("phone", quotesRequest.recipient().phone());
        toAddressMap.put("email", quotesRequest.recipient().email());

        var fromAddressMap = new HashMap<String, Object>();
        fromAddressMap.put("name", quotesRequest.shipper().name());
        fromAddressMap.put("street1", quotesRequest.shipper().address());
        fromAddressMap.put("city", quotesRequest.shipper().city());
        fromAddressMap.put("state", quotesRequest.shipper().state());
        fromAddressMap.put("zip", quotesRequest.shipper().postcode());
        fromAddressMap.put("country", quotesRequest.shipper().country());
        fromAddressMap.put("phone", quotesRequest.shipper().phone());
        fromAddressMap.put("email", quotesRequest.shipper().email());

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
