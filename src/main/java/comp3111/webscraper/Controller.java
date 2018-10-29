/**
 *
 */
package comp3111.webscraper;


import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import java.io.File;
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
    private void actionSave(){
        JSONArray activeSearchResultArray = new JSONArray(activeSearchResult);
        JSONObject jsonObject = new JSONObject();
        jsonObject.accumulate("keyword",activeSearchKeyword);
        jsonObject.accumulate("result",activeSearchResultArray);
        String outputJson = jsonObject.toString();
        System.out.println(outputJson);

//        FileChooser fileChooser = new FileChooser();
//        fileChooser.setTitle("Save Image");
//        File file = fileChooser.showSaveDialog();
//        if (file != null) {
//            try {
//                ImageIO.write(SwingFXUtils.fromFXImage(pic.getImage(),
//                        null), "png", file);
//            } catch (IOException ex) {
//                System.out.println(ex.getMessage());
//            }
//        }

    }
}

