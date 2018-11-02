package comp3111.webscraper;


import comp3111.webscraper.controllers.MenuController;
import comp3111.webscraper.models.SearchRecord;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import org.jetbrains.annotations.NotNull;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.Window;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;

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
                    .append(item.getUrl())
                    .append("\n");
        }
        return output.toString();
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

