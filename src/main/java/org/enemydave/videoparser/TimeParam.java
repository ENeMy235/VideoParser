package org.enemydave.videoparser;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeParam {

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    boolean skip = false;
    LocalTime startTime;
    LocalTime endTime;

    int timeBeforeEnd;

    public TimeParam(String time) {
        //Start and end time format: hh:mm:ss;hh:mm:ss
        // or only end time: hh:mm:ss + - +- timeBeforeEnd


    }

    public String getStartTime() {
        if (startTime == null) {
            startTime = endTime.minusSeconds(timeBeforeEnd);
        }

        return startTime.format(formatter);
    }

    public String getEndTime() {
        return endTime.format(formatter);
    }

    public boolean isSkip() {
        return skip;
    }
}
