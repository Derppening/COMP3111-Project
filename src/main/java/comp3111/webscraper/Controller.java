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
        activeSearchResult = scraper.scrape(textFieldKeyword.getText());
        activeSearchKeyword = textFieldKeyword.getText();
        clearConsole();
        printActiveSearchResult();
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
        JSONArray activeSearchResultArray = new JSONArray(activeSearchResult);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("keyword",activeSearchKeyword);
        jsonObject.put("result",activeSearchResultArray);
        String outputJson = jsonObject.toString();
//        System.out.println(activeSearchResultArray.toString());
//        System.out.println(outputJson);

        Window stage = root.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Search");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Webscrapper File(*.3111)", "*.3111"));
        File file = fileChooser.showSaveDialog(stage);
        if (file != null) {
            try {
                if(!file.getName().contains(".")) {
                    file = new File(file.getAbsolutePath() + ".3111");
                }
                FileOutputStream fooStream = new FileOutputStream(file, false);
                fooStream.write(outputJson.getBytes());
                fooStream.close();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }

    /**
     * Called when going to save
     */
    @FXML
    private void actionOpen(){
        Window stage = root.getScene().getWindow();

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Search");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Webscrapper File(*.3111)", "*.3111"));
        File file = fileChooser.showOpenDialog(stage);
        if (file != null) {
            try {
                if(!file.getName().contains(".")) {
                    file = new File(file.getAbsolutePath() + ".3111");
                }
                String inputJson = readFile(file);
                JSONObject inputObject = new JSONObject(inputJson);
                activeSearchKeyword = inputObject.optString("keyword");
                JSONArray result = (JSONArray) inputObject.get("result");
                activeSearchResult = new ArrayList<>();
                for(int i=0 ;i<result.length();i++){
                    activeSearchResult.add(new Item(result.getJSONObject(i)));
                }
                clearConsole();
                printConsole("--Data Loading from "+file.getAbsolutePath()+"--\n");
                printActiveSearchResult();

            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }

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
}

