package comp3111.webscraper;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.html.*;

import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.Comparator;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.List;
import java.util.TimeZone;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * WebScraper provide a sample code that scrape web content. After it is constructed, you can call the method scrape with a keyword,
 * the client will go to the default url and parse the page by looking at the HTML DOM.
 * <br>
 * In this particular sample code, it access to craigslist.org. You can directly search on an entry by typing the URL
 * <br>
 * https://newyork.craigslist.org/search/sss?sort=rel&amp;query=KEYWORD
 * <br>
 * where KEYWORD is the keyword you want to search.
 * <br>
 * Assume you are working on Chrome, paste the url into your browser and press F12 to load the source code of the HTML. You might be freak
 * out if you have never seen a HTML source code before. Keep calm and move on. Press Ctrl-Shift-C (or CMD-Shift-C if you got a mac) and move your
 * mouse cursor around, different part of the HTML code and the corresponding the HTML objects will be highlighted. Explore your HTML page from
 * body &rarr; section class="page-container" &rarr; form id="searchform" &rarr; div class="content" &rarr; ul class="rows" &rarr; any one of the multiple
 * li class="result-row" &rarr; p class="result-info". You might see something like this:
 * <br>
 * <pre>
 * {@code
 *    <p class="result-info">
 *        <span class="icon icon-star" role="button" title="save this post in your favorites list">
 *           <span class="screen-reader-text">favorite this post</span>
 *       </span>
 *       <time class="result-date" datetime="2018-06-21 01:58" title="Thu 21 Jun 01:58:44 AM">Jun 21</time>
 *       <a href="https://newyork.craigslist.org/que/clt/d/green-star-polyp-gsp-on-rock/6596253604.html" data-id="6596253604" class="result-title hdrlnk">Green Star Polyp GSP on a rock frag</a>
 *       <span class="result-meta">
 *               <span class="result-price">$15</span>
 *               <span class="result-tags">
 *                   pic
 *                   <span class="maptag" data-pid="6596253604">map</span>
 *               </span>
 *               <span class="banish icon icon-trash" role="button">
 *                   <span class="screen-reader-text">hide this posting</span>
 *               </span>
 *           <span class="unbanish icon icon-trash red" role="button" aria-hidden="true"></span>
 *           <a href="#" class="restore-link">
 *               <span class="restore-narrow-text">restore</span>
 *               <span class="restore-wide-text">restore this posting</span>
 *           </a>
 *       </span>
 *   </p>
 * }
 * </pre>
 * <br>
 * The code
 * <pre>
 * {@code
 * List<?> items = (List<?>) page.getByXPath("//li[@class='result-row']");
 * }
 * </pre>
 * extracts all result-row and stores the corresponding HTML elements to a list called items. Later in the loop it extracts the anchor tag
 * &lsaquo; a &rsaquo; to retrieve the display text (by .asText()) and the link (by .getHrefAttribute()). It also extracts
 */
public class WebScraper {

    /**
     * Date formatter for the dates scraped from Craigslist.
     */
    private static final DateTimeFormatter DATE_TIME_FMT = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm")
            .parseDefaulting(ChronoField.SECOND_OF_MINUTE, 0)
            .toFormatter()
            .withZone(TimeZone.getDefault().toZoneId());

    /**
     * Default URL to scrape from.
     */
    private static final String DEFAULT_URL = "https://newyork.craigslist.org/";
    /**
     * Client to use for scraping.
     */
    private WebClient client;

    /**
     * Default Constructor
     */
    public WebScraper() {
        client = new WebClient();
        client.getOptions().setCssEnabled(false);
        client.getOptions().setJavaScriptEnabled(false);
    }

