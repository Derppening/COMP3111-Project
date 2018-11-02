package comp3111.webscraper.models;

import comp3111.webscraper.Item;
import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.util.*;

/**
 * Container for storing previous searches. Modeled similar to a {@link java.util.Stack}.
 */
public class SearchRecord {
    /**
     * Stack storing all search records.
     */
    private static ObservableList<SearchRecord> lastSearch = FXCollections.observableList(new ArrayList<>());

    /**
     * Keyword used to conduct the search.
     */
    private final String keyword;
    /**
     * Items returned by the search.
     */
    private final List<Item> items;
    /**
     * Time when this search is conducted.
     */
    private final Instant timeSaved;

    /**
     * Private constructor for creating an object.
     *
     * @param keyword Keyword for the query.
     * @param items Items returned by the query.
     */
    private SearchRecord(@NotNull String keyword, @NotNull List<Item> items) {
        this.keyword = keyword;
        this.items = items;
        this.timeSaved = Instant.now();
    }

    /**
     * @return Time when this record is saved into the stack.
     */
    public Instant getTimeSaved() {
        return timeSaved;
    }

    /**
     * @return List of items scraped by this query.
     */
    public List<Item> getItems() {
        return items;
    }

    /**
     * @return Keyword used to initiate this query.
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * Adds a record into the search history.
     *
     * @param keyword Keyword used to initiate the search.
     * @param items Items returned by the search.
     */
    public static void push(@NotNull String keyword, @NotNull List<Item> items) {
        synchronized (SearchRecord.class) {
            lastSearch.add(new SearchRecord(keyword, items));
        }
    }

    /**
     * Whether a "last search" can be loaded, i.e. if two or more searches have been initiated.
     *
     * @return True if a "last search" can be loaded.
     */
    public static boolean canLoad() {
        return lastSearch.size() >= 2;
    }

    /**
     * Removes the "last search" of the search history.
     *
     * Note that in this context, the "last search" is actually the second-to-last search in the stack, because the
     * topmost search will be the current search.
     *
     * @return Pair of keyword used to initiate the search, and a list of items returned by the original search.
     */
    public static @NotNull SearchRecord popLastSearch() {
        if (!canLoad()) {
            throw new IllegalStateException("Cannot pop search results when <2 queries are conducted!");
        }

        return lastSearch.remove(lastSearch.size() - 2);
    }

    /**
     * Peeks at the topmost element of the search history.
     *
     * This is useful when the current search is required for this operation.
     *
     * @return Most recent/current record of the search history.
     */
    public static @Nullable SearchRecord peek() {
        return lastSearch.get(lastSearch.size() - 1);
    }

    /**
     * Retrieves a record from the specified index.
     *
     * @param index Index number.
     * @return Record with the specified index, or null if such element does not exist.
     */
    public static @Nullable SearchRecord get(int index) {
        if (index >= lastSearch.size() || index < 0) {
            return null;
        }

        return lastSearch.get(index);
    }

    /**
     * @return Read-only view of the search records.
     */
    public static @NotNull List<SearchRecord> view() {
        return Collections.unmodifiableList(lastSearch);
    }

    /**
     * Adds an observer for monitoring {@link SearchRecord#lastSearch}.
     *
     * @param o Observer to execute.
     */
    public static void addObserver(InvalidationListener o) {
        lastSearch.addListener(o);
    }
}
