package com.serp1983.nokiacomposer.util;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.serp1983.nokiacomposer.R;

import java.util.ArrayList;

public class FileSaveDialog extends Dialog {
    public static final int FILE_KIND_ALARM = 0;
    public static final int FILE_KIND_NOTIFICATION = 1;
    public static final int FILE_KIND_RINGTONE = 2;

    private Spinner mTypeSpinner;
    private EditText mFilename;
    private Message mResponse;
    private String mOriginalName;
    private ArrayList<String> mTypeArray;
    private int mPreviousSelection;

    public FileSaveDialog(Context context,
                          Resources resources,
                          String originalName,
                          Message response) {
        super(context);

        // Inflate our UI from its XML layout description.
        setContentView(R.layout.file_save);

        setTitle(resources.getString(R.string.file_save_title));

        mTypeArray = new ArrayList<>();
        mTypeArray.add(resources.getString(R.string.type_alarm));
        mTypeArray.add(resources.getString(R.string.type_notification));
        mTypeArray.add(resources.getString(R.string.type_ringtone));

        mFilename = (EditText)findViewById(R.id.filename);
        mOriginalName = originalName;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                context, android.R.layout.simple_spinner_item, mTypeArray);
        adapter.setDropDownViewResource(
                android.R.layout.simple_spinner_dropdown_item);
        mTypeSpinner = (Spinner) findViewById(R.id.ringtone_type);
        mTypeSpinner.setAdapter(adapter);
        mTypeSpinner.setSelection(FILE_KIND_RINGTONE);
        mPreviousSelection = FILE_KIND_RINGTONE;

        setFilenameEditBoxFromName(false);

        mTypeSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView parent,
                                       View v,
                                       int position,
                                       long id) {
                setFilenameEditBoxFromName(true);
            }

            public void onNothingSelected(AdapterView parent) {
            }
        });

        Button save = (Button)findViewById(R.id.save);
        View.OnClickListener saveListener = new View.OnClickListener() {
            public void onClick(View view) {
                mResponse.obj = mFilename.getText();
                mResponse.arg1 = mTypeSpinner.getSelectedItemPosition();
                mResponse.sendToTarget();
                dismiss();
            }
        };
        save.setOnClickListener(saveListener);
        Button cancel = (Button)findViewById(R.id.cancel);
        View.OnClickListener cancelListener = new View.OnClickListener() {
            public void onClick(View view) {
                dismiss();
            }
        };
        cancel.setOnClickListener(cancelListener);
        mResponse = response;
    }

    private void setFilenameEditBoxFromName(boolean onlyIfNotEdited) {
        if (onlyIfNotEdited) {
            CharSequence currentText = mFilename.getText();
            String expectedText = mOriginalName + " " +
                    mTypeArray.get(mPreviousSelection);

            if (!expectedText.contentEquals(currentText)) {
                return;
            }
        }

        int newSelection = mTypeSpinner.getSelectedItemPosition();
        String newSuffix = mTypeArray.get(newSelection);
        mFilename.setText(mOriginalName + " " + newSuffix);
        mPreviousSelection = mTypeSpinner.getSelectedItemPosition();
    }

}