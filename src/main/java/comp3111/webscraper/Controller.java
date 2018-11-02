package comp3111.webscraper;


import comp3111.webscraper.controllers.MenuController;
import comp3111.webscraper.models.SearchRecord;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.AreaChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import org.jetbrains.annotations.NotNull;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;


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

    private WebScraper scraper;
    private Application hostApplication = null;

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
        SearchRecord.push(textFieldKeyword.getText(), result);

        // TODO: Delete this later
        System.out.println(serializeItems(result));
//        textAreaConsole.setText(serializeItems(result));
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
}

