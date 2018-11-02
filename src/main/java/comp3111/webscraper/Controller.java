package comp3111.webscraper;


import comp3111.webscraper.controllers.MenuController;
import comp3111.webscraper.models.SearchRecord;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import org.jetbrains.annotations.NotNull;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import javafx.event.ActionEvent;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * @author kevinw
 * <p>
 * <p>
 * Controller class that manage GUI interaction. Please see document about JavaFX for details.
 */
public class Controller {

    private static final DateTimeFormatter DATE_TIME_FMT = new DateTimeFormatterBuilder()
            .appendPattern("MM/dd/yyyy")
            .parseDefaulting(ChronoField.NANO_OF_DAY, 0)
            .toFormatter()
            .withZone(TimeZone.getTimeZone("UTC").toZoneId());

    private static final List<Instant> SEVEN_DAY_INSTANTS;
    static {
        SEVEN_DAY_INSTANTS = new ArrayList<>(8);
        for (int i = 0; i < 8; ++i) {
             SEVEN_DAY_INSTANTS.add(Instant.now().truncatedTo(ChronoUnit.DAYS).minus(i, ChronoUnit.DAYS));
        }
    }

    @FXML
    public Tab trendTab;

    @FXML
    private ComboBox<String> searchRecordComboBox;

    @FXML
    private AreaChart<String, Double> areaChart;

    @FXML
    private TabPane tabPane;

    @FXML
    private MenuItem itemLastSearch;

    @FXML
    private Label labelCount;

    @FXML
    private Label labelPrice;

    @FXML
    private Hyperlink labelMin;

    @FXML
    private Hyperlink labelLatest;

    @FXML
    private TextField textFieldKeyword;

    @FXML
    private TextArea textAreaConsole;

    @FXML
    private VBox root;

    private WebScraper scraper;
    private Application hostApplication = null;

    private List<Item> activeSearchResult;

    private String activeSearchKeyword;

    private class OnTabChangeListener implements ChangeListener<Tab> {

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
     */
    void setHostApplication(@NotNull Application app) {
        this.hostApplication = app;
    }

    /**
     * Default initializer.
     */
    @FXML
    private void initialize() {
        if (SearchRecord.canLoad()) {
            itemLastSearch.setDisable(false);
        }

        tabPane.getSelectionModel().selectedItemProperty().addListener(new OnTabChangeListener());
        SearchRecord.addObserver(o -> updateRecordComboBox());
    }

    /**
     * Invoked when the "About the Team" menu item is clicked.
     */
    @FXML
    private void actionDisplayTeamInfo() {
        MenuController.displayTeamInfo();
    }

    /**
     * Invoked when "Close" menu item is clicked.
     */
    @FXML
    private void actionClose() {
        MenuController.closeSearch((WebScraperApplication) hostApplication);
    }

    /**
     * Invoked when "Quit" menu item is clicked.
     */
    @FXML
    private void actionQuit() {
        Platform.exit();
    }

