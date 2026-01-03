package com.posthaste.shipment;

import com.posthaste.model.PosthasteShipment;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequiredArgsConstructor
public class QuotesEndpoint {
    @Qualifier("shipStationQuotesProvider")
    private final QuotesProvider quotesProvider;

    @PostMapping("/quotes")
    @SneakyThrows
    public Object quotes(@RequestBody PosthasteShipment quotesRequest) {
        return quotesProvider.quotes(quotesRequest);
    }

}
