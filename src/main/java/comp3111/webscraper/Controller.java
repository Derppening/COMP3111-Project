package comp3111.webscraper;


import comp3111.webscraper.controllers.MenuController;
import comp3111.webscraper.models.SearchRecord;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;


/**
 * @author kevinw
 * <p>
 * <p>
 * Controller class that manage GUI interaction. Please see document about JavaFX for details.
 */
public class Controller {

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

        textAreaConsole.setText(serializeItems(result));
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
        return output.toString();
    }
}

