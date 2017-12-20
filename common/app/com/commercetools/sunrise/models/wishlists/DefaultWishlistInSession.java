package com.commercetools.sunrise.models.wishlists;

import com.commercetools.sunrise.core.sessions.CacheableObjectStoringSessionStrategy;
import com.commercetools.sunrise.core.sessions.DataFromResourceStoringOperations;
import com.commercetools.sunrise.core.sessions.ObjectStoringSessionStrategy;
import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.json.SphereJsonUtils;
import io.sphere.sdk.shoppinglists.ShoppingList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.Configuration;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.Optional;

/**
 * Default implementation of {@link WishlistInSession}.
 */
public class DefaultWishlistInSession extends DataFromResourceStoringOperations<ShoppingList> implements  WishlistInSession {
    private static final Logger LOGGER = LoggerFactory.getLogger(WishlistInSession.class);
    private static final String DEFAULT_WISHLIST_ID_SESSION_KEY = "sunrise-wishlist-id";
    private static final String DEFAULT_WISHLIST_SESSION_KEY = "sunrise-wishlist";

    private final String wishlistIdSessionKey;
    private final String wishlistSessionKey;
    private final ObjectStoringSessionStrategy session;

    @Inject
    protected DefaultWishlistInSession(final Configuration configuration, final CacheableObjectStoringSessionStrategy session) {
        this.wishlistIdSessionKey = configuration.getString("session.wishlist.wishlistId", DEFAULT_WISHLIST_ID_SESSION_KEY);
        this.wishlistSessionKey = configuration.getString("session.wishlist.wishlist", DEFAULT_WISHLIST_SESSION_KEY);
        this.session = session;
    }

    @Override
    public Optional<String> findWishlistId() {
        return session.findValueByKey(wishlistIdSessionKey);
    }

    @Override
    public Optional<ShoppingList> findWishlist() {
        return session.findObjectByKey(wishlistSessionKey, JsonNode.class)
                .map(json -> SphereJsonUtils.readObject(json, ShoppingList.class));
    }

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    @Override
    protected void storeAssociatedData(@NotNull final ShoppingList wishlist) {
        session.overwriteValueByKey(wishlistIdSessionKey, wishlist.getId());
        session.overwriteObjectByKey(wishlistSessionKey, SphereJsonUtils.toJsonNode(wishlist));
    }

    @Override
    protected void removeAssociatedData() {
        session.removeValueByKey(wishlistIdSessionKey);
        session.removeObjectByKey(wishlistSessionKey);
    }
}
