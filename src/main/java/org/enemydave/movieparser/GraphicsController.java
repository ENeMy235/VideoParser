package org.enemydave.movieparser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class GraphicsController implements Initializable {

    @FXML
    private Button helpButton;

    @FXML
    private TextField movie;

    @FXML
    private TextField output;

    @FXML
    private TextArea times;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Tooltip.install(helpButton, tooltip);
    }

    Tooltip tooltip = new Tooltip("Zadejte časy, kdy se má video zastavit.\nČasy oddělte čárkou. Např. 1:23, 2:34, 3:45");

    @FXML
    void help(ActionEvent event) {
        Point2D p = helpButton.localToScene(0.0, 0.0);

        tooltip.setAutoHide(true);

        tooltip.show(times, p.getX() + helpButton.getScene().getX() + helpButton.getScene().getWindow().getX() + 30,
                p.getY() + helpButton.getScene().getY() + helpButton.getScene().getWindow().getY() + 15);
    }

    @FXML
    void importFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otevřít předchozí export");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Export", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File f = fileChooser.showOpenDialog(null);
        System.out.println(f.getAbsolutePath());
    }

    @FXML
    void openMovie(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otevřít video");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mkv", "*.mov", "*.wmv", "*.flv", "*.webm"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File f = fileChooser.showOpenDialog(null);
        System.out.println(f.getAbsolutePath());
    }

    @FXML
    void run(ActionEvent event) {
        System.out.println("run");

        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


}
