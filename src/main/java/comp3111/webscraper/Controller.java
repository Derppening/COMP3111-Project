package comp3111.webscraper;


import comp3111.webscraper.controllers.MenuController;
import comp3111.webscraper.models.SearchRecord;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Window;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Math;
import java.lang.reflect.Field;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/**
 * Controller class that manage GUI interaction. Please see document about JavaFX for details.
 *
 * @author kevinw
 */
public class Controller {

    /**
     * Date formatter for the "Date" axis of the Trend graph.
     */
    private static final DateTimeFormatter DATE_TIME_FMT = new DateTimeFormatterBuilder()
            .appendPattern("MM/dd/yyyy")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(TimeZone.getTimeZone("UTC").toZoneId());
    /**
     * Color for highlighted data point in the Trend graph.
     */
    private static final String HIGHTLIGHT_CHART_COLOR = "rgb(255, 0, 0)";
    /**
     * Immutable list of {@link Instant} representing the past 8 days (including today).
     */
    private static final List<Instant> SEVEN_DAY_INSTANTS;

    static {
        List<Instant> instants = new ArrayList<>(8);
        for (int i = 0; i < 8; ++i) {
            instants.add(Instant.now().truncatedTo(ChronoUnit.DAYS).minus(i, ChronoUnit.DAYS));
        }
        SEVEN_DAY_INSTANTS = Collections.unmodifiableList(instants);
    }

    /**
     * FXML element for DistributionTab.
     */
    @FXML
    public Tab distributionTab;

    /**
     * FXML element for the histogram in the Distribution Tab.
     */
    @FXML
    public BarChart<String, Number> barChartHistogram;

    /**
     * FXML element for TrendTab.
     */
    @FXML
    public Tab trendTab;

    /**
     * FXML element for the search record combo box in the Trend Tab.
     */
    @FXML
    public ComboBox<String> searchRecordComboBox;

    /**
     * FXML element for the area chart in the Trend Tab.
     */
    @FXML
    public AreaChart<String, Double> areaChart;

    /**
     * FXML element for the pane containing all tabs.
     */
    @FXML
    public TabPane tabPane;

    /**
     * FXML element for "Last Search" item in the "File" menu.
     */
    @FXML
    public MenuItem itemLastSearch;

    /**
     * FXML element for "Total" label in the "Summary" tab.
     */
    @FXML
    public Label labelCount;

    /**
     * FXML element for "AvgPrice" label in the "Summary" tab.
     */
    @FXML
    public Label labelPrice;

    /**
     * FXML element for the "Lowest" label in the "Summary" tab.
     */
    @FXML
    public Hyperlink labelMin;

    /**
     * FXML element for the "Latest" label in the "Summary" tab.
     */
    @FXML
    public Hyperlink labelLatest;

    /**
     * FXML element for the keyword text field.
     */
    @FXML
    public TextField textFieldKeyword;

    /**
     * FXML element for the console text area in the "Console" tab.
     */
    @FXML
    public TextArea textAreaConsole;

    /**
     * FXML element for the scene root.
     */
    @FXML
    public VBox root;

    /**
     * Instance of scraper.
     */
    private WebScraper scraper;

    /**
     * Reference to the JavaFX application.
     */
    private Application hostApplication = null;

    /**
     * Listener for tab change events.
     *
     * @author Derppening
     */
    private class OnTabChangeListener implements ChangeListener<Tab> {

        /**
         * Handles "Changed" events from tab change.
         *
         * @param observable Value which was changed.
         * @param oldValue Originally focused tab.
         * @param newValue New focused tab.
         */
        @Override
        public void changed(ObservableValue<? extends Tab> observable, Tab oldValue, Tab newValue) {
            if (newValue == trendTab) {
                updateRecordComboBox();
            }
        }
    }

    /**
     * Default controller
     */
    public Controller() {
        scraper = new WebScraper();
    }

    /**
     * Sets the reference of the host application.
     *
     * @param app Current instance of {@link javafx.application.Application}.
     *
     * @author Derppening
     */
    void setHostApplication(@NotNull Application app) {
        this.hostApplication = app;
    }

    /**
     * @author Derppening
     *
     * Default initializer.
     */
    @FXML
    private void initialize() {
        if (SearchRecord.canLoad()) {
            itemLastSearch.setDisable(false);
        } else {
            itemLastSearch.setDisable(true);
        }

        tabPane.getSelectionModel().selectedItemProperty().addListener(new OnTabChangeListener());
        SearchRecord.addObserver(o -> updateRecordComboBox());
    }

