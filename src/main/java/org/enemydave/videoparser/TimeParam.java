package org.enemydave.videoparser;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class TimeParam {

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");

    boolean skip;
    LocalTime startTime;
    LocalTime endTime;

    int timeShift = 30;
    TimeShiftType timeShiftType = TimeShiftType.BEFORE_END;

    public TimeParam(String time, boolean skip) {
        //Start and end time format: hh:mm:ss;hh:mm:ss
        // or only end time: hh:mm:ss + - +- timeBeforeEnd //no +- is like -
        // or only start time: hh:mm:ss automatically 30s before end
        this.skip = skip;


    }

    public String getStartTime() {
        LocalTime pom = null;

        switch (timeShiftType) {
            case NONE:
                if (startTime == null) {
                    pom = endTime.minusSeconds(timeShift);
                } else {
                    pom = startTime;
                }
                break;
            case BEFORE_END:
            case BEFORE_AND_AFTER_END:
                pom = endTime.minusSeconds(timeShift);
                break;
            case AFTER_END:
                pom = endTime;
                break;
        }

        return pom.format(formatter);
    }

    public String getEndTime() {
        LocalTime pom = null;

        switch (timeShiftType) {
            case NONE:
                if (endTime == null) {
                    pom = startTime.plusSeconds(timeShift);
                    break;
                }
            case BEFORE_END:
                pom = endTime;
                break;
            case BEFORE_AND_AFTER_END:
            case AFTER_END:
                pom = endTime.plusSeconds(timeShift);
                break;
        }

        return pom.format(formatter);
    }

    public String getSaveString() {
        String skipString = skip ? "*" : "";
        switch (timeShiftType) {
            case NONE:
                return String.format("%s%s;%s", skipString, formatTime(startTime), formatTime(endTime));
            case BEFORE_END:
                return String.format("%s%s -%s", skipString, formatTime(endTime), timeShift);
            case BEFORE_AND_AFTER_END:
                return String.format("%s%s +-%s", skipString, formatTime(endTime), timeShift);
            case AFTER_END:
                return String.format("%s%s +%s", skipString, formatTime(endTime), timeShift);
        }
        return "";
    }

    private static String formatTime(LocalTime time) {
        return time.format(formatter);
    }

    public boolean isSkip() {
        return skip;
    }
}
