package org.enemydave.videoparser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class Configuration {


    String moviePath;
    String outputPath;
    String outputName;
    String encodingScript;
    String notEncodingScript;
    boolean useNvenc;
    boolean changeEncoding;


    ArrayList<TimeParam> times;

    public Configuration() {
        this("", "", "", new ArrayList<>(), true, true, ProcessCommands.DEFAULT_ENCODING_SCRIPT, ProcessCommands.DEFAULT_NOT_ENCODING_SCRIPT);
    }

    public Configuration(String moviePath, String outputPath, String outputName, ArrayList<TimeParam> times, boolean useNvenc, boolean changeEncoding, String encodingScript, String notEncodingScript) {
        this.moviePath = moviePath;
        this.outputPath = outputPath;
        this.outputName = outputName;
        this.times = times;
        this.useNvenc = useNvenc;
        this.changeEncoding = changeEncoding;
        this.encodingScript = encodingScript;
        this.notEncodingScript = notEncodingScript;
    }

    public static Configuration getConfigurationFromFile(String path) {
        File file = new File(path);
        if (!file.exists()) {
            throw new RuntimeException("File does not exist: " + path);
        }

        String moviePath = null;
        String outputPath = null;
        String outputName = null;
        boolean useNvenc = false;
        boolean changeEncoding = false;
        String encodingScript = null;
        String notEncodingScript = null;
        ArrayList<TimeParam> times = new ArrayList<>();

        try {
            String content = new String(java.nio.file.Files.readAllBytes(file.toPath()));
            String[] lines = content.split("\n");
            int count = 0;

            for (String line : lines) {
                if (line.startsWith("moviePath=")) {
                    moviePath = line.substring("moviePath=".length());
                } else if (line.startsWith("outputPath=")) {
                    outputPath = line.substring("outputPath=".length());
                } else if (line.startsWith("outputName=")) {
                    outputName = line.substring("outputName=".length());
                } else if (line.startsWith("times=")) {
                    String time = line.substring("times=".length());
                    times.add(new TimeParam(time, true, count++));
                } else if (line.startsWith("useNvenc=")) {
                    useNvenc = Boolean.parseBoolean(line.substring("useNvenc=".length()).trim());
                } else if (line.startsWith("changeEncoding=")) {
                    changeEncoding = Boolean.parseBoolean(line.substring("changeEncoding=".length()).trim());
                } else if (line.startsWith("encodingScript=")) {
                    encodingScript = line.substring("encodingScript=".length());
                } else if (line.startsWith("notEncodingScript=")) {
                    notEncodingScript = line.substring("notEncodingScript=".length());
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new Configuration(moviePath, outputPath, outputName, times, useNvenc, changeEncoding, encodingScript, notEncodingScript);
    }

    public void saveConfigurationToFile(File file) {
        StringBuilder sb = new StringBuilder();
        sb.append("moviePath=").append(moviePath).append("\n");
        sb.append("outputPath=").append(outputPath).append("\n");
        sb.append("outputName=").append(outputName).append("\n");
        sb.append("useNvenc=").append(useNvenc).append("\n");
        sb.append("changeEncoding=").append(changeEncoding).append("\n");
        sb.append("encodingScript=").append(encodingScript).append("\n");
        sb.append("notEncodingScript=").append(notEncodingScript).append("\n");

        for (TimeParam time : times) {
            sb.append("times=").append(time.getSaveString(true)).append("\n");
        }

        try {
            FileWriter fw = new FileWriter(file);
            fw.write(sb.toString());
            fw.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getMoviePath() {
        return moviePath;
    }

    public String getOutputPath() {
        return outputPath;
    }

    public String getOutputName() {
        return outputName;
    }

    public ArrayList<TimeParam> getTimes() {
        return times;
    }

    public void setMoviePath(String moviePath) {
        this.moviePath = moviePath;
    }

    public void setOutputPath(String outputPath) {
        this.outputPath = outputPath;
    }

    public void setOutputName(String outputName) {
        this.outputName = outputName;
    }

    public void setTimes(ArrayList<TimeParam> times) {
        this.times = times;
    }

    public boolean isUseNvenc() {
        return useNvenc;
    }

    public void setUseNvenc(boolean useNvenc) {
        this.useNvenc = useNvenc;
    }

    public boolean isChangeEncoding() {
        return changeEncoding;
    }

    public void setChangeEncoding(boolean changeEncoding) {
        this.changeEncoding = changeEncoding;
    }

    public String getEncodingScript() {
        return encodingScript;
    }

    public void setEncodingScript(String encodingScript) {
        this.encodingScript = encodingScript;
    }

    public String getNotEncodingScript() {
        return notEncodingScript;
    }

    public void setNotEncodingScript(String notEncodingScript) {
        this.notEncodingScript = notEncodingScript;
    }
}
