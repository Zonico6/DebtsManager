package com.zonico.debtsmanager;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.JsonWriter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.Map;

import static com.zonico.debtsmanager.BuildConfig.DEBUG;
import static com.zonico.debtsmanager.HomeScreenActivity.DEBTORS_EXTRA_NAME;

public class AddDebtActivity extends AppCompatActivity {
    private static final String TAG = "AddDebtActivity";

    public final static SimpleDate today = new SimpleDate();
    public static Map<String, Debtor> debtors;

    TextView nameTV;
    TextView euroTV;
    TextView centTV;
    TextView commTV;
    TextView dateTV;

    private SimpleDate date;

    DatePickerDialog.OnDateSetListener dateSetListener;

    @SuppressWarnings("unchecked")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_debt);

        debtors = HomeScreenActivity.pullDebtors(getIntent().getSerializableExtra(DEBTORS_EXTRA_NAME));

        nameTV = (TextView) findViewById(R.id.debtor_name);
        euroTV = (TextView) findViewById(R.id.euro);
        centTV = (TextView) findViewById(R.id.cent);
        commTV = (TextView) findViewById(R.id.comment);
        dateTV = (TextView) findViewById(R.id.date);
        dateTV.setText(today.toString());
        date = today;

        dateSetListener = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                date = new SimpleDate(day, month + 1, year);
                dateTV.setText(date.toString());
            }
        };

        dateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePickerDialog dialog = new DatePickerDialog(
                        AddDebtActivity.this,
                        android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        dateSetListener,
                        date.getYear(), date.getMonth() - 1, date.getDay());
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();
            }
        });

        findViewById(R.id.add).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameTV.getText().length() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.value_necessary) + "Name", Toast.LENGTH_SHORT).show();
                    return;
                }

                int euro;
                try {
                    euro = Integer.parseInt(euroTV.getText().toString());
                } catch (NumberFormatException e) {
                    euro = 0;
                }
                short cent;
                try {
                    cent = Short.parseShort(centTV.getText().toString());
                } catch (NumberFormatException e) {
                    cent = 0;
                }
                if (euro + cent <= 0) {
                    Toast.makeText(getApplicationContext(), R.string.amount_less_zero, Toast.LENGTH_LONG).show();
                    return;
                }

                String name = toNameFormat(nameTV.getText().toString());

                Debt debt = new Debt(false,
                        name,
                        makeMoneyAmount(euro, cent),
                        date,
                        commTV.getText().toString());

                if (debtors.containsKey(name)) {
                    debtors.get(name).addDebt(debt);
                } else {
                    Debtor debtor = new Debtor(name);
                    debtor.addDebt(debt);
                    debtors.put(name, debtor);
                }

                Intent i = new Intent(AddDebtActivity.this, ShowDebtsActivity.class);
                i.putExtra(HomeScreenActivity.DEBTORS_EXTRA_NAME, debtors.values().toArray());
                i.putExtra(ShowDebtsActivity.DEBTS_FILTER_EXTRA_NAME, DebtsFilter.UNPAID);
                try {
                    saveDebts();
                } catch (java.io.IOException e) {
                    e.printStackTrace();
                }
                startActivity(i);
            }
        });

        ((AutoCompleteTextView) findViewById(R.id.debtor_name)).setAdapter(new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, debtors.keySet().toArray(new String[debtors.size()])));
    }

    private void saveDebts() throws java.io.IOException {
        // android docs: https://developer.android.com/reference/android/util/JsonWriter.html

        FileOutputStream saveFileStream = openFileOutput(HomeScreenActivity.DEBTOR_SAVE_FILE_NAME, Context.MODE_PRIVATE);
        JsonWriter writer = new JsonWriter(new OutputStreamWriter(saveFileStream, "UTF-8"));
        if (DEBUG)
            writer.setIndent("    ");
        writer.beginArray();

        if (DEBUG) { // set to false to reset debtors
            for (Debtor debtor : debtors.values()) {
                debtor.writeJson(writer);
            }
        }
        writer.endArray();
        writer.close();
        Log.d(TAG, "saveDebts: Saved Debts!");
    }

    public static double makeMoneyAmount(int euro, short cent) {
        return (double) euro + (double) cent / 100d;
    }

    public static String toNameFormat(String input) {
        return input.substring(0, 1).toUpperCase() + input.substring(1).toLowerCase();
    }
}
