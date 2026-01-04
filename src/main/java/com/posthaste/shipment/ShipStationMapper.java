package com.posthaste.shipment;

import com.posthaste.model.PosthasteShipment;
import com.shipstation.client.model.CustomsItem;
import com.shipstation.client.model.DimensionUnit;
import com.shipstation.client.model.Dimensions;
import com.shipstation.client.model.ModelPackage;
import com.shipstation.client.model.ShippingAddress;
import com.shipstation.client.model.ShippingAddressTo;
import com.shipstation.client.model.Weight;
import com.shipstation.client.model.WeightUnit;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ValueMapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ShipStationMapper {
    ShipStationMapper INSTANCE = Mappers.getMapper(ShipStationMapper.class);

    @Mapping(source = "city", target = "cityLocality")
    @Mapping(target = "addressResidentialIndicator", constant = "NO")
    ShippingAddress shippingAddressFrom(PosthasteShipment.Address address);

    @Mapping(source = "city", target = "cityLocality")
    @Mapping(target = "addressResidentialIndicator", constant = "NO")
    ShippingAddressTo shippingAddressTo(PosthasteShipment.Address address);

    List<ModelPackage> packages(List<PosthasteShipment.Package> packages);

    ModelPackage modelPackage(PosthasteShipment.Package aPackage);

    Weight weight(PosthasteShipment.Weight weight);

    @ValueMapping(source = "kg", target = "KILOGRAM")
    @ValueMapping(source = "lbs", target = "POUND")
    WeightUnit weightUnit(PosthasteShipment.WeightUnit aPackage);

    Dimensions dimensions(PosthasteShipment.Dimensions dimensions);

    @ValueMapping(source = "cm", target = "CENTIMETER")
    @ValueMapping(source = "in", target = "INCH")
    DimensionUnit dimensionUnit(PosthasteShipment.DimensionUnit dimensions);

    CustomsItem customsItem(PosthasteShipment.Commodity commodity);

    List<CustomsItem> customsItems(List<PosthasteShipment.Commodity> commodity);
}
