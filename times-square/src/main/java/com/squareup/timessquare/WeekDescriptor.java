package com.squareup.timessquare;

import java.util.Date;

public class WeekDescriptor {

    private final int week;
    private final int month;
    private final int year;
    private final Date date;

    public WeekDescriptor(int week, int month, int year, Date date) {
        this.week = week;
        this.month = month;
        this.year = year;
        this.date = date;
    }

    public int getWeek() {
        return week;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }

    public Date getDate() {
        return date;
    }


}
