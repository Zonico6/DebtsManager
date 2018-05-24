package com.zonico.debtsmanager;

import java.io.Serializable;

public class DebtsFilter implements Serializable {
    public static final int PAID = 0b001;
    public static final int UNPAID = 0b010;
    public static final int PAID_AND_UNPAID = 0b011;

    private int filter;

    DebtsFilter() {
        filter = 0;
    }
    DebtsFilter(int i) {
        filter = i;
    }

    public String toString() {
        String retStr = "";
        if (isSet(PAID)) {
            retStr += "paid";
        }
        if (isSet(UNPAID)) {
            retStr += ", unpaid";
        }
        return retStr;
    }

    public int toInt() {
        return filter;
    }

    public int add(DebtsFilter other) {
        return filter |= other.getFilter();
    }
    public int add(int other) {
        return filter |= other;
    }

    public int remove(DebtsFilter other) {
        return filter &= (~other.getFilter());
    }
    public int remove(int other) {
        return filter &= (~other);
    }

    public boolean isSet(DebtsFilter other) {
        return (filter & other.getFilter()) == other.getFilter();
    }
    public boolean isSet(int other) {
        return (filter & other) == other;
    }

    public void setFilter(int other) {
        this.filter = other;
    }
    public int getFilter() { return filter; }
}