    /**
     * Invoked when the "About the Team" menu item is clicked.
     *
     * @author Derppening
     */
    @FXML
    public void actionDisplayTeamInfo() {
        MenuController.displayTeamInfo();
    }

    /**
     * Invoked when "Close" menu item is clicked.
     *
     * @author Derppening
     */
    @FXML
    public void actionClose() {
        MenuController.closeSearch((WebScraperApplication) hostApplication);
    }

    /**
     * Invoked when "Quit" menu item is clicked.
     *
     * @author Derppening
     */
    @FXML
    public void actionQuit() {
        Platform.exit();
    }

    /**
     * Called when the search button is pressed.
     *
     * @author kevinCrylz
     */
    @FXML
    public void actionSearch() {
        System.out.println("actionSearch: " + textFieldKeyword.getText());

        List<Item> result = scraper.scrape(textFieldKeyword.getText());

        //Building histogram
        updateHistogram(textFieldKeyword.getText(), result);

        if (result != null) {
            SearchRecord.push(textFieldKeyword.getText(), result);

            clearConsole();
            printActiveSearchResult();
        }

        if (SearchRecord.canLoad()) {
            itemLastSearch.setDisable(false);
        }
    }

    /**
     * Called when "Last Search" menu item is clicked.
     *
     * @author Derppening
     */
    @FXML
    public void actionLastSearch() {
        if (!SearchRecord.canLoad()) {
            throw new IllegalStateException("actionLastSearch should not be invokable");
        }

        SearchRecord lastSearch = SearchRecord.popAndGet();
        itemLastSearch.setDisable(true);
        textFieldKeyword.setText(lastSearch.getKeyword());
        textAreaConsole.setText(serializeItems(lastSearch.getItems()));

        System.out.println("Loaded query \"" + lastSearch.getKeyword() + "\" from " + lastSearch.getTimeSaved().toString());
    }

