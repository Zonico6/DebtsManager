package com.zonico.debtsmanager;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Logger;

import static com.zonico.debtsmanager.DebtorViewActivity.DEBTOR_NAME_EXTRA_NAME;
import static com.zonico.debtsmanager.ShowDebtsActivity.DEBTS_FILTER_EXTRA_NAME;

public class HomeScreenActivity extends AppCompatActivity {
    public static final String VERSION = "1.1b";

    private static final String TAG = "HomeScreenActivity";

    public static final String DEBTORS_EXTRA_NAME = "debtors";
    public static final String DEBTOR_SAVE_FILE_NAME = "debts.txt";

    TextView nameTV;

    private LinkedList debtors;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        try {
            debtors = getDebtors();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getApplicationContext(), R.string.welcome, Toast.LENGTH_SHORT).show();
            debtors = new LinkedList();
        }

        nameTV = (TextView)findViewById(R.id.debtor_name);

        if (debtors.size() != 0) {
            String[] debtorNames = new String[debtors.size()];
            for (int i = 0; i < debtors.size(); i++) {
                debtorNames[i] = ((Debtor) debtors.get(i)).getName();
            }
            // Introduce adapter for the name TextView
            ((AutoCompleteTextView) findViewById(R.id.debtor_name)).setAdapter(new ArrayAdapter<>(this,
                    android.R.layout.simple_dropdown_item_1line, debtorNames));
        }

        findViewById(R.id.goto_debtor_view).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeScreenActivity.this, DebtorViewActivity.class);
                i.putExtra(DEBTOR_NAME_EXTRA_NAME, nameTV.getText().toString());
                i.putExtra(DEBTS_FILTER_EXTRA_NAME, DebtsFilter.UNPAID);
                i.putExtra(DEBTORS_EXTRA_NAME, debtors.toArray());
                startActivity(i);
            }
        });

        findViewById(R.id.add_debt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeScreenActivity.this, AddDebtActivity.class);
                i.putExtra(DEBTORS_EXTRA_NAME, debtors.toArray());
                startActivity(i);
            }
        });
        findViewById(R.id.show_debts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeScreenActivity.this, ShowDebtsActivity.class);
                i.putExtra(DEBTORS_EXTRA_NAME, debtors.toArray());
                i.putExtra(ShowDebtsActivity.DEBTS_FILTER_EXTRA_NAME, DebtsFilter.PAID_AND_UNPAID);
                startActivity(i);
            }
        });
        findViewById(R.id.show_open_debts).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(HomeScreenActivity.this, ShowDebtsActivity.class);
                i.putExtra(DEBTORS_EXTRA_NAME, debtors.toArray());
                i.putExtra(ShowDebtsActivity.DEBTS_FILTER_EXTRA_NAME, DebtsFilter.UNPAID);
                startActivity(i);
            }
        });
    }

    public LinkedList<Debtor> getDebtors() throws org.json.JSONException, java.io.IOException {
        // read save file into a string
        StringBuilder debtorStringBuilder = new StringBuilder();
        try {
            FileInputStream debtorSaveStream = openFileInput(DEBTOR_SAVE_FILE_NAME);
            int c;
            while ((c = debtorSaveStream.read()) != -1) {
                debtorStringBuilder.append(Character.toString((char)c));
            }
        } catch (FileNotFoundException e) {
            try {
                openFileOutput(DEBTOR_SAVE_FILE_NAME, Context.MODE_PRIVATE);
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            }
        }
        String debtorString = debtorStringBuilder.toString();
        if (Log.isLoggable(TAG, Log.DEBUG))
            Log.d(TAG, "getDebtors: \n" + debtorString);
        if (debtorString.length() == 0) {
            return new LinkedList<>();
        }
        return createDebtors(new JSONArray(debtorString));
    }

    public static LinkedList<Debtor> createDebtors(JSONArray debtors) throws org.json.JSONException {
        Log.d(TAG, "createDebtors called");
        LinkedList<Debtor> retDebtors = new LinkedList<>();

        for (int i = 0; i < debtors.length(); i++) {
            retDebtors.add(
                    new Debtor(
                            debtors.getJSONObject(i)));
        }
        return retDebtors;
    }

    public static Map<String, Debtor> pullDebtors(Serializable serializable) {
        Map<String, Debtor> debtors = new TreeMap<>();

        for (Object i :
                (Object[]) serializable) {
            Debtor d = (Debtor) i;
            debtors.put(d.getName(), d);
        }

        return debtors;
    }
}
