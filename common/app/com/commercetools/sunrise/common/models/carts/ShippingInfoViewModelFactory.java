package com.commercetools.sunrise.common.models.carts;

import com.commercetools.sunrise.framework.injection.RequestScoped;
import com.commercetools.sunrise.common.models.ViewModelFactory;
import com.commercetools.sunrise.common.utils.CartPriceUtils;
import com.commercetools.sunrise.common.utils.PriceFormatter;
import io.sphere.sdk.carts.CartLike;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.products.PriceUtils;
import io.sphere.sdk.shippingmethods.ShippingMethod;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.money.CurrencyUnit;
import javax.money.MonetaryAmount;

@RequestScoped
public class ShippingInfoViewModelFactory extends ViewModelFactory<ShippingInfoViewModel, CartLike<?>> {

    private final CurrencyUnit currency;
    private final PriceFormatter priceFormatter;

    @Inject
    public ShippingInfoViewModelFactory(final CurrencyUnit currency, final PriceFormatter priceFormatter) {
        this.currency = currency;
        this.priceFormatter = priceFormatter;
    }

    @Override
    protected ShippingInfoViewModel getViewModelInstance() {
        return new ShippingInfoViewModel();
    }

    @Override
    public final ShippingInfoViewModel create(@Nullable final CartLike<?> cartLike) {
        return super.create(cartLike);
    }

    @Override
    protected final void initialize(final ShippingInfoViewModel viewModel, final CartLike<?> cartLike) {
        fillLabel(viewModel, cartLike);
        fillDescription(viewModel, cartLike);
        fillPrice(viewModel, cartLike);
    }

    protected void fillLabel(final ShippingInfoViewModel model, @Nullable final CartLike<?> cartLike) {
        if (cartLike != null && cartLike.getShippingInfo() != null) {
            model.setLabel(cartLike.getShippingInfo().getShippingMethodName());
        }
    }

    protected void fillDescription(final ShippingInfoViewModel model, @Nullable final CartLike<?> cartLike) {
        if (cartLike != null && cartLike.getShippingInfo() != null) {
            final Reference<ShippingMethod> ref = cartLike.getShippingInfo().getShippingMethod();
            if (ref != null && ref.getObj() != null) {
                model.setDescription(ref.getObj().getDescription());
            }
        }
    }

    protected void fillPrice(final ShippingInfoViewModel model, @Nullable final CartLike<?> cartLike) {
        if (cartLike != null) {
            final MonetaryAmount amount = CartPriceUtils.calculateAppliedShippingPrice(cartLike)
                    .orElseGet(() -> zeroAmount(cartLike.getCurrency()));
            model.setPrice(priceFormatter.format(amount));
        } else {
            model.setPrice(priceFormatter.format(zeroAmount(currency)));
        }
    }

    private static MonetaryAmount zeroAmount(final CurrencyUnit currency) {
        return PriceUtils.zeroAmount(currency);
    }
}