    /**
     * Called when an entry in the combo box is selected.
     *
     * @author Derppening
     */
    @FXML
    public void actionComboBoxSelect() {
        int index = searchRecordComboBox.getSelectionModel().getSelectedIndex();
        SearchRecord record = SearchRecord.get(index);

        if (record == null) {
            return;
        }

        // reset the data and axis
        areaChart.getData().clear();
        areaChart.setAnimated(false);
        areaChart.getXAxis().setLabel("Date in mm/dd/yyyy format");
        areaChart.getYAxis().setLabel("The average selling price of " + record.getKeyword());

        // insert the data
        areaChart.getData().add(mapDataToSeries(record));

        // enable double click event for all data points
        areaChart.getData().forEach(s ->
                s.getData().forEach(data ->
                        data.getNode().setOnMouseClicked(event -> {
                            // if double click
                            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                                // filter all entries of that day, and display on the text console
                                List<Item> items = record.getItems()
                                        .parallelStream()
                                        .filter(item -> item.getTime() != null)
                                        .filter(item -> item.getTime().truncatedTo(ChronoUnit.DAYS).equals(data.getExtraValue()))
                                        .collect(Collectors.toList());

                                clearConsole();
                                textAreaConsole.setText(serializeItems(items));

                                // make the color of that point "different"
                                setAreaChartColors(s.getData(), data);
                            }
                        })));
        // reset all the colors of the data points
        areaChart.getData().forEach(s -> setAreaChartColors(s.getData(), null));
    }


    /**
     * Called when there is a new search to update the distribution tab.
     *
     * @param keyword search keyword
     * @param items List containing returned items of search result
     *
     * @author kevinCrylz
     */
    private void updateHistogram(String keyword, List<Item> items) {
        barChartHistogram.setTitle("The selling price of " + keyword);
        barChartHistogram.getXAxis().setLabel("Price Range");
        barChartHistogram.getYAxis().setLabel("Frequency");

        barChartHistogram.setAnimated(false);
        //barChartHistogram.setBarGap(0);
        //barChartHistogram.setCategoryGap(0);

        barChartHistogram.getData().clear();

        if (items != null) {
            // create new histogram
            barChartHistogram.getData().add(checkFrequency(items));

            barChartHistogram.getData().forEach(s1 -> s1.getData().forEach(data1 -> data1.getNode().setStyle("-fx-bar-fill: orange")));

            barChartHistogram.getData().forEach(s ->
                    s.getData().forEach(data ->
                        data.getNode().setOnMouseClicked(event -> {
                            if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                                double lowPrice = Double.valueOf(data.getXValue().split("-")[0]);
                                double highPrice = Double.valueOf(data.getXValue().split("-")[1]);

                                clearConsole();

                                if (lowPrice != highPrice) {
                                    List<Item> filteredItems = items.parallelStream()
                                            .filter(item -> item.getPrice() >= lowPrice)
                                            .filter(item -> item.getPrice() < highPrice)
                                            .collect(Collectors.toList());

                                    textAreaConsole.setText(serializeItems(filteredItems));
                                } else {
                                    textAreaConsole.setText(serializeItems(items));
                                }

                                barChartHistogram.getData().forEach(s1 -> s1.getData().forEach(data1 -> data1.getNode().setStyle("-fx-bar-fill: orange")));
                                data.getNode().setStyle("-fx-bar-fill: #e5671d");
                            }
                        })));
        }
    }

    /**
     * Helper function for categorizing {@link Item} into ten price range.
     *
     * @param items Items returned from search
     * @return {@link XYChart.Series} containing a list of frequencies of ten price range.
     *
     * @author kevinCrylz
     */
    private XYChart.Series<String, Number> checkFrequency(List<Item> items) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        double lowPrice = Math.round((items.get(0).getPrice()-5)/10.0)*10;
        double highPrice = Math.round((items.get(items.size()-1).getPrice()+5)/10.0)*10;
        double d = (highPrice - lowPrice) / 10;

        if (lowPrice == highPrice) {
            series.getData().add(new XYChart.Data<String, Number>(""+lowPrice+"-"+highPrice, items.size()));
            return series;
        }

        int cnt_data[] = new int[10];
        double price;

        for (Item item : items) {
            price = item.getPrice();
            for (int i = 1; i <= 10; i++) {
                if (price > highPrice - d*i) {
                    cnt_data[10-i] += 1;
                    break;
                }
            }
        }

        for (int i=0; i<10; i++)
            series.getData().add(new XYChart.Data<String, Number>(  ""+(lowPrice + d*i)+"-"+(lowPrice + d*(i+1)), cnt_data[i]));

        return series;
    }

    /**
     * Helper function for mapping a {@link SearchRecord} data into {@link XYChart.Series} for displaying
     * on the chart.
     *
     * @param record Record to map into a series.
     * @return {@link XYChart.Series} containing a list of average prices per day.
     *
     * @author Derppening
     */
    private XYChart.Series<String, Double> mapDataToSeries(SearchRecord record) {
        XYChart.Series<String, Double> series = new XYChart.Series<>();
        for (Instant i : SEVEN_DAY_INSTANTS) {
            Stream<Item> filteredItems = record.getItems()
                    .parallelStream()
                    .filter(item -> item.getTime() != null)
                    .filter(item -> item.getTime().truncatedTo(ChronoUnit.DAYS).equals(i))
                    .filter(item -> item.getPrice() != 0.0);
            double average = filteredItems
                    .mapToDouble(Item::getPrice)
                    .average()
                    .orElse(0.0);

            if (average != 0.0) {
                String s = DATE_TIME_FMT.format(i);

                AreaChart.Data<String, Double> data = new AreaChart.Data<>(s, average, i);
                series.getData().add(0, data);
            }
        }

        return series;
    }

    /**
     * Set the color of the area chart.
     *
     * @param series The series of data points to format.
     * @param hData The data point to highlight.
     *
     * @author Derppening
     */
    private void setAreaChartColors(List<XYChart.Data<String, Double>> series, XYChart.Data<String, Double> hData) {
        series.forEach(data -> data.getNode().setStyle(null));
        if (hData != null) {
            hData.getNode().setStyle("-fx-background-color: " + HIGHTLIGHT_CHART_COLOR);
        }
    }

    /**
     * Serializes a list of items to text for displaying in the {@link Controller#textAreaConsole}.
     *
     * @param items List of items to serialize.
     * @return Items serialized in the format "$title\t$price\t$portal\t$url".
     *
     * @author Derppening, dipsywong98
     */
    private static String serializeItems(List<Item> items) {
        StringBuilder output = new StringBuilder();
        for (Item item : items) {
            output.append(item.getTitle())
                    .append("\t")
                    .append(item.getPrice())
                    .append("\t")
                    .append(item.getPortal())
                    .append("\t")
                    .append(item.getUrl())
                    .append("\n");
        }
        return output.toString().trim();
    }

    /**
     * Updates the search record combo box from {@link SearchRecord}.
     *
     * @author Derppening
     */
    private void updateRecordComboBox() {
        searchRecordComboBox.getSelectionModel().clearSelection();

        List<String> keywords = SearchRecord.view()
                .stream()
                .map(SearchRecord::getKeyword)
                .collect(Collectors.toList());

        searchRecordComboBox.setItems(FXCollections.observableArrayList(keywords));
    }

    /**
     * clear console
     *
     * @author dipsywong98
     */
    private void clearConsole() {
        textAreaConsole.setText("");
    }

    /**
     * append the str to console
     *
     * @param str the appended string
     *
     * @author dipsywong98
     */
    private void printConsole(String str) {
        textAreaConsole.appendText(str);
    }

    /**
     * print out the most result/ loaded search result
     *
     * @author dipsywong98
     */
    private void printActiveSearchResult() {
        String output = textAreaConsole.getText() + serializeItems(SearchRecord.peek().getItems());
        textAreaConsole.setText(output);
    }

    /**
     * Called when going to save
     *
     * @author dipsywong98
     */
    @FXML
    public void actionSave() {
        Window stage = root.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new java.io.File("."));
        fileChooser.setTitle("Save Search");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Webscrapper File(*.3111)", "*.3111"));
        File file = fileChooser.showSaveDialog(stage);
        try {
            saveFile(file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Advance2 save the search record
     *
     * @param file .3111 file target to save to
     * @throws IOException if the file cannot be saved
     *
     * @author dipsywong98
     */
    public void saveFile(File file) throws IOException {
        SearchRecord record = SearchRecord.peek();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("keyword", record.getKeyword());
        jsonObject.put("result", record.getItems());
        String outputJson = jsonObject.toString();

        if (!file.getName().contains(".")) {
            file = new File(file.getAbsolutePath() + ".3111");
            System.out.println("add .3111 triggered");
        }
        FileOutputStream fooStream = new FileOutputStream(file, false);
        fooStream.write(outputJson.getBytes());
        fooStream.close();
    }

    /**
     * Called when going to open
     *
     * @author dipsywong98
     */
    @FXML
    public void actionOpen() {
        Window stage = root.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setInitialDirectory(new java.io.File("."));
        fileChooser.setTitle("Open Search");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Webscrapper File(*.3111)", "*.3111"));
        File file = fileChooser.showOpenDialog(stage);
        try {
            openFile(file);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * Advance2 load search history
     *
     * @param file the .3111 file to load
     * @throws IOException if an I/O error occurred while reading the file.
     *
     * @author dipsywong98
     */
    public void openFile(File file) throws IOException {
        String inputJson = readFile(file);
        JSONObject inputObject = new JSONObject(inputJson);
        JSONArray result = (JSONArray) inputObject.get("result");

        ArrayList<Item> results = new ArrayList<>();
        for (int i = 0; i < result.length(); ++i) {
            results.add(new Item(result.getJSONObject(i)));
        }

        SearchRecord.push(inputObject.optString("keyword"), results);

        clearConsole();
        printConsole("--Data Loading from " + file.getAbsolutePath() + "--\n");
        printActiveSearchResult();
    }

    /**
     * Read file into string
     *
     * @param file file to read
     * @return string in file
     * @throws IOException if an I/O error occurred while reading the file.
     *
     * @author dipsywong98
     */
    private String readFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        StringBuilder str = new StringBuilder();
        byte[] buf = new byte[8];
        int bufSize;
        while (inputStream.available() > 0) {
            bufSize = inputStream.read(buf);
            str.append(new String(buf, 0, bufSize));
        }
        inputStream.close();
        return str.toString();
    }

    /**
     * For testing advance 2, create some result
     *
     * @return the new search result
     *
     * @author dipsywong98
     */
    public List<Item> testGenerateDummieResult() {
        ArrayList<Item> results = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Item item = new Item();
            item.setPortal("some portal");
            item.setPrice(i);
            item.setTitle("item" + i);
            item.setUrl("http://some.link");
            results.add(item);
        }

        SearchRecord.push("testing", results);
        return SearchRecord.peek().getItems();
    }

    /**
     * For testing advance2, make the active search empty
     *
     * @author dipsywong98
     */
    public void testClearActiveResult() {
        Class<?> clazz = SearchRecord.class;
        try {
            Field recordsField = clazz.getDeclaredField("lastSearch");
            recordsField.setAccessible(true);

            @SuppressWarnings("unchecked") ObservableList<SearchRecord> record = ((ObservableList<SearchRecord>) recordsField.get(null));
            record.remove(record.size() - 1);
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    /**
     * For testing to take the active search result
     *
     * @return the current active search result
     *
     * @author dipsywong98
     */
    public List<Item> testPeekSearchResult() {
        return SearchRecord.peek().getItems();
    }
}
