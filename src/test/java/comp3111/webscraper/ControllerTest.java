package comp3111.webscraper;


import comp3111.webscraper.models.SearchRecord;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;
import org.testfx.util.WaitForAsyncUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;


public class ControllerTest extends ApplicationTest {
    private static final String UI_FILE = "/ui.fxml";

    VBox root;
    Controller controller;

    @Before
    public void setupBeforeEach() {
        Class<?> clazz = SearchRecord.class;
        try {
            Field f = clazz.getDeclaredField("lastSearch");
            f.setAccessible(true);

            ((ObservableList) f.get(null)).clear();
            assertTrue(((ObservableList) f.get(null)).isEmpty());
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
            fail("Unable to clear search record entries!");
        }
    }

    @AfterClass
    public static void cleanup() {
        try {
            Files.walk(Paths.get("."), 0)
                    .filter(file -> file.endsWith(".3111"))
                    .forEach(f -> f.toFile().delete());
        } catch (IOException e) {
            fail("Unable to delete files");
        }
    }

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
    public void testSearch() {
        System.out.println("testSearch");
        clickOn("#textFieldKeyword");
        write("iphone");
        clickOn("#buttonGo");
        List<Item> result = controller.testPeekSearchResult();
        assertNotNull(result);
        clickOn("#textFieldKeyword");
        for (int i = 0; i < 10; i++) push(KeyCode.BACK_SPACE);
        clickOn("#buttonGo");
        assertEquals(new JSONObject(controller.testPeekSearchResult()).toString(), new JSONObject(result).toString());

    }

    @Test
    public void testSaveLoad() throws IOException {
        System.out.println("test: save load");
        File file = new File("t.3111");
        if (file.delete()) {
            System.out.println("deleted t.3111");
        } else System.out.println("delete t.3111 fail");
        controller.testGenerateDummieResult();
        String original = new JSONArray(controller.testPeekSearchResult()).toString();
        clickOn("#menuFile");
        clickOn("#labelSave");
//        WaitForAsyncUtils.waitForFxEvents();
        push(KeyCode.SHIFT, KeyCode.DIGIT8);
        push(KeyCode.DECIMAL);
        push(KeyCode.SHIFT, KeyCode.DIGIT8);
        push(KeyCode.ENTER);
        push(KeyCode.BACK_SPACE);
        push(KeyCode.BACK_SPACE);
        push(KeyCode.BACK_SPACE);
        push(KeyCode.QUOTEDBL);
        push(KeyCode.T);
        push(KeyCode.QUOTEDBL);
        push(KeyCode.ENTER);

        File realfile = new File("r.3111");
        controller.saveFile(realfile);

        WaitForAsyncUtils.sleep(1, TimeUnit.SECONDS);
        controller.testClearActiveResult();
        clickOn("#menuFile");
        clickOn("#labelOpen");
        push(KeyCode.T);
        push(KeyCode.ENTER);
        WaitForAsyncUtils.sleep(4, TimeUnit.SECONDS);
        assertNotNull(controller.testPeekSearchResult());

        controller.openFile(realfile);
        String load = new JSONArray(controller.testPeekSearchResult()).toString();
        System.out.println(original);
        System.out.println(load);
        assertEquals(original, load);
    }

    @Test
    public void testOpenRubbishFile() throws IOException {
        File file = new File("e.3111");
        FileOutputStream fout = new FileOutputStream(file, false);
        String rubbish = "swegjisoefeio";
        fout.write(rubbish.getBytes());
        fout.close();

        controller.testGenerateDummieResult();
        clickOn("#menuFile");
        clickOn("#labelOpen");
        push(KeyCode.E);
        push(KeyCode.ENTER);
        WaitForAsyncUtils.sleep(4, TimeUnit.SECONDS);
        assertEquals(controller.testPeekSearchResult().size(), 10);
    }

    @Test
    public void testSerializeItems() {
        Class<?> clazz = controller.getClass();
        try {
            Method m = clazz.getDeclaredMethod("serializeItems", List.class);
            m.setAccessible(true);
            List<Item> l = Arrays.asList(new Item(), new Item());
            l.get(0).setTitle("Item");
            l.get(0).setPrice(100.0);
            l.get(0).setPortal("CraigsList");
            l.get(0).setUrl("http");
            l.get(1).setTitle("Item2");
            l.get(1).setPrice(1.0);
            l.get(1).setPortal("A");
            l.get(1).setUrl("https");

            String s = (String) m.invoke(null, l);
            assertEquals("Item\t100.0\tCraigsList\thttp\nItem2\t1.0\tA\thttps", s);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail(e.toString());
        }
    }

    @Test
    public void testRecordComboBoxUpdate() {
        controller.textFieldKeyword.setText("iphone");
        clickOn("#buttonGo");

        clickOn("#trendTab");
        assertEquals(1L, controller.searchRecordComboBox.getItems().stream().filter(s -> s.equals("iphone")).count());
    }

    @Test
    public void testSelectComboBoxItem() {
        // populate the search records
        for (String keyword : Arrays.asList("iphone", "ipad", "ipod", "car", "bike", "watch")) {
            controller.textFieldKeyword.setText(keyword);
            clickOn("#buttonGo");
        }

        assertEquals(5, controller.searchRecordComboBox.getItems().size());

        clickOn("#trendTab");
        for (int i = 1; i <= 5; ++i) {
            clickOn("#searchRecordComboBox");
            type(KeyCode.DOWN);
            type(KeyCode.ENTER);
            assertTrue(controller.areaChart.getData().size() != 0);
            assertTrue(controller.areaChart.getData().get(0).getData().size() != 0);
        }
    }

    @Test
    public void testDoubleClickChartNode() {
        controller.textFieldKeyword.setText("iphone");
        clickOn("#buttonGo");

        String tmp = controller.textAreaConsole.getText();

        clickOn("#trendTab");
        clickOn("#searchRecordComboBox");
        type(KeyCode.DOWN);
        type(KeyCode.ENTER);

        Node n = controller.areaChart.getData().get(0).getData().get(0).getNode();
        doubleClickOn(n);

        assertFalse(controller.areaChart.getData().get(0).getData().get(0).getNode().getStyle().isEmpty());
        assertNotEquals(controller.textAreaConsole.getText(), tmp);
    }

    @Test
    public void testLastSearch() {
        String tmp = "";

        try {
            controller.actionLastSearch();
            fail();
        } catch (IllegalStateException e) {
            // test succeeded
        }

        for (String keyword : Arrays.asList("iphone", "ipad", "ipod")) {
            controller.textFieldKeyword.setText(keyword);
            clickOn("#buttonGo");

            if (keyword.equals("ipad")) {
                tmp = controller.textAreaConsole.getText();
            }
        }

        assertFalse(controller.itemLastSearch.isDisable());
        controller.actionLastSearch();
        assertTrue(controller.itemLastSearch.isDisable());

        assertEquals("ipad", controller.textFieldKeyword.getText());
        assertEquals(tmp, controller.textAreaConsole.getText());
    }
}
