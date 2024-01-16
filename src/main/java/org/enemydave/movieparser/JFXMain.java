package org.enemydave.movieparser;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;

public class JFXMain extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(JFXMain.class.getResource("ffmpegExport.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 600);

        JMetro jMetro = new JMetro(Style.DARK);
        jMetro.setScene(scene);

        stage.setTitle("Movie Parser");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}