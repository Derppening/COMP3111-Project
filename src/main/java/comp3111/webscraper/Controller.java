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
import java.io.File;
import java.io.FileOutputStream;
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
        activeSearchResult = result;
        activeSearchKeyword = textFieldKeyword.getText();
        StringBuilder output = new StringBuilder();
        if(result == null)return;
        for (Item item : result) {
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

        labelCount.setText("Hi");
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
    private void actionSave(ActionEvent event){
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
}

