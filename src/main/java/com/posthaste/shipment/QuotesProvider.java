package com.posthaste.shipment;

import com.posthaste.model.PosthasteShipment;

public interface QuotesProvider {
    Object quotes(PosthasteShipment quotesRequest);
}
