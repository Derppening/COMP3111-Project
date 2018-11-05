package comp3111.webscraper.models;

import comp3111.webscraper.Item;
import javafx.collections.ObservableList;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class SearchRecordTest {
    @BeforeClass
    public static void setup() {
        Class<?> clazz = SearchRecord.class;
        try {
            Field f = clazz.getDeclaredField("lastSearch");
            f.setAccessible(true);

            ((ObservableList) f.get(null)).clear();
        } catch (NoSuchFieldException | IllegalAccessException e) {
            fail();
        }
    }

    @Test
    public void testCreateSearchRecord() {
        final String keyword = "iPhone";
        final Item item = new Item();
        item.setTitle("iPhone 5S");
        final List<Item> items = Collections.singletonList(item);

        SearchRecord.push(keyword, items);

        SearchRecord record = SearchRecord.peek();
        assertEquals(keyword, record.getKeyword());
        assertSame(item.getTitle(), record.getItems().get(0).getTitle());
    }

    @Test
    public void testTimeSaved() {
        SearchRecord.push("", Collections.emptyList());

        assertNotNull(SearchRecord.peek().getTimeSaved());
    }

    @Test
    public void testMultipleSearchRecord() {
        SearchRecord.push("record1", Collections.emptyList());
        SearchRecord.push("record2", Collections.emptyList());
        SearchRecord.push("record3", Collections.emptyList());

        assertEquals("record3", SearchRecord.peek().getKeyword());
        assertEquals("record2", SearchRecord.popLastSearch().getKeyword());
        assertEquals("record3", SearchRecord.peek().getKeyword());
        assertEquals("record1", SearchRecord.popLastSearch().getKeyword());
        assertEquals("record3", SearchRecord.peek().getKeyword());

        try {
            SearchRecord.popLastSearch();
            fail("Expected an exception!");
        } catch (IllegalStateException e) {
            // success
        }
    }
}
