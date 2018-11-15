package comp3111.webscraper;


import org.jetbrains.annotations.NotNull;

import java.time.Instant;

import org.jetbrains.annotations.Nullable;
import org.json.JSONObject;

/**
 * Data representing a scraped listing.
 */
public class Item {
    /**
     * Title of the scraped item.
     */
    private String title;
    /**
     * Price of the scraped item.
     */
    private double price;
    /**
     * URL of the scraped item.
     */
    private String url;
    /**
     * Portal where the scraped item is from.
     */
    private String portal;
    /**
     * Time when the scraped item is hosted on the portal.
     */
    private Instant time;

    /**
     * Public no-argument constructor.
     *
     * Initializes all fields to null and 0.
     */
    public Item() {
    }

    /**
     * Construct Item from json object
     *
     * @param rawItem in .3111 json object
     *
     * @author dipsywong98
     */
    public Item(JSONObject rawItem) {
        if (rawItem.has("title")) setTitle(rawItem.optString("title"));
        if (rawItem.has("price")) setPrice(rawItem.optDouble("price"));
        if (rawItem.has("url")) setUrl(rawItem.optString("url"));
        if (rawItem.has("portal")) setPortal(rawItem.optString("portal"));
    }

    /**
     * Returns the title of the item.
     *
     * @return Title of the item, or null if item does not have a title.
     */
    public @Nullable String getTitle() {
        return title;
    }

    /**
     * Sets the title of the item.
     *
     * @param title New title of the item. The actual parameter must not be null.
     */
    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    /**
     * Returns the price of the item.
     *
     * @return Price of the item, or 0.0 if the item does not have a price associated.
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets the price of the item.
     *
     * @param price New price of the item.
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Returns the URL of the item.
     *
     * @return URL of the item, or null if the item does not have a URL.
     */
    public @Nullable String getUrl() {
        return url;
    }

    /**
     * Sets the URL of the item.
     *
     * @param url New URL of the item. The actual parameter must not be null.
     */
    public void setUrl(@NotNull String url) {
        this.url = url;
    }

    /**
     * Returns the portal the item is scraped from.
     *
     * @return Portal of the item, or null if the portal is not set.
     *
     * @author Derppening
     */
    public @Nullable String getPortal() {
        return portal;
    }

    /**
     * Sets the portal where the item is scraped from.
     *
     * @param portal New source portal of the item. The actual parameter must not be null.
     */
    public void setPortal(@NotNull String portal) {
        this.portal = portal;
    }

    /**
     * Returns the time when this item is posted to the portal.
     *
     * @return Time the listing is posted, or null if the data is not available for this listing.
     *
     * @author Derppening
     */
    public @Nullable Instant getTime() {
        return time;
    }

    /**
     * Sets the time this item is listed in the portal.
     *
     * @param time New time when the item is listed in the portal.
     */
    public void setTime(@NotNull Instant time) {
        this.time = time;
    }
}