    /**
     * read the string in string reader
     *
     * @param rd reader that holds the string
     * @return extract string from read
     * @throws IOException if an I/O error occurs when reading.
     *
     * @author dipsywong98
     */
    private static String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    /**
     * convert json from url into jsonobject
     *
     * @param url where the json stored
     * @return the corresponding object represented by the json at the url
     * @throws IOException if an I/O error occurs when reading the URL.
     * @throws JSONException If the scraped JSON is syntactically incorrect.
     *
     * @author dipsywong98
     */
    public static JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            return new JSONObject(jsonText);
        }
    }

    /**
     * The only method implemented in this class, to scrape web content from the craigslist
     *
     * @param keyword - the keyword you want to search
     * @return A list of Item that has found. A zero size list is return if nothing is found. Null if any exception (e.g. no connectivity)
     *
     * @author dipsywong98, kevinw
     */
    public List<Item> scrape(String keyword) {
        List<Item> result = new Vector<>();
        if (keyword.length() == 0) return null;
        try {
            result.addAll(oldScrape(keyword));
            result.addAll(newScrape(keyword));
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
        result.sort(Comparator.comparingDouble(Item::getPrice));
        return result;
    }

    /**
     * Perform all tasks including pagination on old site
     *
     * @param keyword - the keyword you want to search
     * @return A list of Item that has found. A zero size list is return if nothing is found. Null if any exception (e.g. no connectivity)
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported.
     *
     * @author dipsywong98
     */
    private List<Item> oldScrape(String keyword) throws UnsupportedEncodingException {
        return oldScrapeByUrl(obtainOldScrapeUrl(keyword));
    }

    /**
     * obtain a url for scrapping
     *
     * @param keyword keyword for search
     * @return url for scrapping
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported.
     *
     * @author dipsywong98
     */
    private String obtainOldScrapeUrl(String keyword) throws UnsupportedEncodingException {
        return DEFAULT_URL + "search/sss?sort=rel&query=" + URLEncoder.encode(keyword, "UTF-8");
    }

    /**
     * Scrape the old site
     *
     * @param searchUrl in the old site
     * @return the list of items scrapped (with pagination handled). Scrapping update in terminal.
     *
     * @author dipsywong98, kevinCrylz
     */
    private List<Item> oldScrapeByUrl(String searchUrl) {
        try {
            HtmlPage page = client.getPage(searchUrl);
            //System.out.println(searchUrl);
            Vector<Item> result = new Vector<>();

            HtmlAnchor pageAnchor;
            int cnt_page = 1;

            do {
                System.out.println("Now scrapping craigslist:  page " + cnt_page++);

                List<?> items = page.getByXPath("//li[@class='result-row']");

                if (items.size() == 0) break;

                for (Object elem : items) {
                    HtmlElement htmlItem = (HtmlElement) elem;
                    HtmlAnchor itemAnchor = htmlItem.getFirstByXPath(".//p[@class='result-info']/a");
                    HtmlElement spanPrice = htmlItem.getFirstByXPath(".//a/span[@class='result-price']");
                    HtmlTime itemTime = htmlItem.getFirstByXPath(".//p[@class='result-info']/time");

                    // It is possible that an item doesn't have any price, we set the price to 0.0
                    // in this case
                    String itemPrice = spanPrice == null ? "0.0" : spanPrice.asText();

                    Item item = new Item();
                    item.setTitle(itemAnchor.asText());
                    item.setUrl(itemAnchor.getHrefAttribute());
                    item.setTime(DATE_TIME_FMT.parse(itemTime.getAttribute("datetime"), Instant::from));

                    item.setPrice(new Double(itemPrice.replace("$", "")));
                    item.setPortal("craigslist");

                    result.add(item);
                }
                //System.out.println(items.size());

                pageAnchor = page.getFirstByXPath("//a[@class='button next']");
                page = client.getPage(searchUrl.substring(0, searchUrl.indexOf("/search")) + pageAnchor.getHrefAttribute());
                //System.out.println(pageAnchor.getHrefAttribute());
            } while (pageAnchor.getHrefAttribute().length() != 0 && cnt_page <6);
            client.close();
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Perform all tasks including pagination on new site
     *
     * @param keyword - the keyword you want to search
     * @return A list of Item that has found. A zero size list is return if nothing is found. Null if any exception (e.g. no connectivity)
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported
     *
     * @author dipsywong98
     */
    private List<Item> newScrape(String keyword) throws UnsupportedEncodingException {
        return newScrapeByUrl(obtainNewScrapeUrl(keyword));
    }

    /**
     * get a new site search url
     *
     * @param keyword query string
     * @return a url in new site for scrapping
     * @throws UnsupportedEncodingException if UTF-8 encoding is not supported
     *
     * @author dipsywong98
     */
    private String obtainNewScrapeUrl(String keyword) throws UnsupportedEncodingException {
        return "http://api.walmartlabs.com/v1/search?apiKey=knd7pc96vzfvzjb7h6ywz74x&query=" + URLEncoder.encode(keyword, "UTF-8");
    }

    /**
     * perform the scrapping task in new site
     *
     * @param url scrapping url in new site, actually is the api endpoint
     * @return the list of item obtained
     *
     * @author dipsywong98
     */
    private List<Item> newScrapeByUrl(String url) {
        try {
            Vector<Item> result = new Vector<>();
            JSONObject object = readJsonFromUrl(url);
            int length = object.optInt("numItems");
            if (length == 0) {
                return result;
            } else {
                JSONArray rawItems = (JSONArray) object.get("items");
                for (int i = 0; i < length; i++) {
                    JSONObject rawItem = (JSONObject) rawItems.get(i);
                    Item item = new Item();
                    item.setTitle(rawItem.optString("name"));
                    item.setPrice(rawItem.optDouble("salePrice"));
                    item.setUrl(rawItem.optString("addToCartUrl"));
                    item.setPortal("walmart");
                    result.add(item);
                }
                return result;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}