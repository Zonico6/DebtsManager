/**
 * {
 *     "amount":12.50,
 *     "date": "MM/dd/yyyy hh:mm:ss aa",
 *     "curr":"EURO"
 *     "comm":"The fucking asshole did it again."
 * }
 */

package com.zonico.debtsmanager;

import android.support.annotation.Nullable;
import android.text.format.DateFormat;
import android.util.JsonWriter;

import org.json.JSONObject;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Marc on 01.08.2017.
 */

public class Debt implements Serializable {

    enum Currency {
        EURO,
        DOLLAR
    }
    private String name;
    private boolean paid;
    public double amount;
    private SimpleDate date;
    private Currency curr;
    private String comment;

    public Debt() {
        name = "";
        paid = false;
        amount = 0;
        date = new SimpleDate();
        curr = Currency.EURO;
        comment = "This comment was auto-generated and has no useful information bound to it!";
    }

    public Debt(JSONObject debt, String name, boolean paid) throws org.json.JSONException {
        this.name = name;
        this.paid = paid;

        amount = debt.getDouble("amount");
        comment = debt.getString("comm");
        date = SimpleDate.fromString(debt.getString("date"));
        switch (debt.getString("curr")) {
            case "EURO":
                curr = Currency.EURO;
                break;
            case "DOLLAR":
                curr = Currency.DOLLAR;
        }
    }
    public Debt(boolean paid, String name, double amount, SimpleDate date, @Nullable String comment) {
        this.paid = paid;
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.curr = Currency.EURO;
        this.comment = comment == null ? "" : comment;
    }
    public Debt(boolean paid, String name, double amount, SimpleDate date, @Nullable String comment, Currency currency) {
        this.paid = paid;
        this.name = name;
        this.amount = amount;
        this.date = date;
        this.curr = currency;
        this.comment = comment == null ? "" : comment;
    }

    public boolean equalsIgnorePaid(Debt other) {
        return this.name.equals(other.name) && this.amount == other.amount && this.date == other.date &&
                this.curr == other.curr && this.comment.equals(other.comment);
    }

    public void writeJson(JsonWriter writer) throws java.io.IOException {
        writer.beginObject();
        writer.name("amount").value(amount);
        writer.name("date").value(date.toString());
        String currVal = "";
        switch (curr) {
            case EURO:
                currVal = "EURO";
                break;
            case DOLLAR:
                currVal = "DOLLAR";
                break;
        }
        writer.name("curr").value(currVal);
        writer.name("comm").value(comment);
        writer.endObject();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public SimpleDate getDate() {
        return date;
    }

    public void setDate(SimpleDate date) {
        this.date = date;
    }

    public Currency getCurr() {
        return curr;
    }

    public void setCurr(Currency curr) {
        this.curr = curr;
    }

    public String getComment() {
        return this.comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
