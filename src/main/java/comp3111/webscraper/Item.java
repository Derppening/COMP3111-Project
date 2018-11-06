package comp3111.webscraper;


import org.jetbrains.annotations.NotNull;

import java.time.Instant;

import org.json.JSONObject;

public class Item {
    private String title;
    private double price;
    private String url;
    private String portal;
    private Instant time;

    public Item() {
    }

    /**
     * Construct Item from json object
     *
     * @param rawItem in .3111 json object
     */
    public Item(JSONObject rawItem) {
        if (rawItem.has("title")) setTitle(rawItem.optString("title"));
        if (rawItem.has("price")) setPrice(rawItem.optDouble("price"));
        if (rawItem.has("url")) setUrl(rawItem.optString("url"));
        if (rawItem.has("portal")) setPortal(rawItem.optString("portal"));
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(@NotNull String title) {
        this.title = title;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(@NotNull String url) {
        this.url = url;
    }

    public String getPortal() {
        return portal;
    }

    public void setPortal(String portal) {
        this.portal = portal;
    }

    public Instant getTime() {
        return time;
    }

    public void setTime(@NotNull Instant time) {
        this.time = time;
    }
}
