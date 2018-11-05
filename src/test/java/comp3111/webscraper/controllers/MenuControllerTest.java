package comp3111.webscraper.controllers;

import comp3111.webscraper.Controller;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.Test;
import org.testfx.framework.junit.ApplicationTest;

import java.io.IOException;

public class MenuControllerTest extends ApplicationTest {
    private static final String UI_FILE = "/ui.fxml";

    private VBox root;
    private Controller controller;

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
    }

    @Test
    public void testDisplayTeamInfo() {
        Platform.runLater(MenuController::displayTeamInfo);
    }
}
