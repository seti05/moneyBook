package com.example.moneybook.chart;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.example.moneybook.R;

import java.util.Calendar;

public class MonthDayPicker extends DialogFragment{

    private static final int FIRST_DAY = 1;

    private DatePickerDialog.OnDateSetListener listener;
    public Calendar cal = Calendar.getInstance();

    public void setListener(DatePickerDialog.OnDateSetListener listener){
        this.listener = listener;
    }

    Button btnConfirm, btnCancel;
    String yearStr;
    Bundle bundle = null;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();

        View dialog = inflater.inflate(R.layout.activity_month_day_picker, null);

        bundle = getArguments();

        if(bundle != null) {
            yearStr = bundle.getString("year");
        }

        btnConfirm = dialog.findViewById(R.id.btn_confirm);
        btnCancel = dialog.findViewById(R.id.btn_cacncel);

        final NumberPicker monthPicker = dialog.findViewById(R.id.picker_month);
        final NumberPicker dayPicker = dialog.findViewById(R.id.picker_day);

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthDayPicker.this.getDialog().cancel();
            }
        });

        btnConfirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onDateSet(null, Integer.parseInt(yearStr), monthPicker.getValue(), dayPicker.getValue());
                MonthDayPicker.this.getDialog().cancel();
            }
        });

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(cal.get(Calendar.MONTH) + 1);

        if(monthPicker.getValue() == 1 || monthPicker.getValue() == 3 || monthPicker.getValue() == 5 || monthPicker.getValue() == 7 || monthPicker.getValue() == 8 || monthPicker.getValue() == 10 || monthPicker.getValue() == 12) {
            dayPicker.setMinValue(FIRST_DAY);
            dayPicker.setMaxValue(31);
        } else if(monthPicker.getValue() == 2){
           if(Integer.parseInt(yearStr) % 4 == 0 && Integer.parseInt(yearStr) % 100 != 0 || Integer.parseInt(yearStr) % 400 == 0){
               dayPicker.setMinValue(FIRST_DAY);
               dayPicker.setMaxValue(29);
           } else {
               dayPicker.setMinValue(FIRST_DAY);
               dayPicker.setMaxValue(28);
           }
        } else if(monthPicker.getValue() == 4 || monthPicker.getValue() == 6 || monthPicker.getValue() == 9 || monthPicker.getValue() == 11){
            dayPicker.setMinValue(FIRST_DAY);
            dayPicker.setMaxValue(30);
        }

        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                if(monthPicker.getValue() == 1 || monthPicker.getValue() == 3 || monthPicker.getValue() == 5 || monthPicker.getValue() == 7 || monthPicker.getValue() == 8 || monthPicker.getValue() == 10 || monthPicker.getValue() == 12) {
                    dayPicker.setMinValue(FIRST_DAY);
                    dayPicker.setMaxValue(31);
                } else if(monthPicker.getValue() == 2){
                    if(Integer.parseInt(yearStr) % 4 == 0 && Integer.parseInt(yearStr) % 100 != 0 || Integer.parseInt(yearStr) % 400 == 0){
                        dayPicker.setMinValue(FIRST_DAY);
                        dayPicker.setMaxValue(29);
                    } else {
                        dayPicker.setMinValue(FIRST_DAY);
                        dayPicker.setMaxValue(28);
                    }
                } else if(monthPicker.getValue() == 4 || monthPicker.getValue() == 6 || monthPicker.getValue() == 9 || monthPicker.getValue() == 11){
                    dayPicker.setMinValue(FIRST_DAY);
                    dayPicker.setMaxValue(30);
                }
            }
        });

        builder.setView(dialog);

        return  builder.create();
    }

}