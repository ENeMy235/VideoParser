package org.enemydave.videoparser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ProcessCommands {

    public static void process(File input, File output, ArrayList<String> params) {
        Runtime rt = Runtime.getRuntime();
//        for (File f : files) {
//            if (f.isDirectory()) continue;
//            if (!FilenameUtils.getExtension(f.getName()).contains("mp4")) continue;
//
//            String cmd = String.format("ffmpeg -sseof -3 -i \"%s\" -filter:v \"crop=in_w*0.7:in_h*0.5:in_w*0.15:in_h*0.5\" -ss 1 -frames:v 1 -q:v 2 \"%s_%%d.jpg\"", f.getAbsolutePath(), f.getAbsolutePath());
//
//            System.out.println(cmd);
//
//            try {
//                Process pr = rt.exec(cmd);
//
////                pr.waitFor();
//
//            } catch (IOException e) {
//                e.printStackTrace();
////            } catch (InterruptedException e) {
////                throw new RuntimeException(e);
//            }
        }


}
