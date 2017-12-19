package com.commercetools.sunrise.models.products;

import com.commercetools.sunrise.core.injection.RequestScoped;
import com.commercetools.sunrise.core.viewmodels.formatters.ProductAttributeFormatter;
import com.commercetools.sunrise.core.viewmodels.forms.SelectableViewModelFactory;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.products.ProductVariant;
import io.sphere.sdk.products.attributes.Attribute;
import io.sphere.sdk.producttypes.ProductType;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

@RequestScoped
public class SelectableProductAttributeViewModelFactory extends SelectableViewModelFactory<SelectableProductAttributeViewModel, List<ProductVariant>, AttributeWithProductType> {

    private final ProductAttributeFormatter productAttributeFormatter;
    private final ProductAttributesSettings productAttributesSettings;
    private final ProductAttributeFormSelectableOptionViewModelFactory productAttributeFormSelectableOptionViewModelFactory;

    @Inject
    public SelectableProductAttributeViewModelFactory(final ProductAttributeFormatter productAttributeFormatter, final ProductAttributesSettings productAttributesSettings,
                                                      final ProductAttributeFormSelectableOptionViewModelFactory productAttributeFormSelectableOptionViewModelFactory) {
        this.productAttributeFormatter = productAttributeFormatter;
        this.productAttributesSettings = productAttributesSettings;
        this.productAttributeFormSelectableOptionViewModelFactory = productAttributeFormSelectableOptionViewModelFactory;
    }

    protected final ProductAttributeFormatter getProductAttributeFormatter() {
        return productAttributeFormatter;
    }

    protected final ProductAttributesSettings getProductAttributesSettings() {
        return productAttributesSettings;
    }

    protected final ProductAttributeFormSelectableOptionViewModelFactory getProductAttributeFormSelectableOptionViewModelFactory() {
        return productAttributeFormSelectableOptionViewModelFactory;
    }

    @Override
    protected SelectableProductAttributeViewModel newViewModelInstance(final List<ProductVariant> option, final AttributeWithProductType selectedValue) {
        return new SelectableProductAttributeViewModel();
    }

    @Override
    public final SelectableProductAttributeViewModel create(final List<ProductVariant> option, final AttributeWithProductType selectedValue) {
        return super.create(option, selectedValue);
    }

    public final List<SelectableProductAttributeViewModel> createList(final ProductWithVariant productWithVariant) {
        final Reference<ProductType> productTypeRef = productWithVariant.getProduct().getProductType();
        return productAttributesSettings.selectable().stream()
                .map(productWithVariant.getVariant()::getAttribute)
                .filter(Objects::nonNull)
                .map(attribute -> create(productWithVariant.getProduct().getAllVariants(), AttributeWithProductType.of(attribute, productTypeRef)))
                .collect(toList());
    }

    @Override
    protected void initialize(final SelectableProductAttributeViewModel viewModel, final List<ProductVariant> option, final AttributeWithProductType selectedValue) {
        fillKey(viewModel, option, selectedValue);
        fillName(viewModel, option, selectedValue);
        fillValue(viewModel, option, selectedValue);
        fillReload(viewModel, option, selectedValue);
        fillList(viewModel, option, selectedValue);
        fillSelectData(viewModel, option, selectedValue);
    }

    protected void fillKey(final SelectableProductAttributeViewModel viewModel, final List<ProductVariant> variants, final AttributeWithProductType selectedAttribute) {
        viewModel.setKey(selectedAttribute.getAttribute().getName());
    }

    protected void fillName(final SelectableProductAttributeViewModel viewModel, final List<ProductVariant> variants, final AttributeWithProductType selectedAttribute) {
        productAttributeFormatter.label(selectedAttribute.getAttribute().getName(), selectedAttribute.getProductTypeRef())
                .ifPresent(viewModel::setName);
    }

    protected void fillValue(final SelectableProductAttributeViewModel viewModel, final List<ProductVariant> variants, final AttributeWithProductType selectedAttribute) {
        productAttributeFormatter.convert(selectedAttribute.getAttribute(), selectedAttribute.getProductTypeRef())
                .ifPresent(viewModel::setValue);
    }

    protected void fillReload(final SelectableProductAttributeViewModel viewModel, final List<ProductVariant> variants, final AttributeWithProductType selectedAttribute) {
        viewModel.setReload(productAttributesSettings.primarySelectable().contains(selectedAttribute.getAttribute().getName()));
    }

    protected void fillList(final SelectableProductAttributeViewModel viewModel, final List<ProductVariant> variants, final AttributeWithProductType selectedAttribute) {
        final List<ProductAttributeFormSelectableOptionViewModel> formOptions = new ArrayList<>();
        findDistinctAttributeOptions(variants, selectedAttribute).forEach(attribute -> {
            final AttributeWithProductType attributeWithProductType = AttributeWithProductType.of(attribute, selectedAttribute.getProductTypeRef());
            formOptions.add(productAttributeFormSelectableOptionViewModelFactory.create(attributeWithProductType, selectedAttribute.getAttribute()));
        });
        viewModel.setList(formOptions);
    }

    protected void fillSelectData(final SelectableProductAttributeViewModel viewModel, final List<ProductVariant> variants, final AttributeWithProductType selectedAttribute) {
        final Map<String, Map<String, List<String>>> selectableData = new HashMap<>();
        findDistinctAttributeOptions(variants, selectedAttribute).forEach(attrOption -> {
            final AttributeWithProductType attributeOptionWithProductType = AttributeWithProductType.of(attrOption, selectedAttribute.getProductTypeRef());
            productAttributeFormatter.convertEncoded(attributeOptionWithProductType.getAttribute(), attributeOptionWithProductType.getProductTypeRef())
                    .ifPresent(attrOptionValue -> selectableData.put(attrOptionValue, createAllowedAttributeCombinations(attributeOptionWithProductType, variants)));
        });
        viewModel.setSelectData(selectableData);
    }

    private Map<String, List<String>> createAllowedAttributeCombinations(final AttributeWithProductType fixedAttribute, final List<ProductVariant> variants) {
        final Map<String, List<String>> attrCombination = new HashMap<>();
        productAttributesSettings.selectable().stream()
                .filter(enabledAttrKey -> !fixedAttribute.getAttribute().getName().equals(enabledAttrKey))
                .forEach(enabledAttrKey -> {
                    final List<String> allowedAttrValues = attributeCombination(enabledAttrKey, fixedAttribute, variants);
                    if (!allowedAttrValues.isEmpty()) {
                        attrCombination.put(enabledAttrKey, allowedAttrValues);
                    }
                });
        return attrCombination;
    }

    private List<String> attributeCombination(final String attributeKey, final AttributeWithProductType fixedAttribute,
                                              final List<ProductVariant> variants) {
        return variants.stream()
                .filter(variant -> {
                    final Attribute variantAttribute = variant.getAttribute(fixedAttribute.getAttribute().getName());
                    return variantAttribute != null && variantAttribute.equals(fixedAttribute.getAttribute());
                })
                .map(variant -> variant.getAttribute(attributeKey))
                .filter(Objects::nonNull)
                .map(attribute -> productAttributeFormatter.convertEncoded(attribute, fixedAttribute.getProductTypeRef()))
                .filter(Optional::isPresent)
                .map(Optional::get)
                .distinct()
                .collect(toList());
    }

    private static Stream<Attribute> findDistinctAttributeOptions(final List<ProductVariant> variants, final AttributeWithProductType selectedValue) {
        return variants.stream()
                .map(variant -> variant.getAttribute(selectedValue.getAttribute().getName()))
                .filter(Objects::nonNull)
                .distinct();
    }
}
