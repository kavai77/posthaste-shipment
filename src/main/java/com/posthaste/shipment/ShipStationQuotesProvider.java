package com.posthaste.shipment;

import com.posthaste.model.PosthasteShipment;
import com.shipstation.client.ApiClient;
import com.shipstation.client.api.RatesApi;
import com.shipstation.client.model.AddressValidatingShipment;
import com.shipstation.client.model.CalculateRatesRequestBody;
import com.shipstation.client.model.InternationalShipmentOptions;
import com.shipstation.client.model.NonDelivery;
import com.shipstation.client.model.PackageContents;
import com.shipstation.client.model.RateRequestBody;
import com.shipstation.client.model.ValidateAddress;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

import static com.posthaste.shipment.ShipStationMapper.INSTANCE;


@Component
@RequiredArgsConstructor
public class ShipStationQuotesProvider implements QuotesProvider {
    private final ApiClient apiClient;
    private final CarrierProvider carrierProvider;

    //    @PostConstruct
    public void init() {
        quotes(PosthasteShipment.builder()
                .shipper(PosthasteShipment.Address.builder()
                        .name("ShipStation API Team")
                        .phone("222-333-4444")
                        .companyName("ShipStation API corp.")
                        .addressLine1("4301 Bull Creek Road")
                        .city("Austin")
                        .stateProvince("TX")
                        .postalCode("78731")
                        .countryCode("US")
                        .build())
                .recipient(PosthasteShipment.Address.builder()
                        .name("The President")
                        .phone("222-333-4444")
                        .addressLine1("1600 Pennsylvania Avenue NW")
                        .city("Washington")
                        .stateProvince("DC")
                        .postalCode("20500")
                        .countryCode("US")
                        .build())
                .packages(List.of(PosthasteShipment.Package.builder()
                        .weight(PosthasteShipment.Weight.builder()
                                .value(BigDecimal.ONE)
                                .unit(PosthasteShipment.WeightUnit.lbs)
                                .build())
                        .build()))
                .build());
    }

    @Override
    @SneakyThrows
    public Object quotes(PosthasteShipment quotesRequest) {
        RatesApi ratesApi = new RatesApi(apiClient);
        var response = ratesApi.calculateRates(new CalculateRatesRequestBody()
                .rateOptions(new RateRequestBody()
                        .carrierIds(carrierProvider.carrierIds())
                        .rateType(RateRequestBody.RateTypeEnum.SHIPMENT)
                        .calculateTaxAmount(false)
                        .preferredCurrency("USD"))
                .shipment(new AddressValidatingShipment()
                        .validateAddress(ValidateAddress.NO_VALIDATION)
                        .shipFrom(INSTANCE.shippingAddressFrom(quotesRequest.shipper()))
                        .shipTo(INSTANCE.shippingAddressTo(quotesRequest.recipient()))
                        .packages(INSTANCE.packages(quotesRequest.packages()))
                        .customs(new InternationalShipmentOptions()
                                .contents(PackageContents.MERCHANDISE)
                                .nonDelivery(NonDelivery.TREAT_AS_ABANDONED)
                                .customsItems(INSTANCE.customsItems(quotesRequest.commodities())))));

        return response.getRateResponse();
    }
}
