package com.posthaste.shipment;

import com.easypost.service.EasyPostClient;
import com.posthaste.model.PosthasteShipment;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class EasyPostQuotesProvider implements QuotesProvider {
    @Value("${easypost.api.key}")
    private final String easypostApiKey;

    @Override
    @SneakyThrows
    public Object quotes(PosthasteShipment quotesRequest) {
        EasyPostClient client = new EasyPostClient(easypostApiKey);

        var fromAddressMap = createAddressMap(quotesRequest.shipper());
        var toAddressMap = createAddressMap(quotesRequest.recipient());

        var parcelMap = new HashMap<String, Object>();
        parcelMap.put("length", getDimension(quotesRequest, PosthasteShipment.Dimensions::length));
        parcelMap.put("width", getDimension(quotesRequest, PosthasteShipment.Dimensions::width));
        parcelMap.put("height", getDimension(quotesRequest, PosthasteShipment.Dimensions::height));
        parcelMap.put("weight", getMeasuredValue(quotesRequest.weight()));
        var customsItemMap = new HashMap<String, Object>();
        customsItemMap.put("description", quotesRequest.item());
        customsItemMap.put("weight", getMeasuredValue(quotesRequest.weight()));
        customsItemMap.put("quantity", Optional.ofNullable(quotesRequest.quantity()).orElse(1));
        customsItemMap.put("value", Optional.ofNullable(quotesRequest.value()).orElse(BigDecimal.ONE));
        var customsItem = client.customsItem.create(customsItemMap);

        var customsInfoMap = new HashMap<String, Object>();
        customsInfoMap.put("contents_type", "merchandise");
        customsInfoMap.put("customs_items", List.of(customsItem));

        var params = new HashMap<String, Object>();
        params.put("to_address", toAddressMap);
        params.put("from_address", fromAddressMap);
        params.put("parcel", parcelMap);
        params.put("customs_info", customsInfoMap);

        return client.shipment.create(params);
    }


    private Map<String, Object> createAddressMap(PosthasteShipment.Address address) {
        if (address == null) {
            return Map.of();
        }
        var addressMap = new HashMap<String, Object>();
        addressMap.put("name", address.name());
        addressMap.put("street1", address.addressLine1());
        addressMap.put("city", address.city());
        addressMap.put("state", address.stateProvince());
        addressMap.put("zip", address.postalCode());
        addressMap.put("country", address.countryCode());
        addressMap.put("phone", address.phone());
        addressMap.put("email", address.email());
        return addressMap;
    }

    private Float getDimension(PosthasteShipment quotesRequest, Function<PosthasteShipment.Dimensions, PosthasteShipment.MeasuredValueLength> function) {
        if (quotesRequest.dimensions() == null) {
            return null;
        }
        var measuredValue = function.apply(quotesRequest.dimensions());
        return measuredValue != null && measuredValue.value() != null ? measuredValue.value().floatValue() : null;
    }

    private Float getMeasuredValue(PosthasteShipment.MeasuredValueWeight measuredValue) {
        return measuredValue != null && measuredValue.value() != null ? measuredValue.value().floatValue() : null;
    }
}
