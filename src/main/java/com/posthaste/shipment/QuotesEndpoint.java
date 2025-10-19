package com.posthaste.shipment;

import com.easypost.model.Shipment;
import com.easypost.service.EasyPostClient;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;


@RestController
@RequestMapping("/shipment")
public class QuotesEndpoint {
    @Value("${easypost.api.key}")
    private String easypostApiKey;


    @PostMapping("/quotes")
    @SneakyThrows
    public Shipment quotes(@RequestBody QuotesRequest quotesRequest) {
        EasyPostClient client = new EasyPostClient(easypostApiKey);

        var toAddressMap = new HashMap<String, Object>();
        toAddressMap.put("name", quotesRequest.recipient());
        toAddressMap.put("street1", quotesRequest.address());
        toAddressMap.put("city", quotesRequest.city());
        toAddressMap.put("state", quotesRequest.state());
        toAddressMap.put("country", quotesRequest.country());
        toAddressMap.put("phone", quotesRequest.phone());
        toAddressMap.put("email", quotesRequest.email());
        toAddressMap.put("zip", quotesRequest.postcode());

        var fromAddressMap = new HashMap<String, Object>();
        fromAddressMap.put("name", "EasyPost");
        fromAddressMap.put("street1", "417 Montgomery Street");
        fromAddressMap.put("street2", "5th Floor");
        fromAddressMap.put("city", "San Francisco");
        fromAddressMap.put("state", "CA");
        fromAddressMap.put("zip", "94104");
        fromAddressMap.put("country", "US");
        fromAddressMap.put("phone", "4153334445");
        fromAddressMap.put("email", "support@easypost.com");

        var parcelMap = new HashMap<String, Object>();
        parcelMap.put("length", Float.parseFloat(quotesRequest.length()));
        parcelMap.put("width", Float.parseFloat(quotesRequest.width()));
        parcelMap.put("height", Float.parseFloat(quotesRequest.height()));
        parcelMap.put("weight", Float.parseFloat(quotesRequest.weight()));
        var customsInfoMap = new HashMap<String, Object>();
        customsInfoMap.put("description", quotesRequest.item());

        var params = new HashMap<String, Object>();
        params.put("to_address", toAddressMap);
        params.put("from_address", fromAddressMap);
        params.put("parcel", parcelMap);
        params.put("customs_info", customsInfoMap);

        return client.shipment.create(params);
    }
}
