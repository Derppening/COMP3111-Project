package comp3111.webscraper.controllers;

import comp3111.webscraper.models.TeamMemberInfo;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class MenuController {
    /**
     * Spawns a new JavaFX stage and display the team information there.
     */
    public static void displayTeamInfo() {
        GridPane gp = new GridPane();
        gp.setAlignment(Pos.CENTER);
        gp.setHgap(15);
        gp.setVgap(15);
        gp.add(new Label("Name"), 0, 0);
        gp.add(new Label("ITSC Account"), 1, 0);
        gp.add(new Label("GitHub Account"), 2, 0);

        for (int i = 0; i < TeamMemberInfo.allInfo.size(); ++i) {
            TeamMemberInfo t = TeamMemberInfo.allInfo.get(i);

            gp.add(new Label(t.name), 0, i + 1);
            gp.add(new Label(t.itsc), 1, i + 1);
            gp.add(new Label(t.github), 2, i + 1);
        }

        Scene scene = new Scene(gp, 500, 300);
        Stage stage = new Stage();
        stage.setScene(scene);
        stage.show();
    }
}