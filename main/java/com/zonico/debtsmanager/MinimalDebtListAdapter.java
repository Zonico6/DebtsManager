package com.zonico.debtsmanager;

import android.content.Context;
import android.support.annotation.ColorInt;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import static com.zonico.debtsmanager.ShowDebtsActivity.selectedItemPositions;

public final class MinimalDebtListAdapter extends BaseDebtListAdapter {
    public MinimalDebtListAdapter(@NonNull Context context, @LayoutRes int resource, @NonNull LinkedList<Debt> objects,
                                  Map<String, Debtor> debtors, final DebtsFilter filter, @ColorInt final int[] viewBackgroundColors) {
        super(context, resource, objects, debtors, filter, viewBackgroundColors);
    }

    private static class ViewHolderUpdate {
        public ViewHolder holder;
        public View view;

        public ViewHolderUpdate(final ViewHolder holder, final View view) {
            this.holder = holder;
            this.view = view;
        }
    }

    public static class ViewHolder {
        TextView amount;
        TextView date;
        TextView comment;
    }

    @NonNull
    @Override
    public View getView(final int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolderUpdate retPair = updateViewHolder(convertView, parent);
        ViewHolder holder = retPair.holder;
        convertView = retPair.view;

        final Debt debt = getItem(position);

        // Set alternating colors to get a better contrast between the several views
        // and more clarified boundaries
        int bgColor;
        if (debt.isPaid()) {
            bgColor = ShowDebtsActivity.darken(getColor(position), 0.26f);
        } else {
            bgColor = getColor(position);
        }
        convertView.setBackgroundColor(bgColor);

        holder.amount.setText(Double.toString(debt.getAmount()));
        holder.date.setText(debt.getDate().toString());

        if (!debt.getComment().isEmpty()) {
            holder.comment.setText(debt.getComment());
        } else {
            holder.comment.setText(context.getString(R.string.no_comment));
        }

        return convertView;
    }

    private ViewHolderUpdate updateViewHolder(@Nullable View convertView, @NonNull ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            convertView = inflater.inflate(resource, parent, false);

            holder = new ViewHolder();
            holder.amount = (TextView) convertView.findViewById(R.id.amount);
            holder.date = (TextView) convertView.findViewById(R.id.date);
            holder.comment = (TextView) convertView.findViewById(R.id.comment);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        return new ViewHolderUpdate(holder, convertView);
    }
}
