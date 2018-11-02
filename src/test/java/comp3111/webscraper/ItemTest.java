package comp3111.webscraper;


import org.json.JSONObject;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


public class ItemTest {

    @Test
    public void testSetTitle() {
        System.out.println("test Item set title");
        Item i = new Item();
        i.setTitle("ABCDE");
        assertEquals(i.getTitle(), "ABCDE");
    }

    @Test
    public void testConstructByJSON() {
        System.out.println("test item consrtuct by JSON");
        Item i = new Item(new JSONObject("{\"title\":\"t\",\"price\":1,\"url\":\"i\",\"portal\":\"p\"}"));
        assertEquals(i.getTitle(), "t");
        assertEquals(i.getPrice(), 1, 0.01);
        assertEquals(i.getUrl(), "i");
        assertEquals(i.getPortal(), "p");
        i = new Item(new JSONObject("{}"));
        assertNull(i.getPortal());
        assertEquals(i.getPrice(), 0, 0.01);
        assertNull(i.getUrl());
        assertNull(i.getTitle());
    }
}
