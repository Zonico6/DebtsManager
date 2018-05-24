/**
 * {
 *     "name":"Niklas",
 *     "paidDebts": [
 *          {
 *              ; debt
 *          }
 *     ],
 *     "unpaidDebts": [
 *          {
 *
 *          }
 *     ]
 * }
 */

package com.zonico.debtsmanager;

import android.support.annotation.Nullable;
import android.util.JsonWriter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;

public class Debtor implements Serializable {

    private String name;
    private ArrayList<Debt> paidDebts;
    private ArrayList<Debt> unpaidDebts;

    public Debtor(JSONObject debtor) throws org.json.JSONException {
        this.name = debtor.getString("name");
        paidDebts = createDebts(debtor.getJSONArray("paidDebts"), true);
        unpaidDebts = createDebts(debtor.getJSONArray("unpaidDebts"), false);
    }
    public Debtor(String name) {
        this.name = name;
        paidDebts = new ArrayList<>();
        unpaidDebts = new ArrayList<>();
    }

    private ArrayList<Debt> createDebts(JSONArray debts, boolean paid) throws org.json.JSONException {
        ArrayList<Debt> retDebts = new ArrayList<>(debts.length());
        for (int i = 0; i < debts.length(); i++) {
            retDebts.add(new Debt((JSONObject)debts.get(i), name, paid));
        }
        return retDebts;
    }

    public void writeJson(JsonWriter writer) throws java.io.IOException {
        writer.beginObject();
        writer.name("name").value(this.name);

        writer.name("paidDebts").beginArray();
        for (Debt debt : paidDebts) {
            debt.writeJson(writer);
        }
        writer.endArray();

        writer.name("unpaidDebts").beginArray();
        for (Debt debt : unpaidDebts) {
            debt.writeJson(writer);
        }
        writer.endArray();
        writer.endObject();
    }

    public void addDebt(Debt debt) {
        if (debt.isPaid()) {
            paidDebts.add(debt);
        } else {
            unpaidDebts.add(debt);
        }
    }

    public void removeDebt(Debt debt) {
        if (debt.isPaid()) {
            paidDebts.remove(debt);
        } else {
            unpaidDebts.remove(debt);
        }
    }

    public Debt swapDebt(Debt debt) {
        if (debt.isPaid()) {
            if (paidDebts.contains(debt)) {
                paidDebts.remove(debt);
                debt.setPaid(false);
                unpaidDebts.add(debt);
            } else
                return null;
        } else {
            if (unpaidDebts.contains(debt)) {
                unpaidDebts.remove(debt);
                debt.setPaid(true);
                paidDebts.add(debt);
            } else
                return null;
        }
        return debt;
    }
    public void swapDebt(int index, boolean paid) {
        if (paid) {
            unpaidDebts.add(paidDebts.get(index));
            paidDebts.remove(index);
        } else {
            paidDebts.add(unpaidDebts.get(index));
            unpaidDebts.remove(index);
        }
    }

    public int size() {
        return paidDebts.size() + unpaidDebts.size();
    }

    public Debt[] getDebts() {
        Debt[] debts = new Debt[size()];
        for (int i = 0; i < size(); i++) {
            if (i < paidDebts.size()) {
                debts[i] = paidDebts.get(i);
            } else {
                debts[i] = unpaidDebts.get(i - paidDebts.size());
            }
        }
        return debts;
    }

    public double getPaidSum() {
        double retSum = 0;
        for (Debt i : paidDebts) {
            retSum += i.getAmount();
        }
        return retSum;
    }
    public double getUnpaidSum() {
        double retSum = 0;
        for (Debt i : unpaidDebts) {
            retSum += i.getAmount();
        }
        return retSum;
    }
    public double getTotalSum() {
        return getPaidSum() + getUnpaidSum();
    }

    public void addPaidDebt(float amount, SimpleDate date, @Nullable String comment, Debt.Currency currency) {
        paidDebts.add(new Debt(false, this.name, amount, date, comment, currency));
    }
    public void addPaidDebt(float amount, SimpleDate date, @Nullable String comment) {
        paidDebts.add(new Debt(false, this.name, amount, date, comment));
    }

    public void addUnpaidDebt(float amount, SimpleDate date, @Nullable String comment, Debt.Currency currency) {
        unpaidDebts.add(new Debt(false, this.name, amount, date, comment, currency));
    }
    public void addUnpaidDebt(float amount, SimpleDate date, @Nullable String comment) {
        unpaidDebts.add(new Debt(false, this.name, amount, date, comment));
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<Debt> getPaidDebts() {
        return paidDebts;
    }

    public void setPaidDebts(ArrayList<Debt> paidDebts) {
        this.paidDebts = paidDebts;
    }

    public ArrayList<Debt> getUnpaidDebts() {
        return unpaidDebts;
    }

    public void setUnpaidDebts(ArrayList<Debt> unpaidDebts) {
        this.unpaidDebts = unpaidDebts;
    }
}
