package com.zonico.debtsmanager;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Marc on 21.10.2017.
 */

public abstract class BaseDebtListAdapter extends ArrayAdapter<Debt> {

    protected int[] bgColors;

    protected Context context;
    protected int resource;

    protected Map<String, Debtor> debtors;
    protected DebtsFilter filter;

    public BaseDebtListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull LinkedList<Debt> objects,
                           Map<String, Debtor> debtors, final DebtsFilter filter, @ColorInt final int[] viewBackgroundColors) {
        super(context, resource, objects);
        this.context = context;
        this.resource = resource;
        this.debtors = debtors;
        this.filter = filter;
        this.bgColors = viewBackgroundColors;
    }

    public int getColor(long id) {
        return bgColors[(int)id % (bgColors.length)];
    }

    public DebtsFilter getDebtsFilter() {
        return filter;
    }
    public void setDebtsFilter(DebtsFilter filter) {
        this.filter = filter;
    }

    public Map<String, Debtor> getDebtors() {
        return debtors;
    }
}
