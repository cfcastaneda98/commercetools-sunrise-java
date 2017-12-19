package com.commercetools.sunrise.models.products;

import com.commercetools.sunrise.core.injection.RequestScoped;
import com.commercetools.sunrise.core.viewmodels.SimpleViewModelFactory;
import com.commercetools.sunrise.core.viewmodels.formatters.ProductAttributeFormatter;
import io.sphere.sdk.models.Referenceable;
import io.sphere.sdk.products.ProductProjection;
import io.sphere.sdk.products.ProductVariant;
import io.sphere.sdk.producttypes.ProductType;

import javax.inject.Inject;
import java.util.Optional;

import static java.util.stream.Collectors.joining;

@RequestScoped
public class ProductVariantReferenceViewModelMapFactory extends SimpleViewModelFactory<ProductVariantReferenceMapViewModel, ProductWithVariant> {

    private final ProductAttributesSettings productAttributesSettings;
    private final ProductAttributeFormatter productAttributeFormatter;
    private final ProductVariantReferenceViewModelFactory productVariantReferenceViewModelFactory;

    @Inject
    public ProductVariantReferenceViewModelMapFactory(final ProductAttributesSettings productAttributeSettings, final ProductAttributeFormatter productAttributeFormatter, final ProductVariantReferenceViewModelFactory productVariantReferenceViewModelFactory) {
        this.productAttributesSettings = productAttributeSettings;
        this.productAttributeFormatter = productAttributeFormatter;
        this.productVariantReferenceViewModelFactory = productVariantReferenceViewModelFactory;
    }

    protected final ProductAttributesSettings getProductAttributesSettings() {
        return productAttributesSettings;
    }

    protected final ProductAttributeFormatter getProductAttributeFormatter() {
        return productAttributeFormatter;
    }

    protected final ProductVariantReferenceViewModelFactory getProductVariantReferenceViewModelFactory() {
        return productVariantReferenceViewModelFactory;
    }

    @Override
    protected ProductVariantReferenceMapViewModel newViewModelInstance(final ProductWithVariant productWithVariant) {
        return new ProductVariantReferenceMapViewModel();
    }

    @Override
    public final ProductVariantReferenceMapViewModel create(final ProductWithVariant productWithVariant) {
        return super.create(productWithVariant);
    }

    @Override
    protected final void initialize(final ProductVariantReferenceMapViewModel viewModel, final ProductWithVariant productWithVariant) {
        fillMap(viewModel, productWithVariant);
    }

    protected void fillMap(final ProductVariantReferenceMapViewModel map, final ProductWithVariant productWithVariant) {
        productWithVariant.getProduct().getAllVariants()
                .forEach(productVariant -> {
                    final String mapKey = createMapKey(productVariant, productWithVariant.getProduct().getProductType());
                    final ProductVariantReferenceViewModel mapValue = createMapValue(productWithVariant.getProduct(), productVariant);
                    map.put(mapKey, mapValue);
                });
    }

    private String createMapKey(final ProductVariant variant, final Referenceable<ProductType> productTypeRef) {
        return productAttributesSettings.selectable().stream()
                .map(variant::getAttribute)
                .map(attribute -> attribute != null ? AttributeWithProductType.of(attribute, productTypeRef) : null)
                .map(attribute -> productAttributeFormatter.convertEncoded(attribute.getAttribute(), attribute.getProductTypeRef()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(joining("-"));
    }

    private ProductVariantReferenceViewModel createMapValue(final ProductProjection product, final ProductVariant productVariant) {
        final ProductWithVariant productWithVariant = ProductWithVariant.of(product, productVariant);
        return productVariantReferenceViewModelFactory.create(productWithVariant);
    }
}