    /**
     * Called when the search button is pressed.
     */
    @FXML
    private void actionSearch() {
        System.out.println("actionSearch: " + textFieldKeyword.getText());

        List<Item> result = scraper.scrape(textFieldKeyword.getText());
        if (result != null) {
            activeSearchResult = result;
            activeSearchKeyword = textFieldKeyword.getText();

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
     */
    @FXML
    private void actionLastSearch() {
        if (!SearchRecord.canLoad()) {
            throw new IllegalStateException("actionLastSearch should not be invokable");
        }

        SearchRecord lastSearch = SearchRecord.popLastSearch();
        itemLastSearch.setDisable(true);
        textFieldKeyword.setText(lastSearch.getKeyword());
        textAreaConsole.setText(serializeItems(lastSearch.getItems()));

        System.out.println("Loaded query \"" + lastSearch.getKeyword() + "\" from " + lastSearch.getTimeSaved().toString());

        // TODO(Derppening): Invoke other functions to restore other tabs
    }

    @FXML
    private void actionComboBoxSelect() {
        int index = searchRecordComboBox.getSelectionModel().getSelectedIndex();
        SearchRecord record = SearchRecord.get(index);

        if (record == null) {
            return;
        }

        areaChart.getData().clear();
        areaChart.setAnimated(false);
        areaChart.getXAxis().setLabel("Date in mm/dd/yyyy format");
        areaChart.getYAxis().setLabel("The average selling price of " + record.getKeyword());

        XYChart.Series<String, Double> series = new XYChart.Series<>();
        for (Instant i : SEVEN_DAY_INSTANTS) {
            Stream<Item> filteredItems = record.getItems()
                    .parallelStream()
                    .filter(item -> item.getTime().truncatedTo(ChronoUnit.DAYS).equals(i));
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
        areaChart.getData().add(series);

        areaChart.getData()
                .forEach(s -> s.getData().forEach(data -> data.getNode().setOnMouseClicked(event -> {
                    if (event.getButton().equals(MouseButton.PRIMARY) && event.getClickCount() == 2) {
                        textAreaConsole.clear();
                        // TODO(Derppening): Remove this
                        System.out.println(record.getItems().parallelStream().filter(item -> item.getTime().truncatedTo(ChronoUnit.DAYS).equals(data.getExtraValue())).collect(Collectors.toList()));
//                        textAreaConsole.setText(serializeItems(record.getItems().parallelStream().filter(item -> item.getTime().truncatedTo(ChronoUnit.DAYS).equals(data.getExtraValue())).collect(Collectors.toList())));

                        setAreaChartColors(s.getData(), data);
                    }
                })));
        areaChart.getData().forEach(s -> setAreaChartColors(s.getData(), null));
    }

    private void setAreaChartColors(List<XYChart.Data<String, Double>> series, XYChart.Data<String, Double> hData) {
        // TODO(Derppening): Static everything!

        series.forEach(data -> data.getNode().setStyle("-fx-background-color: rgb(0, 255, 0)"));
        if (hData != null) {
            hData.getNode().setStyle("-fx-background-color: rgb(255, 0, 0)");
        }
    }

    /**
     * Serializes a list of items to text for displaying in the {@link Controller#textAreaConsole}.
     *
     * @param items List of items to serialize.
     * @return Items serialized in the format "$title\t$price\t$url".
     */
    private String serializeItems(List<Item> items) {
        StringBuilder output = new StringBuilder();
        for (Item item : items) {
            output.append(item.getTitle())
                    .append("\t")
                    .append(item.getPrice())
                    .append("\t")
                    .append(item.getTime())
                    .append("\t")
                    .append(item.getUrl())
                    .append("\n");
        }
        return output.toString().trim();
    }

    /**
     * Updates the search record combo box from {@link SearchRecord}.
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
     */
    private void clearConsole() {
        textAreaConsole.setText("");
    }

    /**
     * append the str to console
     *
     * @param str the appended string
     */
    private void printConsole(String str) {
        textAreaConsole.appendText(str);
    }

    /**
     * print out the most result/ loaded search result
     */
    private void printActiveSearchResult() {
        StringBuilder output = new StringBuilder();
        if (activeSearchResult == null) return;
        output.append(textAreaConsole.getText());
        for (Item item : activeSearchResult) {
            output.append(item.getTitle())
                    .append("\t")
                    .append(item.getPrice())
                    .append("\t")
                    .append(item.getPortal())
                    .append("\t")
                    .append(item.getUrl())
                    .append("\n");
        }
        textAreaConsole.setText(output.toString());
    }

    /**
     * Called when the new button is pressed. Very dummy action - print something in the command prompt.
     */
    @FXML
    private void actionNew() {
        System.out.println("actionNew");
    }

    /**
     * Called when going to save
     */
    @FXML
    private void actionSave() {
//        System.out.println(activeSearchResultArray.toString());
//        System.out.println(outputJson);

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
     * @throws IOException
     */
    public void saveFile(File file) throws IOException {
        JSONArray activeSearchResultArray = new JSONArray(activeSearchResult);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("keyword", activeSearchKeyword);
        jsonObject.put("result", activeSearchResultArray);
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
     * Called when going to save
     */
    @FXML
    private void actionOpen() {
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
     * @throws IOException
     */
    public void openFile(File file) throws IOException {
        String inputJson = readFile(file);
        JSONObject inputObject = new JSONObject(inputJson);
        activeSearchKeyword = inputObject.optString("keyword");
        JSONArray result = (JSONArray) inputObject.get("result");
        activeSearchResult = new ArrayList<>();
        for (int i = 0; i < result.length(); i++) {
//                    activeSearchResult.add((Item)result.get(i));
            activeSearchResult.add(new Item(result.getJSONObject(i)));
        }
        clearConsole();
        printConsole("--Data Loading from " + file.getAbsolutePath() + "--\n");
        printActiveSearchResult();
    }

    /**
     * Read file into string
     *
     * @param file file to read
     * @return string in file
     * @throws IOException
     */
    private String readFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        String str = "";
        byte buf[] = new byte[8];
        int bufSize;
        while (inputStream.available() > 0) {
            bufSize = inputStream.read(buf);
            str += (new String(buf, 0, bufSize));
        }
        inputStream.close();
        return str;
    }

    /**
     * For testing advance 2, create some result
     *
     * @return the new search result
     */
    public List<Item> testGenerateDummieResult() {
        activeSearchResult = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Item item = new Item();
            item.setPortal("some portal");
            item.setPrice(i);
            item.setTitle("item" + i);
            item.setUrl("http://some.link");
            activeSearchResult.add(item);
        }
        activeSearchKeyword = "testing";
        return activeSearchResult;
    }

    /**
     * For testing advance2, make the active search empty
     */
    public void testClearActiveResult() {
        activeSearchResult = new ArrayList<>();
        activeSearchKeyword = "";
    }

    /**
     * For testing to take the active search result
     *
     * @return the current active search result
     */
    public List<Item> testPeekSearchResult() {
        return activeSearchResult;
    }
}

