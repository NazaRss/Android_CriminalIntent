package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

public class DeleteCrimeDialog extends DialogFragment {

    private static final String ARG_DATE = "date";
    private TextView mDeleteDialog;
    public static final String EXTRA_RESULT_OK ="com.bignerdranch.android.criminalintent.date";

    public static DeleteCrimeDialog newInstance(String title) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATE, title);
        DeleteCrimeDialog fragment = new DeleteCrimeDialog();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = (String) getArguments().getSerializable(ARG_DATE);

        View v = LayoutInflater.from(getActivity())
                .inflate(R.layout.dialog_delete_crime, null);

        mDeleteDialog = (TextView) v.findViewById(R.id.dialog_crime_delete);
//        mDeleteDialog.setText(R.string.dialog_crime_delete_text);

        return new AlertDialog.Builder(getActivity())
                .setView(v)
                .setTitle("Delete crime: " + title + " ?")
                .setNegativeButton(R.string.cancel_button, null)
                .setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                sendResult(Activity.RESULT_OK);
                            }
                        })
                .create();
    }

    private void sendResult(int resultCode) {
        if (getTargetFragment() == null) {
            return;
        }
        Intent intent = new Intent();
        boolean result = true;
        intent.putExtra(EXTRA_RESULT_OK, result);
        getTargetFragment()
                .onActivityResult(getTargetRequestCode(), resultCode, intent);
    }
}
