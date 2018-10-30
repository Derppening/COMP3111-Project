/**
 *
 */
package comp3111.webscraper;


import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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
 *
 * @author kevinw
 *
 *
 * Controller class that manage GUI interaction. Please see document about JavaFX for details.
 *
 */
public class Controller {

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

    private List<Item> activeSearchResult;

    private String activeSearchKeyword;

    /**
     * Default controller
     */
    public Controller() {
        scraper = new WebScraper();
    }

    /**
     * Default initializer. It is empty.
     */
    @FXML
    private void initialize() {

    }

    /**
     * Called when the search button is pressed.
     */
    @FXML
    private void actionSearch() {
        System.out.println("actionSearch: " + textFieldKeyword.getText());
        List<Item> result = scraper.scrape(textFieldKeyword.getText());
        if(result!=null){
            activeSearchResult = result;
            activeSearchKeyword = textFieldKeyword.getText();
            clearConsole();
            printActiveSearchResult();
        }
        labelCount.setText("Hi");
    }

    /**
     * clear console
     */
    private void clearConsole(){
        textAreaConsole.setText("");
    }

    /**
     * append the str to console
     * @param str the appended string
     */
    private void printConsole(String str){
        textAreaConsole.appendText(str);
    }

    /**
     * print out the most result/ loaded search result
     */
    private void printActiveSearchResult(){
        StringBuilder output = new StringBuilder();
        if(activeSearchResult == null)return;
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
    private void actionSave(){
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
     * @param file .3111 file target to save to
     * @throws IOException
     */
    public void saveFile(File file) throws IOException {
        JSONArray activeSearchResultArray = new JSONArray(activeSearchResult);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("keyword",activeSearchKeyword);
        jsonObject.put("result",activeSearchResultArray);
        String outputJson = jsonObject.toString();

        if(!file.getName().contains(".")) {
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
    private void actionOpen(){
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
     * @param file the .3111 file to load
     * @throws IOException
     */
    public void openFile(File file) throws IOException {
        String inputJson = readFile(file);
        JSONObject inputObject = new JSONObject(inputJson);
        activeSearchKeyword = inputObject.optString("keyword");
        JSONArray result = (JSONArray) inputObject.get("result");
        activeSearchResult = new ArrayList<>();
        for(int i=0 ;i<result.length();i++){
//                    activeSearchResult.add((Item)result.get(i));
            activeSearchResult.add(new Item(result.getJSONObject(i)));
        }
        clearConsole();
        printConsole("--Data Loading from "+file.getAbsolutePath()+"--\n");
        printActiveSearchResult();
    }

    /**
     * Read file into string
     * @param   file file to read
     * @return  string in file
     * @throws IOException
     */
    private String readFile(File file) throws IOException {
        FileInputStream inputStream = new FileInputStream(file);
        String str = "";
        byte buf[] = new byte[8];
        int bufSize;
        while (inputStream.available() > 0){
            bufSize = inputStream.read(buf);
            str+=(new String(buf, 0, bufSize));
        }
        inputStream.close();
        return str;
    }

    /**
     * For testing advance 2, create some result
     * @return the new search result
     */
    public List<Item> testGenerateDummieResult(){
        activeSearchResult = new ArrayList<>();
        for(int i=0; i<10; i++){
            Item item = new Item();
            item.setPortal("some portal");
            item.setPrice(i);
            item.setTitle("item"+i);
            item.setUrl("http://some.link");
            activeSearchResult.add(item);
        }
        activeSearchKeyword = "testing";
        return activeSearchResult;
    }

    /**
     * For testing advance2, make the active search empty
     */
    public void testClearActiveResult(){
        activeSearchResult = new ArrayList<>();
        activeSearchKeyword = "";
    }

    /**
     * For testing to take the active search result
     * @return the current active search result
     */
    public List<Item> testPeekSearchResult(){
        return activeSearchResult;
    }
}

