package com.commercetools.sunrise.shoppingcart.checkout.shipping;

import com.commercetools.sunrise.common.models.FormSelectableOptionBean;
import io.sphere.sdk.shippingmethods.ShippingMethod;

import javax.annotation.Nullable;

public class ShippingMethodBean extends FormSelectableOptionBean {

    private String deliveryDays;
    private String price;

    public ShippingMethodBean() {
    }

    public ShippingMethodBean(final ShippingMethod shippingMethod, final @Nullable String selectedShippingMethodId) {
        setLabel(shippingMethod.getName());
        setValue(shippingMethod.getId());
        setSelected(shippingMethod.getId().equals(selectedShippingMethodId));
    }

    public String getDeliveryDays() {
        return deliveryDays;
    }

    public void setDeliveryDays(final String deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(final String price) {
        this.price = price;
    }
}
