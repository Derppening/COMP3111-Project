package comp3111.webscraper;


import org.jetbrains.annotations.NotNull;

import java.time.Instant;

public class Item {
    private String title;
    private double price;
    private String url;
    private Instant time;

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

    public Instant getTime() {
        return time;
    }

    public void setTime(@NotNull Instant time) {
        this.time = time;
    }
}
