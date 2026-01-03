package com.posthaste.shipment;

import com.posthaste.model.PosthasteShipment;
import com.shipstation.client.ApiClient;
import com.shipstation.client.api.RatesApi;
import com.shipstation.client.model.AddressResidentialIndicator;
import com.shipstation.client.model.AddressValidatingShipment;
import com.shipstation.client.model.CalculateRatesRequestBody;
import com.shipstation.client.model.ModelPackage;
import com.shipstation.client.model.RateRequestBody;
import com.shipstation.client.model.ShippingAddress;
import com.shipstation.client.model.ShippingAddressTo;
import com.shipstation.client.model.ValidateAddress;
import com.shipstation.client.model.Weight;
import com.shipstation.client.model.WeightUnit;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;


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
                .weight(PosthasteShipment.MeasuredValueWeight.builder()
                        .value(BigDecimal.ONE)
                        .unit(PosthasteShipment.WeightUnit.lbs)
                        .build())
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
                        .shipFrom(new ShippingAddress()
                                .name(quotesRequest.shipper().name())
                                .phone(quotesRequest.shipper().phone())
                                .email(quotesRequest.shipper().email())
                                .companyName(quotesRequest.shipper().companyName())
                                .addressLine1(quotesRequest.shipper().addressLine1())
                                .addressLine2(quotesRequest.shipper().addressLine2())
                                .addressLine3(quotesRequest.shipper().addressLine3())
                                .cityLocality(quotesRequest.shipper().city())
                                .stateProvince(quotesRequest.shipper().stateProvince())
                                .postalCode(quotesRequest.shipper().postalCode())
                                .countryCode(quotesRequest.shipper().countryCode())
                                .addressResidentialIndicator(AddressResidentialIndicator.NO))
                        .shipTo(new ShippingAddressTo()
                                .name(quotesRequest.recipient().name())
                                .phone(quotesRequest.recipient().phone())
                                .email(quotesRequest.recipient().email())
                                .companyName(quotesRequest.recipient().companyName())
                                .addressLine1(quotesRequest.recipient().addressLine1())
                                .addressLine2(quotesRequest.recipient().addressLine2())
                                .addressLine3(quotesRequest.recipient().addressLine3())
                                .cityLocality(quotesRequest.recipient().city())
                                .stateProvince(quotesRequest.recipient().stateProvince())
                                .postalCode(quotesRequest.recipient().postalCode())
                                .countryCode(quotesRequest.recipient().countryCode())
                                .addressResidentialIndicator(AddressResidentialIndicator.NO))
                        .packages(List.of(new ModelPackage()
                                .packageCode("package")
                                .weight(new Weight()
                                        .value(quotesRequest.weight().value())
                                        .unit(quotesRequest.weight().unit() == null
                                                ? WeightUnit.POUND : switch (quotesRequest.weight().unit()) {
                                            case kg -> WeightUnit.KILOGRAM;
                                            case lbs -> WeightUnit.POUND;
                                        }))))));

        return response.getRateResponse();
    }
}
