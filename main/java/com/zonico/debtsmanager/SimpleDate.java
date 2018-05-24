package com.zonico.debtsmanager;

import android.support.annotation.NonNull;
import android.util.Log;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by Marc on 11.08.2017.
 */

public class SimpleDate implements Comparable<SimpleDate>, Serializable {

    private static final String TAG = "SimpleDate";

    private int year;
    private int month;
    private int day;

    public static SimpleDate fromString(String input) {
        String[] parts = input.split("/");
        return new SimpleDate(Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
    }

    public SimpleDate() {
        Calendar today = Calendar.getInstance();
        this.day = today.get(Calendar.DAY_OF_MONTH);
        this.month = today.get(Calendar.MONTH) + 1;
        this.year = today.get(Calendar.YEAR);
    }
    public SimpleDate(int day, int month, int year) {
        setDay(day);
        setMonth(month);
        setYear(year);
    }

    @Override
    public int compareTo(@NonNull SimpleDate other) {
        if (this.year < other.year) {
            return 1;
        } else if (this.year > other.year) {
            return -1;
        } else {
            if (this.month < other.month) {
                return 1;
            } else if (this.month > other.month) {
                return -1;
            } else {
                if (this.day < other.day) {
                    return 1;
                } else if (this.day > other.day) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    public String toString() {
        return day + "/" + month + "/" + year;
    }

    public void setDay(int day) {
        if (day > 31) {
            Log.d(TAG, "setDay: day value was over 31");
            this.day = -1;
            return;
        }
        this.day = day;
    }
    public void setMonth(int month) {
        this.month = month % 12;
    }
    public void setYear(int year) {
        this.year = year;
    }

    public int getYear() {
        return year;
    }
    public int getMonth() {
        return month;
    }
    public int getDay() {
        return day;
    }
}
