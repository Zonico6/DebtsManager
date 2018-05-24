package com.zonico.debtsmanager;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.ActionMode;
import android.util.JsonWriter;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

public class ShowDebtsActivity extends AppCompatActivity {

    private static final String TAG = "ShowDebtsActivity";
    public static final String DEBTS_FILTER_EXTRA_NAME = "debts_filter";

    public static int getDebtViewBackgroundColor(final Context context, final int index) {
        int res;
        switch (index) {
            case 0: res = R.color.colorDebtView0; break;
            case 1: res = R.color.colorDebtView1; break;
            case 2: res = R.color.colorDebtView2; break;
            default: throw new IllegalArgumentException("Must be in the range of 0 to 2");
        }
        return context.getColor(res);
    }
    public static int[] getDebtViewBackgroundColors(final Context context) {
        int[] backgroundColors = new int[3];
        for (int i = 0; i < backgroundColors.length; i++) {
            backgroundColors[i] = getDebtViewBackgroundColor(context, i);
        }
        return backgroundColors;
    }
    /*public static final int[] LIST_VIEW_BACKGROUND_COLORS = {
            0x732F6E,
            0xD94E8F,
            0xF2637E
    };
    /*public static final int[] LIST_VIEW_BACKGROUND_COLORS = {
            Color.rgb(238, 228, 220),
            Color.rgb(225, 235, 225),
            Color.rgb(230, 230, 230)
    }; Deprecated colors*/

    public static final Comparator<Debt> DEBT_COMPARATOR_LATEST_FIRST = new Comparator<Debt>() {
        @Override
        public int compare(Debt r, Debt l) {
            return r.getDate().compareTo(l.getDate());
        }
    };

    public static final Comparator<Debt> DEBT_COMPARATOR_LATEST_LAST = new Comparator<Debt>() {
        @Override
        public int compare(Debt o1, Debt o2) {
            return o2.getDate().compareTo(o1.getDate());
        }
    };

    public static TreeSet<Integer> selectedItemPositions;



