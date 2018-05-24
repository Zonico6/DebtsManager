package com.zonico.debtsmanager;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import static com.zonico.debtsmanager.ShowDebtsActivity.selectedItemPositions;

/**
 * Created by Marc on 12.08.2017.
 */

public final class DebtListAdapter extends BaseDebtListAdapter {

    private Map<Integer, Boolean> paidVals;

    public static class ViewHolder {
        CheckBox paid;
        TextView name;
        TextView amount;
        TextView date;
        TextView comment;
    }
    private static class ViewHolderUpdate {
        public ViewHolder holder;
        public View view;

        public ViewHolderUpdate(final ViewHolder holder, final View view) {
            this.holder = holder;
            this.view = view;
        }
    }

    public DebtListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull LinkedList<Debt> objects,
                           Map<String, Debtor> debtors, final DebtsFilter filter, @ColorInt final int[] viewBackgroundColors) {
        super(context, resource, objects, debtors, filter, viewBackgroundColors);
        paidVals = new TreeMap<>();
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolderUpdate retPair = updateViewHolder(convertView, parent);
        ViewHolder holder = retPair.holder;
        convertView = retPair.view;


        final Debt debt = getItem(position);
        final String name = debt.getName();

        // Set alternating colors to get a better contrast between the several views
        // and more clarified boundaries
        if (selectedItemPositions != null && selectedItemPositions.contains(position)) {
            convertView.setBackgroundColor(ShowDebtsActivity.darken(getColor(position), 0.24f));
        } else {
            convertView.setBackgroundColor(getColor(position));
        }

        // since apparently you can't depend on the underlying data set of the adapter,
        // pull the values from the paidVals map.
        if (paidVals.containsKey(position)) {
            holder.paid.setChecked(paidVals.get(position));
        } else {
            holder.paid.setChecked(debt.isPaid());
        }

        holder.name.setText(name);
        holder.amount.setText("" + debt.getAmount());
        holder.date.setText(debt.getDate().toString());

        if (!debt.getComment().isEmpty()) {
            holder.comment.setText(debt.getComment());
        } else {
            holder.comment.setText(context.getString(R.string.no_comment));
        }

        holder.paid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paidVals.containsKey(position)) {
                    paidVals.remove(position);
                }

                paidVals.put(position,
                        debtors.get(name).swapDebt(debt)
                                .isPaid());
            }
        });

        return convertView;
    }

    private ViewHolderUpdate updateViewHolder(@Nullable View convertView, @NonNull ViewGroup parent) {
        // MAYCRASH: Modifying a copy of a view does not change the view itself
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);

            holder = new ViewHolder();
            holder.paid = (CheckBox) convertView.findViewById(R.id.paid);
            holder.name = (TextView) convertView.findViewById(R.id.name);
            holder.amount = (TextView) convertView.findViewById(R.id.amount);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.comment = (TextView) convertView.findViewById(R.id.comment);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return new ViewHolderUpdate(holder, convertView);
    }

    @Override
    public void notifyDataSetChanged() {
        paidVals.clear();
        super.notifyDataSetChanged();
    }

    @Override
    public boolean areAllItemsEnabled()
    {
        return true;
    }

    @Override
    public boolean isEnabled(int arg0)
    {
        return true;
    }
}