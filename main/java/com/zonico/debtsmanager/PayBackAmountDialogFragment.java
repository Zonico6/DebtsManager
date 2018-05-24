package com.zonico.debtsmanager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

public class PayBackAmountDialogFragment extends DialogFragment {
    public interface PayBackDialogListener {
        void onPositiveButtonClick(DialogFragment dialog, final double payBackAmount);
        void onNegativeButtonClick(DialogFragment dialog);
    }

    PayBackDialogListener payBackListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            payBackListener = (PayBackDialogListener) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() + "must implement PayBackAmountDialogFragment interface");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        final View view = inflater.inflate(R.layout.enter_money_layout, null);

        builder.setTitle(R.string.how_much_pay_back).setView(view)
                .setPositiveButton(R.string.pay_back, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        TextView euroTV = (TextView)view.findViewById(R.id.euro);
                        TextView centTV = (TextView)view.findViewById(R.id.cent);
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
                            Toast.makeText(getContext(), R.string.amount_less_zero, Toast.LENGTH_LONG).show();
                            return;
                        }

                        double amount = AddDebtActivity.makeMoneyAmount(euro, cent);
                        ((DebtorViewActivity)getActivity()).onPositiveButtonClick(PayBackAmountDialogFragment.this, amount); // TODO: Add value
                    }
                });
        return builder.create();
    }
}