    private DebtsFilter filter;
    private ListView debtsLV;
    private DebtListAdapter adapter;
    private Map<String, Debtor> debtors;
    private List<Debt> deletedDebts;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_debts);

        deletedDebts = new LinkedList<>();

        filter = new DebtsFilter( (int)getIntent().getSerializableExtra(DEBTS_FILTER_EXTRA_NAME) );
        debtors = HomeScreenActivity.pullDebtors(getIntent().getSerializableExtra(HomeScreenActivity.DEBTORS_EXTRA_NAME));

        debtsLV = (ListView)findViewById(R.id.listView);

        FloatingActionButton addDebtFab = (FloatingActionButton) findViewById(R.id.add_debt_fab);
        addDebtFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(ShowDebtsActivity.this, AddDebtActivity.class);
                i.putExtra(HomeScreenActivity.DEBTORS_EXTRA_NAME, debtorsToArray(debtors));
                startActivity(i);
            }
        });

        //DEBUG
        debtsLV.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemLongClick: Item long clicked");
                return false;
            }
        });

        debtsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: Item clicked, position: " + position + ", id: " + id);
            }
        });

        updateListViewAdapter();

        debtsLV.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE_MODAL);
        debtsLV.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {

            private int deleteSelectedItems() {
                int retDeletedItemCount = 0;

                for (int position : selectedItemPositions) {
                    Debt delDebt = adapter.getItem(position);
                    deletedDebts.add(delDebt);
                }

                for (Debt debt : deletedDebts) {
                    adapter.remove(debt);
                    retDeletedItemCount++;
                }
                return retDeletedItemCount;
            }

            public View getChildAtPosition(int pos) {
                final int firstListItemPosition = debtsLV.getFirstVisiblePosition();
                final int lastListItemPosition = firstListItemPosition + debtsLV.getChildCount() - 1;

                if (pos < firstListItemPosition || pos > lastListItemPosition ) {
                    return debtsLV.getAdapter().getView(pos, null, debtsLV);
                } else {
                    final int childIndex = pos - firstListItemPosition;
                    return debtsLV.getChildAt(childIndex);
                }
            }

            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                Log.d(TAG, "onItemCheckedStateChanged: position: " + position + ", id: " + id);
                if (checked) {
                    selectedItemPositions.add(position);
                    getChildAtPosition(position).setBackgroundColor(darken(adapter.getColor(id), 0.24f));
                } else {
                    selectedItemPositions.remove(position);
                    getChildAtPosition(position).setBackgroundColor(adapter.getColor(id));
                }
                mode.setTitle(getString(R.string.items_selected) + selectedItemPositions.size());
            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                selectedItemPositions = new TreeSet<>();
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.context_menu_select_debts, menu);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.delete:
                        deleteSelectedItems();
                        mode.finish();
                        return true;
                    default:
                        return false;
                }
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                selectedItemPositions = null;
            }
        });
    }

    @Override
    protected void onPause() {
        debtors = filterDebts(adapter.getDebtors(), deletedDebts);
        try {
            saveDebts();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.onPause();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // android docs: https://developer.android.com/guide/topics/ui/menus.html

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_show_debts, menu);

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
        switch (item.getItemId()) { /*
            case R.id.add_new_debt:
                Intent i = new Intent(this, AddDebtActivity.class);
                i.putExtra(HomeScreenActivity.DEBTORS_EXTRA_NAME, debtorsToArray(debtors));
                startActivity(i);
                return true; */
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

    public static LinkedList<Debt> extractDebts(final Debtor debtor, final Comparator<Debt> c, final DebtsFilter filter) {
        LinkedList<Debt> retList = new LinkedList<>();

        if (filter.isSet(DebtsFilter.PAID)) {
            retList.addAll(debtor.getPaidDebts());
        }
        if (filter.isSet(DebtsFilter.UNPAID)) {
            retList.addAll(debtor.getUnpaidDebts());
        }

        Collections.sort(retList, c);
        return retList;
    }

    public static LinkedList<Debt> extractDebts(final Debtor[] debtors, final Comparator<Debt> c, final DebtsFilter filter) {
        LinkedList<Debt> retList = new LinkedList<>();
        for (Debtor debtor : debtors) {
            if (filter.isSet(DebtsFilter.PAID)) {
                retList.addAll(debtor.getPaidDebts());
            }
            if (filter.isSet(DebtsFilter.UNPAID)) {
                retList.addAll(debtor.getUnpaidDebts());
            }
        }

        Collections.sort(retList, c);
        return retList;
    }

    public static Map<String, Debtor> filterDebts(Map<String, Debtor> debtors, final List<Debt> debts) {
        for (Debt debt : debts) {
            debtors.get(debt.getName()).removeDebt(debt);
        }
        return debtors;
    }

    private void saveDebts() throws java.io.IOException {
        // android docs: https://developer.android.com/reference/android/util/JsonWriter.html

        FileOutputStream saveFileStream = openFileOutput(HomeScreenActivity.DEBTOR_SAVE_FILE_NAME, Context.MODE_PRIVATE);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(saveFileStream, "UTF-8"));
        if (BuildConfig.DEBUG) {
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

    public static Debtor[] debtorsToArray(final Map<?, Debtor> map) {
        return map.values().toArray(new Debtor[map.size()]);
    }
    @SuppressWarnings("unchecked")
    public static<T> T[] mapToArray(final Map<?, T> map) {
        return (T[])map.values().toArray();
    }

    private void updateListViewAdapter() {
        if (adapter == null) {
            adapter = new DebtListAdapter(this, R.layout.debt_view_layout,
                    extractDebts(debtorsToArray(debtors), DEBT_COMPARATOR_LATEST_LAST, filter),
                    debtors, filter, getDebtViewBackgroundColors(getApplicationContext()));

            debtsLV.setAdapter(adapter);
        } else {
            adapter.setDebtsFilter(filter);
            adapter.clear();
            adapter.addAll(extractDebts(debtorsToArray(filterDebts(adapter.getDebtors(), deletedDebts)), DEBT_COMPARATOR_LATEST_LAST, filter));
            adapter.notifyDataSetChanged();
        }
    }

    public static int darken(int inCol_rgb, float darkenVal) {
        float[] inCol_hsv = new float[3];
        Color.colorToHSV(inCol_rgb, inCol_hsv);
        inCol_hsv[2] = inCol_hsv[2] - darkenVal < 0 ? 0 : inCol_hsv[2] - darkenVal;
        return Color.HSVToColor(inCol_hsv);
    }
}
