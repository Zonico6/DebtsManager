package com.zonico.debtsmanager;

/*
 - Intent:
     - Extras:
          - Map of debtors
          - Name of debtor, whose stats the activity displays: DEBTOR_NAME_EXTRA_NAME
          - DebtsFilter
 */

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;

import static com.zonico.debtsmanager.BuildConfig.DEBUG;
import static com.zonico.debtsmanager.HomeScreenActivity.DEBTORS_EXTRA_NAME;
import static com.zonico.debtsmanager.ShowDebtsActivity.DEBTS_FILTER_EXTRA_NAME;
import static com.zonico.debtsmanager.ShowDebtsActivity.DEBT_COMPARATOR_LATEST_LAST;
import static com.zonico.debtsmanager.ShowDebtsActivity.debtorsToArray;
import static com.zonico.debtsmanager.ShowDebtsActivity.extractDebts;

public class DebtorViewActivity extends AppCompatActivity implements PayBackAmountDialogFragment.PayBackDialogListener {
    private static final String TAG = "DebtorViewActivity";

    public static final String DEBTOR_NAME_EXTRA_NAME = "debtorName";

    private ListView debtsLV;

    private DebtsFilter filter;
    private MinimalDebtListAdapter adapter;
    private Map<String, Debtor> debtors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debtor_view);

        debtors = HomeScreenActivity.pullDebtors(getIntent().getSerializableExtra(DEBTORS_EXTRA_NAME));
        filter = new DebtsFilter( (int)getIntent().getSerializableExtra(DEBTS_FILTER_EXTRA_NAME) );

        ((TextView)findViewById(R.id.total_tv)).setText(Double.toString(getDebtor().getUnpaidSum()));
        ((TextView)findViewById(R.id.total_debts_tv)).setText(Double.toString(getDebtor().getTotalSum()));

        debtsLV = (ListView)findViewById(R.id.debts_lv);

        updateListViewAdapter();
    }

    // Read Only
    private Debtor getDebtor() {
        return debtors.get(getDebtorName());
    }

    private String getDebtorName() {
        return getIntent().getStringExtra(DEBTOR_NAME_EXTRA_NAME);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // android docs: https://developer.android.com/guide/topics/ui/menus.html
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.debtor_view_menu, menu);
        setTitle(getDebtor().getName());

        if (filter.isSet(DebtsFilter.PAID)) {
            menu.findItem(R.id.show_paid).setChecked(true);
        } else {
            menu.findItem(R.id.show_paid).setChecked(false);
        }
        if (filter.isSet(DebtsFilter.UNPAID)) {
            menu.findItem(R.id.show_unpaid).setChecked(true);
        } else {
            menu.findItem(R.id.show_unpaid).setChecked(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.pay_back:
                showPayBackDialog();
                return true;
            case R.id.goto_show_debts:
                Intent i = new Intent(DebtorViewActivity.this, ShowDebtsActivity.class);
                i.putExtra(DEBTORS_EXTRA_NAME, debtorsToArray(debtors));
                i.putExtra(ShowDebtsActivity.DEBTS_FILTER_EXTRA_NAME, DebtsFilter.PAID_AND_UNPAID);
                startActivity(i);
            case R.id.show_paid:
                // invert because it's still the previous state
                if (!item.isChecked()) {
                    item.setChecked(true);
                    filter.add(DebtsFilter.PAID);
                } else {
                    item.setChecked(false);
                    filter.remove(DebtsFilter.PAID);
                }
                updateListViewAdapter();
                return true;
            case R.id.show_unpaid:
                if (!item.isChecked()) {
                    item.setChecked(true);
                    filter.add(DebtsFilter.UNPAID);
                } else {
                    item.setChecked(false);
                    filter.remove(DebtsFilter.UNPAID);
                }
                updateListViewAdapter();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onPause() {
        try {
            saveDebts();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public void onPositiveButtonClick(DialogFragment dialog, final double payBackAmount) {
        payBack(payBackAmount);
    }

    @Override
    public void onNegativeButtonClick(DialogFragment dialog) {}

    private void updateListViewAdapter() {
        if (adapter == null) {
            adapter = new MinimalDebtListAdapter(this, R.layout.debt_view_layout_minimal,
                    extractDebts(getDebtor(), DEBT_COMPARATOR_LATEST_LAST, filter),
                    debtors, filter, ShowDebtsActivity.getDebtViewBackgroundColors(getApplicationContext()));

            debtsLV.setAdapter(adapter);
        } else {
            adapter.setDebtsFilter(filter);
            adapter.clear();
            adapter.addAll(extractDebts(getDebtor(), DEBT_COMPARATOR_LATEST_LAST, filter));
            adapter.notifyDataSetChanged();
        }
    }

    public void showPayBackDialog() {
        DialogFragment fragment = new PayBackAmountDialogFragment();
        fragment.show(getFragmentManager(), "pay_back");
    }

    // Huge Bodge: only the first index from indexPriorities is used anyway!
    public void payBack(final double amount, final int... indexPriorities) {
        double debtAmount = getDebtor().getUnpaidDebts().get(indexPriorities[0]).getAmount();
        if (amount >= debtAmount) {
            debtors.get(getDebtorName()).swapDebt(indexPriorities[0], false);
            payBack(amount - debtAmount);
        } else {
            debtors.get(getDebtorName()).getUnpaidDebts().get(indexPriorities[0]).amount -= amount;
        }
    }

    public void payBack(final double amount) {
        payBack(amount, 0);
    }

    private void saveDebts() throws java.io.IOException {
        // android docs: https://developer.android.com/reference/android/util/JsonWriter.html

        FileOutputStream saveFileStream = openFileOutput(HomeScreenActivity.DEBTOR_SAVE_FILE_NAME, Context.MODE_PRIVATE);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(saveFileStream, "UTF-8"));
        if (DEBUG) {
            writer.setIndent("    ");
        }
        writer.beginArray();

        for (Debtor debtor : debtors.values()) {
            debtor.writeJson(writer);
        }

        writer.endArray();
        writer.close();
        Log.i(TAG, "saveDebts: Saved Debts!");
    }
}
