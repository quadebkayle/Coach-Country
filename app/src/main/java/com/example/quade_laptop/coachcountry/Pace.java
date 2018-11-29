package com.example.quade_laptop.coachcountry;

import java.util.List;

public class Pace {

    private int seconds;
    private int minute;

    public int getMinute() {
        return minute;
    }

    public int getSeconds() {
        return seconds;
    }

    public String getPaceString() {
        String retval = Integer.toString(minute) + ":";
        if (seconds < 10)
            retval = retval + "0";
        retval = retval + Integer.toString(seconds);

        return retval;
    }


    public void setMinute(int minute) {
        this.minute = minute;
    }

    public void setSeconds(int seconds) {
        this.seconds = seconds;
    }

    Pace(int minute, int seconds) {
        this.minute = minute;
        this.seconds = seconds;
    }

    public static Double calculatePace(Double distance, long currentTime, long previousTime) {
        Double timeInHour = ((currentTime - previousTime) / 1000.0) / 3600.0;
        Double inMiles = distance / 1609.344;
        Double mph = inMiles / timeInHour;
        Double pace = 60 / mph;
        return pace;
    }

    public static Pace calculateAveragePace(List<Double> paces) {
        Double runningPace = 0.0;
        for (Double pace : paces) {
            runningPace += pace;
        }
        Double d = new Double(paces.size());
        runningPace = runningPace / d;
        Pace retval = new Pace((int) Math.floor(runningPace), (int) Math.round(((runningPace - (int) Math.floor(runningPace)) * 60)));
        return retval;
    }
}
