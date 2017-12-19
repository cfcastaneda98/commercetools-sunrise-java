package com.commercetools.sunrise.core.viewmodels.content.products;

import com.commercetools.sunrise.models.products.*;
import com.commercetools.sunrise.core.viewmodels.formatters.ProductAttributeFormatter;
import io.sphere.sdk.models.Reference;
import io.sphere.sdk.products.ProductVariant;
import io.sphere.sdk.products.attributes.Attribute;
import io.sphere.sdk.products.attributes.AttributeAccess;
import io.sphere.sdk.producttypes.ProductType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SelectableProductAttributeViewModelFactory}.
 */
@RunWith(MockitoJUnitRunner.class)
public class SelectableProductAttributeViewModelFactoryTest  {
    private static final String STRING_ATTRIBUTE_NAME = "stringAttribute";
    private static final String NUMBER_ATTRIBUTE_NAME = "numberAttribute";
    private static final String PRODUCT_TYPE_ID = "productTypeId";

    @Mock
    private ProductAttributesSettings fakeProductAttributeSettings;
    @Mock
    private ProductAttributeFormatter fakeProductAttributeFormatter;
    @Mock
    private ProductAttributeFormSelectableOptionViewModelFactory productAttributeFormSelectableOptionViewModelFactory;
    @Mock
    private Reference<ProductType> fakeProductTypeRef;

    @InjectMocks
    private SelectableProductAttributeViewModelFactory viewModelFactory;

    @Test
    public void createFromSelectableStringAttributeAndSelectableNumberAttribute() {
        final String stringValue = "v1";
        final AttributeWithProductType stringAttributeWithProductType = attributeWithProductType(STRING_ATTRIBUTE_NAME, AttributeAccess.ofString(), stringValue);
        when(fakeProductAttributeFormatter.convertEncoded(eq(stringAttributeWithProductType.getAttribute()), eq(stringAttributeWithProductType.getProductTypeRef()))).thenReturn(Optional.of(stringValue));

        final AttributeWithProductType numberAttributeWithProductType = attributeWithProductType(NUMBER_ATTRIBUTE_NAME, AttributeAccess.ofDouble(), 12.0);
        when(fakeProductAttributeFormatter.convertEncoded(eq(numberAttributeWithProductType.getAttribute()), eq(numberAttributeWithProductType.getProductTypeRef()))).thenReturn(Optional.of("120"));

        mockProductAttributeSettings(STRING_ATTRIBUTE_NAME, NUMBER_ATTRIBUTE_NAME);
        final ProductVariant productVariant = mockProductVariant(stringAttributeWithProductType, numberAttributeWithProductType);
        final List<ProductVariant> variants = Collections.singletonList(productVariant);

        final SelectableProductAttributeViewModel attributeViewModel = viewModelFactory.create(variants, stringAttributeWithProductType);

        assertThat(attributeViewModel.getSelectData())
                .hasSize(1)
                .containsEntry(stringValue, singletonMap(NUMBER_ATTRIBUTE_NAME, singletonList("120")));
    }

    private <T> AttributeWithProductType attributeWithProductType(final String name, final AttributeAccess<T> attributeAccess, final T value) {
        final Attribute attribute = Attribute.of(name, attributeAccess, value);
        final AttributeWithProductType attributeWithProductType = AttributeWithProductType.of(attribute, fakeProductTypeRef);
        return attributeWithProductType;
    }

    private void mockProductAttributeSettings(final String selectablePrimaryAttribute, final String selectableSecondaryAttribute) {
        when(fakeProductAttributeSettings.primarySelectable()).thenReturn(Arrays.asList(selectablePrimaryAttribute));
        when(fakeProductAttributeSettings.selectable()).thenReturn(Arrays.asList(selectablePrimaryAttribute, selectableSecondaryAttribute));
    }

    private ProductVariant mockProductVariant(final AttributeWithProductType... attributeWithProductTypes) {
        final ProductVariant productVariant = mock(ProductVariant.class);

        final List<Attribute> attributes = Stream.of(attributeWithProductTypes)
                .map(AttributeWithProductType::getAttribute)
                .collect(Collectors.toList());

        for (final Attribute attribute : attributes) {
            when(productVariant.getAttribute(attribute.getName())).thenReturn(attribute);
        }

        return productVariant;
    }
}
