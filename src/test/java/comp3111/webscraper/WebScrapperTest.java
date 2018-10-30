package comp3111.webscraper;

import org.junit.Test;

import java.util.List;

import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertEquals;

public class WebScrapperTest {
    @Test
    public void testScrapeTypical(){
        System.out.println("testScrapeTypical");
        WebScraper webScraper = new WebScraper();
        List<Item> result = webScraper.scrape("iphone");
        assertNotNull(result);
    }

    @Test
    public void testScrapeEmpty(){
        System.out.println("testScrapeEmpty");
        WebScraper webScraper = new WebScraper();
        List<Item> result = webScraper.scrape("");
        assertEquals(result,null);
    }

    @Test
    public void testScrapeNoResult(){
        System.out.println("testScrapeNoResult");
        WebScraper webScraper = new WebScraper();
        List<Item> result = webScraper.scrape("hsieghseiofjseoigbseiofjsoiegbhseoidnvseioghseiofsneiogbhseiofjsemigonseiobhsjiofjsegnbsiojsioefnboiesv");
        assertEquals(result.size(),0);
    }

    @Test
    public void testScrapeStrangeKeyword(){
        System.out.println("testScrapeStrangeKryword");
        WebScraper webScraper = new WebScraper();
        List<Item> result = webScraper.scrape("\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B\uD83D\uDE0B");
        assertNotNull(result);
    }
}
