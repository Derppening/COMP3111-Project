package comp3111.webscraper;


import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.junit.Assert.assertNull;


public class ControllerTest extends ApplicationTest {
    private static final String UI_FILE = "/ui.fxml";

    VBox root;
    Controller controller;

    @Override
    public void start(Stage stage) throws IOException {
        System.out.println("start controller test");
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
        System.out.println("testSearch");
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

    @Test
    public void testSaveLoad(){
        System.out.println("test: save load");
        File file = new File("t.3111");
        if(file.delete()){
            System.out.println("deleted t.3111");
        }else System.out.println("delete t.3111 fail");
        controller.testGenerateDummieResult();
        String original = new JSONArray(controller.testPeekSearchResult()).toString();
        clickOn("#menuFile");
        clickOn("#labelSave");
//        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.SHIFT,KeyCode.DIGIT8);
        push(KeyCode.DECIMAL);
        push(KeyCode.SHIFT,KeyCode.DIGIT8);
        push(KeyCode.ENTER);
        push(KeyCode.BACK_SPACE);
        push(KeyCode.BACK_SPACE);
        push(KeyCode.BACK_SPACE);
        push(KeyCode.QUOTEDBL);
        push(KeyCode.T);
        push(KeyCode.QUOTEDBL);
        push(KeyCode.ENTER);

        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);
        controller.testClearActiveResult();
        clickOn("#menuFile");
        clickOn("#labelOpen");
        push(KeyCode.T);
        push(KeyCode.ENTER);
        WaitForAsyncUtils.sleep(4, TimeUnit.SECONDS);
//        assertNotNull(controller.testPeekSearchResult());
        String load = new JSONArray(controller.testPeekSearchResult()).toString();
        System.out.println(original);
        System.out.println(load);
        assertEquals(original,load);
    }

    @Test
    public void testOpenRubbishFile() throws IOException {
        File file = new File("e.3111");
        FileOutputStream fout = new FileOutputStream(file,false);
        String rubbish ="swegjisoefeio";
        fout.write(rubbish.getBytes());
        fout.close();

        controller.testGenerateDummieResult();
        clickOn("#menuFile");
        clickOn("#labelOpen");
        push(KeyCode.E);
        push(KeyCode.ENTER);
        WaitForAsyncUtils.sleep(4, TimeUnit.SECONDS);
        assertEquals(controller.testPeekSearchResult().size(),10);
    }
}
