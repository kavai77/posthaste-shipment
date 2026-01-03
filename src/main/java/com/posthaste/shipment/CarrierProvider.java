package com.posthaste.shipment;

import com.shipstation.client.ApiClient;
import com.shipstation.client.api.CarriersApi;
import com.shipstation.client.model.Carrier;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.posthaste.ApplicationConfiguration.DEFAULT_CACHE;

@Component
@RequiredArgsConstructor
public class CarrierProvider {
    private final ApiClient apiClient;

    @SneakyThrows
    @Cacheable(DEFAULT_CACHE)
    public List<String> carrierIds() {
        CarriersApi carriersApi = new CarriersApi(apiClient);
        var carriers = carriersApi.listCarriers();
        return carriers.getCarriers().stream()
                .map(Carrier::getCarrierId)
                .toList();
    }
}
