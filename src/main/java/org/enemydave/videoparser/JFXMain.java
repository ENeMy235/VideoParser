package org.enemydave.videoparser;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import jfxtras.styles.jmetro.JMetro;
import jfxtras.styles.jmetro.Style;

import java.io.IOException;

public class JFXMain extends Application {

    static GraphicsController mainWindowController;

    public static GraphicsController getMainController() {
        return mainWindowController;
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(JFXMain.class.getResource("ffmpegExport.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 300, 600);
        mainWindowController = fxmlLoader.getController();

        JMetro jMetro = new JMetro(Style.DARK);
        jMetro.setScene(scene);
        jMetro.getOverridingStylesheets().add(JFXMain.class.getResource("custom.css").toExternalForm());

        // Add icon
        stage.getIcons().add(new javafx.scene.image.Image(JFXMain.class.getResourceAsStream("roll-film.png")));

        stage.setOnCloseRequest(new EventHandler<WindowEvent>() {
            @Override
            public void handle(WindowEvent e) {
                ProcessCommands.killProcess();
                Platform.exit();
                System.exit(0);
            }
        });

        stage.setTitle("Movie Parser");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch();
    }
}