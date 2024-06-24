package org.enemydave.videoparser;


import javafx.application.Platform;

import java.io.*;
import java.util.ArrayList;

public class ProcessCommands {

    static final String DEFAULT_ENCODING_SCRIPT = "ffmpeg -y -i INPUT_VIDEO -pix_fmt yuv420p -ss START_TIME -to END_TIME -map 0:v -c:v VIDEO_ENCODE -map 0:a -c:a aac -ac 2 -threads 4 OUTPUT_VIDEO";
    static final String DEFAULT_NOT_ENCODING_SCRIPT = "ffmpeg -y -i INPUT_VIDEO -ss START_TIME -to END_TIME -map 0:v -c:v copy -map 0:a -c:a copy OUTPUT_VIDEO";

    static Runtime rt = Runtime.getRuntime();

    public static void process(Configuration configuration) {

        // ffmpeg -i input_video.mp4 -ss START_TIME -to END_TIME -c:v libx264 -c:a aac -strict experimental -b:a 192k output_clip.mp4
        // ffmpeg -i input_video.mp4 -ss START_TIME -to END_TIME -c:v libx264 -c:a aac -b:a 192k output_clip.mp4
        // ffmpeg -y -i input_video.mp4 -ss START_TIME -to END_TIME -c:v libx264 -c:a aac -strict experimental -b:a 192k -map 0 -c:s copy output_clip.mp4

        // ffmpeg -y -i INPUT_VIDEO -ss START_TIME -to END_TIME -c:v VIDEO_ENCODE -preset fast -b:v 5M -c:a aac -b:a -map 0 192k OUTPUT_VIDEO
        // ffmpeg -y -i INPUT_VIDEO -ss START_TIME -to END_TIME -map 0:a -c:v copy -c:a copy OUTPUT_VIDEO

        ArrayList<TimeParam> times = configuration.getTimes().stream().collect(ArrayList::new, (list, item) -> {
            if (!item.isSkip()) list.add(item);
        }, ArrayList::addAll);

        int count = times.size();
        double progress = 0.0;

        for (TimeParam p : times) {
            double finalProgress = progress / count;
            Platform.runLater(() -> {
                JFXMain.getMainController().setProgress(finalProgress);
            });
            progress++;

            String outputFile = String.format("%s\\%s\\%s-%02d.mp4", configuration.getOutputPath(), configuration.getOutputName(), configuration.getOutputName(), p.getOrder());
            outputFile = checkIfFileExists(outputFile);

            // //ffmpeg -y -i input_video.mp4 -ss START_TIME -to END_TIME -c:v libx264 -c:a aac -strict experimental -b:a 192k -map 0 -c:s copy output_clip.mp4
            // String.format("ffmpeg -y -i \"%s\" -ss %s -to %s -c:v libx264 -c:a aac -strict experimental -b:a 192k -map 0  \"%s\\%5$s\\%s-%02d.mp4\"",
            //                            configuration.getMoviePath(), p.getStartTime(), p.getEndTime(), configuration.getOutputPath(), configuration.getOutputName(), p.getOrder());

            String cmd;
            if (configuration.changeEncoding) {
                String gpu = configuration.useNvenc ? "h264_nvenc" : "libx264";
                cmd = configuration.getEncodingScript().replace("VIDEO_ENCODE", gpu);

                // cmd = String.format("ffmpeg -i \"%s\" -ss %s -to %s -map 0:v -map 0:a -c:v %s -c:a aac -b:a 192k -threads 4 \"%s\" -y",
                //                     configuration.getMoviePath(), p.getStartTime(), p.getEndTime(), gpu, outputFile);
            } else {
                cmd = configuration.getNotEncodingScript();

                // cmd = String.format("ffmpeg -i \"%s\" -ss %s -to %s -map 0:v -map 0:a -c:v copy -c:a copy \"%s\" -y",
                //                     configuration.getMoviePath(), p.getStartTime(), p.getEndTime(), outputFile);
            }

            cmd = cmd.replace("INPUT_VIDEO", "\"" + configuration.getMoviePath() + "\"")
                    .replace("START_TIME", p.getStartTime())
                    .replace("END_TIME", p.getEndTime())
                    .replace("OUTPUT_VIDEO", "\"" + outputFile + "\"");

            System.out.println(cmd);

            try {
                Process pr = rt.exec(cmd);

                redirectOutputToTextArea(pr.getInputStream(), "output");
                redirectOutputToTextArea(pr.getErrorStream(), "error");

                pr.waitFor();

                Thread.sleep(200);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        Platform.runLater(() -> {
            JFXMain.getMainController().setProgress(1);
        });
    }

    private static String checkIfFileExists(String outputFile) {
        int i = 1;
        String newOutputFile = outputFile;
        while (true) {
            if (!new File(newOutputFile).exists()) {
                return newOutputFile;
            }
            newOutputFile = outputFile.substring(0, outputFile.length() - 4) + "-" + i + ".mp4";
            i++;
        }
    }

    private static void redirectOutputToTextArea(InputStream inputStream, String type) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                // System.out.println("redirecting output");
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[" + type + "] " + line);
                    JFXMain.getMainController().appendLineToConsole(line);
                    // appendToOutputTextArea(line + "\n");
                }
                // System.out.println("redirecting output done");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void killProcess() {
        try {
            rt.exit(0);
            // Runtime.getRuntime().exec("taskkill /F /IM ffmpeg.exe");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
