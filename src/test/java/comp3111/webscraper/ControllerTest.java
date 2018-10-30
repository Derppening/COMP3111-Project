package comp3111.webscraper;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONObject;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import javax.swing.text.FlowView;
import java.io.IOException;
import java.util.List;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;


public class ControllerTest extends ApplicationTest {
    private static final String UI_FILE = "/ui.fxml";

    VBox root;
    Controller controller;

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader();
        loader.setLocation(getClass().getResource(UI_FILE));
        root = loader.load();
        Scene scene = new Scene(root);
        stage.setScene(scene);
        stage.setTitle("WebScrapper");
        stage.show();
        controller = loader.getController();
//        Flow flow = new Flow(Controller.class);
//        FlowHandler handler = flow.createHandler();
//        FlowView view = handler.getCurrentView();
//        controller = (Controller) view.getViewContext().getController();
    }

    @Test
    public void testSearch(){
        clickOn("#textFieldKeyword");
        write("iphone");
        clickOn("#buttonGo");
        List<Item> result = controller.testPeekSearchResult();
        assertNotNull(result);
        clickOn("#textFieldKeyword");
        for(int i=0 ;i< 10; i++)push(KeyCode.BACK_SPACE);
        clickOn("#buttonGo");
        assertEquals(new JSONObject(controller.testPeekSearchResult()).toString(),new JSONObject(result).toString());

    }


}
