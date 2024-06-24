package org.enemydave.videoparser;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TimeParam {

    static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    static final DateTimeFormatter parse = DateTimeFormatter.ofPattern("[H:]mm:ss");

    boolean skip;
    LocalTime startTime;
    LocalTime endTime;
    int order;

    int timeShift = 30;
    int timeShift2 = 5;
    TimeShiftType timeShiftType = TimeShiftType.BEFORE_END;

    public static void main(String[] args) {
        // System.out.println(new TimeParam("0:00:00;0:00:10", false).getParsedTimes());
        // System.out.println(new TimeParam("0:23:42 +-10", true).getParsedTimes());
        // System.out.println(new TimeParam("0:23:52 +10", true).getParsedTimes());
        // System.out.println(new TimeParam("0:23:52 -15", true).getParsedTimes());
        // System.out.println(new TimeParam("1:23:52", true).getParsedTimes());
        // System.out.println(new TimeParam("1 23 22", true).getParsedTimes());
        // System.out.println(new TimeParam("2, 33,22", true).getParsedTimes());
        // System.out.println(new TimeParam("  3 22  +  15", true).getParsedTimes());
        System.out.println(new TimeParam("01   3 2  -  25  +55", true).getParsedTimes());
        System.out.println(new TimeParam("01   3 2  +  25  -45 +52", true).getParsedTimes());
    }

    public TimeParam(String time, boolean skip) {
        this(time, skip, 0);
    }

    public TimeParam(String time, boolean skip, int order) {
        // Start and end time format: hh:mm:ss;hh:mm:ss
        // or only end time: hh:mm:ss + - +- timeBeforeEnd //no +- is like -
        // or only start time: hh:mm:ss automatically 30s before end
        this.skip = skip;
        this.order = order;

        if (time.contains(">>")) {
            time = time.substring(time.indexOf(">>") + 2);
        }

        if (time.startsWith("*")) {
            this.skip = true;
            time = time.substring(1);
        }

        if (time.contains(";")) {
            String[] times = time.split(";");
            startTime = parseTime(times[0]);
            endTime = parseTime(times[1]);
            timeShiftType = TimeShiftType.NONE;

        } else if (time.contains("+") || time.contains("-")) {
            String[] times = time.split("\\+\\-|\\-\\+|[\\+\\-]");
            endTime = parseTime(times[0]);
            if (time.contains("+-") || time.contains("-+")) {
                timeShift = parseInteger(times[1]);
                timeShiftType = TimeShiftType.BEFORE_AND_AFTER_END;
            } else {
                if (times.length > 2) {
                    time = time.substring(times[0].length());
                    time = time.replaceAll(" ", "");
                    handleTwoTimeShifts(time);
                    timeShiftType = TimeShiftType.BEFORE_AND_AFTER_END_TIMES;
                } else {
                    timeShift = parseInteger(times[1]);
                    timeShiftType = time.contains("-") ? TimeShiftType.BEFORE_END : TimeShiftType.AFTER_END;
                }
            }
        } else {
            endTime = parseTime(time);
            timeShiftType = TimeShiftType.BEFORE_AND_AFTER_END_TIMES;
        }
    }

    static final Pattern pattern = Pattern.compile("\\b(\\d)\\b");

    private LocalTime parseTime(String time) {
        time = time.trim();
        // replace spaces or commas, dots or any non digit with :
        time = time.replaceAll("[^\\d:]", ":");
        // replace multiple : with single :
        time = time.replaceAll(":+", ":");

        // Regex pattern to match numbers that are not two digits
        Matcher matcher = pattern.matcher(time);
        // Replace each match with the matched number and add a leading zero
        time = matcher.replaceAll("0$1");

        if (time.split(":").length == 2) {
            time = "00:" + time;
        }

        return LocalTime.parse(time, parse);
    }

    static final Pattern timeShiftPattern = Pattern.compile("-?\\d+");

    private void handleTwoTimeShifts(String timeString) {
        Matcher matcher = timeShiftPattern.matcher(timeString);

        // Extract and print individual numbers
        while (matcher.find()) {
            String matchedNumber = matcher.group();
            int number = Integer.parseInt(matchedNumber);
            if (number < 0) {
                timeShift = Math.abs(number);
            } else {
                timeShift2 = number;
            }
        }
    }

    private int parseInteger(String num) {
        num = num.trim();
        // replace spaces or commas, dots or any non digit with :
        num = num.replaceAll("[^\\d:]", "");
        return Integer.parseInt(num);
    }

    @Override
    public String toString() {
        return "TimeParam{" + "skip=" + skip + ", startTime=" + startTime + ", endTime=" + endTime + ", timeShift=" + timeShift + ", timeShiftType=" + timeShiftType + '}';
    }

    public String getParsedTimes() {
        return String.format("[%s] %s;%s", timeShiftType, getStartTime(), getEndTime());
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
            case BEFORE_AND_AFTER_END_TIMES:
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
            case BEFORE_AND_AFTER_END_TIMES:
                pom = endTime.plusSeconds(timeShift2);
                break;
        }

        return pom.format(formatter);
    }

    public String getSaveString(boolean forceSkip) {
        String skipString = (forceSkip || skip) ? "*" : "";
        switch (timeShiftType) {
            case NONE:
                return String.format("%s%s;%s", skipString, formatTime(startTime), formatTime(endTime));
            case BEFORE_END:
                return String.format("%s%s -%s", skipString, formatTime(endTime), timeShift);
            case BEFORE_AND_AFTER_END:
                return String.format("%s%s +-%s", skipString, formatTime(endTime), timeShift);
            case AFTER_END:
                return String.format("%s%s +%s", skipString, formatTime(endTime), timeShift);
            case BEFORE_AND_AFTER_END_TIMES:
                return String.format("%s%s -%s +%s", skipString, formatTime(endTime), timeShift, timeShift2);
        }
        return "";
    }

    private static String formatTime(LocalTime time) {
        return time.format(formatter);
    }

    public boolean isSkip() {
        return skip;
    }

    public int getOrder() {
        return order;
    }

    public void setSkip(boolean b) {
        skip = b;
    }

    public void setPlaySpeed(double playSpeed) {
        if(startTime != null) {
            startTime = adjustTimeByPlaySpeed(startTime, playSpeed);
        }

        if(endTime != null) {
            endTime = adjustTimeByPlaySpeed(endTime, playSpeed);
        }
    }

    private LocalTime adjustTimeByPlaySpeed(LocalTime time, double playSpeed) {
        Duration elapsedRealTime = Duration.between(LocalTime.of(0, 0), time);

        // Scale the elapsed time by the playback speed
        long scaledSeconds = (long) (elapsedRealTime.getSeconds() * playSpeed);

        long timeDiff = scaledSeconds - elapsedRealTime.getSeconds();

        // Calculate the current play time based on the scaled duration
        return time.plus(timeDiff, ChronoUnit.SECONDS);
    }
}
