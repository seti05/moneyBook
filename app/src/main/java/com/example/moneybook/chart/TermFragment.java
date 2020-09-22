package com.example.moneybook.chart;

import android.app.DatePickerDialog;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.moneybook.DatabaseHelper;
import com.example.moneybook.R;
import com.example.moneybook.daily.DailyInAndOut;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

public class TermFragment extends Fragment {

    TextView termStartText, termEndText, titleText;
    String yearStr, monthStr, dayStr;
    String yearEndStr, monthEndStr, dayEndStr;
    String startTerm, endTerm;
    String sqlEx, sqlIn, sumEx, sumIn, weekEx, weekIn;
    DatabaseHelper dbhelper;
    SQLiteDatabase database;
    TableLayout tableLayout;
    ArrayList<DailyInAndOut> date;
    TableRow tr1, tr2, tr3;
    TextView t1, t2, t3, t4, t5, t6, t7;
    TextView monthIncome, monthExpense, monthSum;
    NumberFormat numberFormat;
    String inSum, exSum, exWeek, inWeek;
    int weekSum;
    TextView nodata;
    ScrollView weekContent;
    TableLayout sumContent;
    Date startDate, endDate;
    View divider;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_term, container, false);

        termStartText = view.findViewById(R.id.termStartText);
        termEndText = view.findViewById(R.id.termEndText);
        titleText = getActivity().findViewById(R.id.titleText);
        tableLayout = view.findViewById(R.id.table);
        date = new ArrayList<DailyInAndOut>();
        monthIncome = view.findViewById(R.id.monthIncomeText);
        monthExpense = view.findViewById(R.id.monthExpenseText);
        monthSum = view.findViewById(R.id.monthSumText);
        numberFormat = NumberFormat.getInstance(Locale.getDefault());
        nodata = view.findViewById(R.id.nodata);
        weekContent = view.findViewById(R.id.weekContent);
        sumContent = view.findViewById(R.id.sumContent);
        divider = view.findViewById(R.id.divider);

        dbhelper = new DatabaseHelper(getActivity());
        database = dbhelper.getWritableDatabase();

        //현재월에 맞는 시작날짜와 끝날짜 지정
        Calendar cal = Calendar.getInstance();
        termStartText.setText((cal.get(Calendar.MONTH) + 1)  + "월 " + cal.getMinimum(Calendar.DAY_OF_MONTH) + "일");
        termEndText.setText((cal.get(Calendar.MONTH) + 1) + "월 " +  cal.getActualMaximum(Calendar.DAY_OF_MONTH) + "일");
        yearStr = String.valueOf(cal.get(Calendar.YEAR));
        yearEndStr = String.valueOf(cal.get(Calendar.YEAR));
        monthStr = String.valueOf(cal.get(Calendar.MONTH) + 1);
        monthEndStr = String.valueOf(cal.get(Calendar.MONTH) + 1);
        if(monthStr.length() == 1){
            monthStr = "0" + monthStr;
        }
        if(monthEndStr.length() == 1){
            monthEndStr = "0" + monthEndStr;
        }
        dayStr = String.valueOf(cal.getMinimum(Calendar.DAY_OF_MONTH));
        dayEndStr = String.valueOf(cal.getActualMaximum(Calendar.DAY_OF_MONTH));
        if(dayStr.length() == 1){
            dayStr = "0" + dayStr;
        }
        startTerm = yearStr + "-" + monthStr + "-" + dayStr;
        endTerm = yearEndStr + "-" + monthEndStr + "-" + dayEndStr;
        termSelect();

        //날짜을 위한 이벤트
        //시작날짜 이벤트
        termStartText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthDayPicker picker = new MonthDayPicker();
                Bundle yearBundle = new Bundle();
                yearBundle.putString("year",titleText.getText().toString().substring(0, 4));
                picker.setArguments(yearBundle);
                picker.setListener(startListener);
                picker.show(getFragmentManager(), "MonthDayPicker");
            }
        });

        //끝날짜 이벤트
        termEndText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MonthDayPicker picker = new MonthDayPicker();
                Bundle yearBundle = new Bundle();
                yearBundle.putString("year",titleText.getText().toString().substring(0, 4));
                picker.setArguments(yearBundle);
                picker.setListener(endListener);
                picker.show(getFragmentManager(), "MonthDayPicker");
            }
        });

        return view;
    }

    private void termSelect() {
        if(database != null) {
            sqlEx = "select expense_date, asset_name, expensecategory_name, amount, memo from expense where expense_date >= '" + startTerm + "' and expense_date <= '" + endTerm + "' order by expense_date";
            if (sqlEx != null) {
                Cursor cursor = database.rawQuery(sqlEx, null);
                while (cursor.moveToNext()) {
                    String exDate = cursor.getString(0);
                    String exAn = cursor.getString(1);
                    String exCt = cursor.getString(2);
                    String exAmount = cursor.getString(3);
                    String exMemo = cursor.getString(4);
                    if (!exMemo.equals("")) {
                        date.add(new DailyInAndOut(0, "지출", exDate, exAn, exCt, Integer.parseInt(exAmount), null));
                    } else {
                        date.add(new DailyInAndOut(0, "지출", exDate, exAn, exCt, Integer.parseInt(exAmount), exMemo));
                    }
                }
                cursor.close();
            }
            sqlIn = "select income_date, asset_name, incomecategory_name, amount, memo from income where income_date >= '" + startTerm + "' and income_date <= '" + endTerm + "' order by income_date";
            if (sqlIn != null) {
                Cursor cursor = database.rawQuery(sqlIn, null);
                while (cursor.moveToNext()) {
                    String inDate = cursor.getString(0);
                    String inAn = cursor.getString(1);
                    String inCt = cursor.getString(2);
                    String inAmount = cursor.getString(3);
                    String inMemo = cursor.getString(4);
                    if (!inMemo.equals("")) {
                        date.add(new DailyInAndOut(0, "수입", inDate, inAn, inCt, Integer.parseInt(inAmount), null));
                    } else {
                        date.add(new DailyInAndOut(0, "수입", inDate, inAn, inCt, Integer.parseInt(inAmount), inMemo));
                    }
                }
                cursor.close();
            }
            //해당하는 기간별 지출
            sumEx = "select sum(amount) from expense where expense_date >=  '" + startTerm + "' and expense_date <= '" + endTerm + "'";
            if(sumEx != null) {
                Cursor cursor = database.rawQuery(sumEx, null);
                while (cursor.moveToNext()) {
                    exSum = cursor.getString(0);
                    if(exSum != null){
                        monthExpense.setText(numberFormat.format(Integer.parseInt(exSum)) + " 원");
                    } else {
                        monthExpense.setText("0 원");
                    }
                }
                cursor.close();
            }
            //해당하는 기간별 수입
            sumIn = "select sum(amount) from income where income_date >=  '" + startTerm + "' and income_date <= '" + endTerm + "'";
            if(sumEx != null) {
                Cursor cursor = database.rawQuery(sumIn, null);
                while (cursor.moveToNext()) {
                    inSum = cursor.getString(0);
                    if(inSum != null){
                        monthIncome.setText(numberFormat.format(Integer.parseInt(inSum)) + " 원");
                    } else {
                        monthIncome.setText("0 원");
                    }
                }
                cursor.close();
            }
            //해당 기간별 전체
            if(inSum == null){
                inSum = "0";
            }
            if(exSum == null){
                exSum = "0";
            }
            int mountSumInt = Integer.parseInt(inSum) - Integer.parseInt(exSum);
            if(mountSumInt < 0){
                monthSum.setText(Html.fromHtml("<font color=\"#ff0000\">" + numberFormat.format(mountSumInt) + "</font>" + " 원"));
            } else if(mountSumInt == 0){
                monthSum.setText(numberFormat.format(mountSumInt) + " 원");
            } else if(mountSumInt > 0){
                monthSum.setText(Html.fromHtml("<font color=\"#0000ff\">" + numberFormat.format(mountSumInt) + "</font>" + " 원"));
            }

            //가계부 날짜에 따른 정렬
            Collections.sort(date, new Comparator<DailyInAndOut>() {
                @Override
                public int compare(DailyInAndOut o1, DailyInAndOut o2) {
                    return o1.getDate().compareTo(o2.getDate());
                }
            });
//            for(DailyInAndOut d : date){
//                Log.d("TAG", "termSelect: " + d + "\n");
//            }
            if(date.size() > 0){
                nodata.setVisibility(View.GONE);
                weekContent.setVisibility(View.VISIBLE);
                sumContent.setVisibility(View.VISIBLE);
                divider.setVisibility(View.VISIBLE);
                String dateck = date.get(0).getDate();
                    for (int i = 0; i < date.size(); i++) {
                        //초기값 셋팅
                        if(i == 0){
                            //날짜
                            tr1 = new TableRow(getActivity());
                            TableLayout.LayoutParams tl1 = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                            tl1.setMargins(15, 15, 15, 0);
                            tr1.setLayoutParams(tl1);
                            tr1.setBackgroundResource(R.color.colorAccent);
                            t1 = new TextView(getActivity());
                            t1.setText(date.get(0).getDate());
                            t1.setTextSize(18f);
                            t1.setGravity(Gravity.CENTER);
                            tr1.addView(t1, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 3));
                            tableLayout.addView(tr1);
                            //해당날짜의 합계
                            tr3 = new TableRow(getActivity());
                            TableLayout.LayoutParams tl3 = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                            tl3.setMargins(15, 0, 15, 0);
                            tr3.setLayoutParams(tl3);
                            tr3.setBackgroundResource(R.color.colorAccent);
                            t5 = new TextView(getActivity());
                            t6 = new TextView(getActivity());
                            t7 = new TextView(getActivity());
                            weekEx = "select sum(amount) from expense where expense_date =  '" + date.get(0).getDate() + "'";
                            if(weekEx != null) {
                                Cursor cursor = database.rawQuery(weekEx, null);
                                while (cursor.moveToNext()) {
                                    exWeek = cursor.getString(0);
                                    if(exWeek != null){
                                        t7.setText(numberFormat.format(Integer.parseInt(exWeek)) + " 원");
                                        t7.setGravity(Gravity.CENTER);
                                    } else {
                                        t7.setText("0 원");
                                        t7.setGravity(Gravity.CENTER);
                                    }
                                }
                                cursor.close();
                            }
                            weekIn = "select sum(amount) from income where income_date =  '" + date.get(0).getDate() + "'";
                            if(weekIn != null) {
                                Cursor cursor = database.rawQuery(weekIn, null);
                                while (cursor.moveToNext()) {
                                    inWeek = cursor.getString(0);
                                    if(inWeek != null){
                                        t5.setText(numberFormat.format(Integer.parseInt(inWeek)) + " 원");
                                        t5.setGravity(Gravity.CENTER);
                                    } else {
                                        t5.setText("0 원");
                                        t5.setGravity(Gravity.CENTER);
                                    }
                                }
                                cursor.close();
                            }
                            if(inWeek == null){
                                inWeek = "0";
                            }
                            if(exWeek == null){
                                exWeek = "0";
                            }
                            weekSum = Integer.parseInt(inWeek) - Integer.parseInt(exWeek);
                            if(weekSum < 0){
                                t6.setText(Html.fromHtml("<font color=\"#ff0000\">" + numberFormat.format(weekSum) + "</font>" + " 원"));
                                t6.setGravity(Gravity.CENTER);
                            } else if(weekSum == 0){
                                t6.setText(numberFormat.format(weekSum) + " 원");
                                t6.setGravity(Gravity.CENTER);
                            } else if(weekSum > 0){
                                t6.setText(Html.fromHtml("<font color=\"#0000ff\">" + numberFormat.format(weekSum) + "</font>" + " 원"));
                                t6.setGravity(Gravity.CENTER);
                            }
                            tr3.addView(t5, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1));
                            tr3.addView(t6, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1));
                            tr3.addView(t7, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1));
                            tableLayout.addView(tr3);
                        } else if(!(dateck.equals(date.get(i).getDate()))){//다를때는 날짜부터해서 새로 적어주기
                            //날짜
                            tr1 = new TableRow(getActivity());
                            TableLayout.LayoutParams tl1 = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                            tl1.setMargins(15, 20, 15, 0);
                            tr1.setLayoutParams(tl1);
                            tr1.setBackgroundResource(R.color.colorAccent);
                            t1 = new TextView(getActivity());
                            t1.setText(date.get(i).getDate());
                            t1.setTextSize(18f);
                            t1.setGravity(Gravity.CENTER);
                            tr1.addView(t1, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 3));
                            tableLayout.addView(tr1);
                            //해당날짜의 합계
                            tr3 = new TableRow(getActivity());
                            TableLayout.LayoutParams tl3 = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                            tl3.setMargins(15, 0, 15, 0);
                            tr3.setLayoutParams(tl3);
                            tr3.setBackgroundResource(R.color.colorAccent);
                            t5 = new TextView(getActivity());
                            t6 = new TextView(getActivity());
                            t7 = new TextView(getActivity());
                            weekEx = "select sum(amount) from expense where expense_date =  '" + date.get(i).getDate() + "'";
                            if(weekEx != null) {
                                Cursor cursor = database.rawQuery(weekEx, null);
                                while (cursor.moveToNext()) {
                                    exWeek = cursor.getString(0);
                                    if(exWeek != null){
                                        t7.setText(numberFormat.format(Integer.parseInt(exWeek)) + " 원");
                                        t7.setGravity(Gravity.CENTER);
                                    } else {
                                        t7.setText("0 원");
                                        t7.setGravity(Gravity.CENTER);
                                    }
                                }
                                cursor.close();
                            }
                            weekIn = "select sum(amount) from income where income_date =  '" + date.get(i).getDate() + "'";
                            if(weekIn != null) {
                                Cursor cursor = database.rawQuery(weekIn, null);
                                while (cursor.moveToNext()) {
                                    inWeek = cursor.getString(0);
                                    if(inWeek != null){
                                        t5.setText(numberFormat.format(Integer.parseInt(inWeek)) + " 원");
                                        t5.setGravity(Gravity.CENTER);
                                    } else {
                                        t5.setText("0 원");
                                        t5.setGravity(Gravity.CENTER);
                                    }
                                }
                                cursor.close();
                            }
                            if(inWeek == null){
                                inWeek = "0";
                            }
                            if(exWeek == null){
                                exWeek = "0";
                            }
                            weekSum = Integer.parseInt(inWeek) - Integer.parseInt(exWeek);
                            if(weekSum < 0){
                                t6.setText(Html.fromHtml("<font color=\"#ff0000\">" + numberFormat.format(weekSum) + "</font>" + " 원"));
                                t6.setGravity(Gravity.CENTER);
                            } else if(weekSum == 0){
                                t6.setText(numberFormat.format(weekSum) + " 원");
                                t6.setGravity(Gravity.CENTER);
                            } else if(weekSum > 0){
                                t6.setText(Html.fromHtml("<font color=\"#0000ff\">" + numberFormat.format(weekSum) + "</font>" + " 원"));
                                t6.setGravity(Gravity.CENTER);
                            }
                            tr3.addView(t5, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1));
                            tr3.addView(t6, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1));
                            tr3.addView(t7, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1));
                            tableLayout.addView(tr3);
                            dateck = date.get(i).getDate();
                        }
                        //상세내역
                        tr2 = new TableRow(getActivity());
                        TableLayout.LayoutParams tl2 = new TableLayout.LayoutParams(TableLayout.LayoutParams.WRAP_CONTENT, TableLayout.LayoutParams.WRAP_CONTENT);
                        if(date.get(date.size()-1) == date.get(i)){//제일 마지막만 bottom margin
                            tl2.setMargins(15, 0, 15, 20);
                            tr2.setLayoutParams(tl2);
                        } else {
                            tl2.setMargins(15, 0, 15, 0);
                            tr2.setLayoutParams(tl2);
                        }
                        tr2.setBackgroundColor(Color.parseColor("#FFF9C4"));
                        t2 = new TextView(getActivity());
                        t3 = new TextView(getActivity());
                        t4 = new TextView(getActivity());
                        t2.setText(date.get(i).getAssetName());
                        t2.setGravity(Gravity.CENTER);
                        if(date.get(i).getType().equals("수입")){
                            t3.setText(Html.fromHtml("<font color=\"#0000ff\">" + numberFormat.format(date.get(i).getAmount()) + "</font>" + " 원"));
                            t3.setGravity(Gravity.CENTER);
                        }
                        if(date.get(i).getType().equals("지출")){
                            t3.setText(Html.fromHtml("<font color=\"#ff0000\">" + numberFormat.format(date.get(i).getAmount())+ "</font>" + " 원"));
                            t3.setGravity(Gravity.CENTER);
                        }
                        t4.setText(date.get(i).getCategoryName());
                        t4.setGravity(Gravity.CENTER);
                        tr2.addView(t2, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1));
                        tr2.addView(t3, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1));
                        tr2.addView(t4, new TableRow.LayoutParams(0, TableLayout.LayoutParams.WRAP_CONTENT, 1));
                        tableLayout.addView(tr2);
                    }
                } else {
                nodata.setVisibility(View.VISIBLE);
                weekContent.setVisibility(View.GONE);
                sumContent.setVisibility(View.VISIBLE);
                divider.setVisibility(View.GONE);
            }
        }


    }

    //데이터 피커 기능
    DatePickerDialog.OnDateSetListener startListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            yearStr = titleText.getText().toString().substring(0, 4);
            termStartText.setText(month + "월 " + dayOfMonth + "일 ");
            monthStr = String.valueOf(month);
            dayStr = String.valueOf(dayOfMonth);
            if(monthStr.length() == 1){
                monthStr = "0" + monthStr;
            }
            if(dayStr.length() == 1){
                dayStr = "0" + dayStr;
            }

            startTerm = yearStr + "-" + monthStr + "-" + dayStr;
            if(endTerm != null){
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    startDate = f.parse(startTerm);
                    endDate = f.parse(endTerm);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(startDate.compareTo(endDate) > 0){
                    Toast.makeText(getContext(), "시작날짜보다 뒷 날짜로 선택하세요", Toast.LENGTH_SHORT).show();
                }
                date.clear();
                tableLayout.removeAllViews();
                termSelect();
            }
        }
    };

    DatePickerDialog.OnDateSetListener endListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            yearEndStr = titleText.getText().toString().substring(0, 4);
            termEndText.setText(month + "월 " + dayOfMonth + "일 ");
            monthEndStr = String.valueOf(month);
            dayEndStr = String.valueOf(dayOfMonth);
            if(monthEndStr.length() == 1){
                monthEndStr = "0" + monthEndStr;
            }
            if(dayEndStr.length() == 1){
                dayEndStr = "0" + dayEndStr;
            }
            endTerm = yearEndStr + "-" + monthEndStr + "-" + dayEndStr;
            if(startTerm != null){
                SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    startDate = f.parse(startTerm);
                    endDate = f.parse(endTerm);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if(startDate.compareTo(endDate) > 0){
                    Toast.makeText(getContext(), "시작날짜보다 뒷 날짜로 선택하세요", Toast.LENGTH_SHORT).show();
                }
                date.clear();
                tableLayout.removeAllViews();
                termSelect();
            }
        }
    };
}