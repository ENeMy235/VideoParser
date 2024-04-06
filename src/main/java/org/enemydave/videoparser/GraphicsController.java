package org.enemydave.videoparser;

import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static org.enemydave.videoparser.ProcessCommands.DEFAULT_ENCODING_SCRIPT;
import static org.enemydave.videoparser.ProcessCommands.DEFAULT_NOT_ENCODING_SCRIPT;

public class GraphicsController implements Initializable {

    @FXML
    private Button helpButton, runButton, consoleOpenButton, consoleCloseButton;

    @FXML
    private TextField movie, output, outputFolder;

    @FXML
    private TextArea times, console;

    @FXML
    private VBox progressPane;
    @FXML
    private AnchorPane grayPane;
    @FXML
    private ProgressBar progress;


    @FXML
    CheckBox gpuEncode, encode;

    File appDir;

    Configuration configuration;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        Tooltip.install(helpButton, tooltip);

        URL u = GraphicsController.class.getProtectionDomain().getCodeSource().getLocation();
        String path = "";
        try {
            path = URLDecoder.decode(u.getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace(); // Handle the exception as needed
        }
        // Remove %20 from path (space)
        appDir = new File(path).getParentFile();

        System.out.println(appDir.getAbsolutePath());



        runButton.disableProperty().bind(Bindings.or(
                Bindings.isEmpty(movie.textProperty()),
                Bindings.or(Bindings.isEmpty(times.textProperty()),
                            Bindings.isEmpty(output.textProperty()))));

        configuration = new Configuration();
        configuration.setOutputPath(appDir.getAbsolutePath());

        File defaultScript = new File(appDir.getAbsolutePath() + "\\defaultFfmpegScript.txt");
        if(defaultScript.exists()) {
            var defaultConfig = Configuration.getConfigurationFromFile(defaultScript.getAbsolutePath());
            configuration.setEncodingScript(defaultConfig.getEncodingScript());
            configuration.setNotEncodingScript(defaultConfig.getNotEncodingScript());
        } else {
            configuration.setEncodingScript(DEFAULT_ENCODING_SCRIPT);
            configuration.setNotEncodingScript(DEFAULT_NOT_ENCODING_SCRIPT);
            configuration.saveConfigurationToFile(defaultScript);
        }

        loadFields(false);

        progressPane.setVisible(false);

        gpuEncode.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            configuration.setUseNvenc(t1);
        });

        encode.selectedProperty().addListener((observableValue, aBoolean, t1) -> {
            configuration.setChangeEncoding(t1);
        });

        grayPane.visibleProperty().bind(progressPane.visibleProperty());
    }

    Tooltip tooltip = new Tooltip("Zadejte časy, kdy se má video zastavit.\n" +
                                          "\tStart a konec: h:mm:ss;h:mm:ss\n" +
                                          "\tČas před koncem: h:mm:ss -sekundy\n" +
                                          "\tČas po konci: h:mm:ss +sekundy\n" +
                                          "\tČas před a po konci: h:mm:ss +-sekundy\n" +
                                          "\tČasy před a po konci: h:mm:ss -sekundy +sekundy\n" +
                                          "Čas může obsahovat čárky nebo mezery místo dvojtečky.\n" +
                                          "Pokud se zadají sekundy, je potřeba uvést znaménko.");

    @FXML
    void help(ActionEvent event) {
        Point2D p = helpButton.localToScene(0.0, 0.0);

        tooltip.setAutoHide(true);

        tooltip.show(times, p.getX() + helpButton.getScene().getX() + helpButton.getScene().getWindow().getX() + 30,
                     p.getY() + helpButton.getScene().getY() + helpButton.getScene().getWindow().getY() + 15);
    }

    boolean timesChanged = false;
    public void invertState() {
        prepareTimes(timesChanged, true);
        timesChanged = !timesChanged;
    }

    @FXML
    void importFile(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otevřít předchozí export");

        File f = appDir;

        if (f != null && f.exists() && f.isDirectory()) {
            fileChooser.setInitialDirectory(f);
        }

        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Export", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File ff = fileChooser.showOpenDialog(null);

        if (ff == null) {
            return;
        }

        configuration = Configuration.getConfigurationFromFile(ff.getAbsolutePath());
        loadFields(true);
    }

    private void loadFields(boolean changeParams) {
        output.setText(configuration.outputName);
        outputFolder.setText(configuration.outputPath);

        if (changeParams) {
            fillTextArea(configuration.getTimes());
        }

        File movieFile = new File(configuration.moviePath);
        movie.setText(movieFile.getName());
        movie.setTooltip(new Tooltip(movieFile.getAbsolutePath()));

        gpuEncode.setSelected(configuration.useNvenc);
        encode.setSelected(configuration.changeEncoding);
    }

    @FXML
    void openMovie(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Otevřít video");

        File f = appDir;
        if (configuration != null && configuration.moviePath != null) {
            f = new File(configuration.moviePath);
            f = f.getParentFile();
        }

        if (f != null && f.exists() && f.isDirectory()) {
            fileChooser.setInitialDirectory(f);
        }
        // fileChooser.setInitialDirectory(f);
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Video Files", "*.mp4", "*.avi", "*.mkv", "*.mov", "*.wmv", "*.flv", "*.webm"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        File ff = fileChooser.showOpenDialog(null);

        if (ff == null) {
            return;
        }

        movie.setText(ff.getName());
        movie.setTooltip(new Tooltip(ff.getAbsolutePath()));
        configuration.setMoviePath(ff.getAbsolutePath());
    }

    private void prepareTimes(boolean skip, boolean changeSkip) {
        ArrayList<TimeParam> timeParams = configuration.getTimes();
        timeParams.clear();

        int count = 0;

        for (String time : times.getText().split("\n")) {
            TimeParam timeParam = new TimeParam(time, false, count++);
            if(changeSkip) {
                timeParam.setSkip(skip);
            }
            timeParams.add(timeParam);
        }

        fillTextArea(timeParams);
    }

    private void fillTextArea(List<TimeParam> list) {
        times.clear();
        int count = 0;
        for (TimeParam p : list) {
            times.appendText(String.format("%02d>>%s\n", count++, p.getSaveString(false)));
            // times.appendText(p.getSaveString(false) + "\n");
        }
    }

    @FXML
    void run(ActionEvent event) {
        console.clear();
        configuration.setOutputName(output.getText());

        prepareTimes(false, false);

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Potvrzení časů");
        confirm.setHeaderText("Spustit export klipů?");
        confirm.setContentText("Zkontroluj si definované časy!\nProvedla se jejich automatická korekce.");
        confirm.initOwner(runButton.getScene().getWindow());

        if (!confirm.showAndWait().get().equals(ButtonType.OK)) {
            return;
        }

        File f = new File(configuration.getOutputPath() + "\\" + configuration.getOutputName() + "\\export-" + configuration.getOutputName() + ".txt");
        if (!f.getParentFile().exists()) {
            f.getParentFile().mkdirs();
        }

        if(configuration.getEncodingScript() == null || configuration.getEncodingScript().isEmpty()) {
            configuration.setEncodingScript(DEFAULT_ENCODING_SCRIPT);
        }

        if(configuration.getNotEncodingScript() == null || configuration.getNotEncodingScript().isEmpty()) {
            configuration.setNotEncodingScript(DEFAULT_NOT_ENCODING_SCRIPT);
        }

        configuration.saveConfigurationToFile(f);

        consoleCloseButton.setVisible(false);
        progressPane.setVisible(true);
        progress.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);


        Thread t = new Thread(() -> {
            ProcessCommands.process(configuration);
        });

        t.start();


    }

    public void appendLineToConsole(String line) {
        Platform.runLater(() -> {
            console.appendText(line + "\n");
        });
    }

    public void setProgress(double progressNumber) {
        progress.setProgress(progressNumber);

        if (progressNumber == 1.0) {
            progressPane.setVisible(false);
        }
    }

    public void changeOutputFolder(ActionEvent actionEvent) {
        File f = appDir;
        if (configuration != null && configuration.outputPath != null) {
            f = new File(configuration.outputPath);
        }


        DirectoryChooser dir = new DirectoryChooser();
        dir.setTitle("Vybrat výstupní složku");

        if (f != null && f.exists() && f.isDirectory()) {
            dir.setInitialDirectory(f);
        }
        File ff = dir.showDialog(null);

        if (ff == null) {
            return;
        }

        configuration.setOutputPath(ff.getAbsolutePath());
        outputFolder.setText(ff.getAbsolutePath());
    }

    public void closeConsole(ActionEvent actionEvent) {
        progressPane.setVisible(false);
        consoleOpenButton.setVisible(true);
    }

    public void openConsole(ActionEvent actionEvent) {
        progressPane.setVisible(true);
        consoleOpenButton.setVisible(false);
        consoleCloseButton.setVisible(true);
    }
}
