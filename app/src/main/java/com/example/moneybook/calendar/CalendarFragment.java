package com.example.moneybook.calendar;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneybook.DatabaseHelper;
import com.example.moneybook.R;
import com.example.moneybook.SettingsActivity;
import com.example.moneybook.daily.DailyInAndOut;
import com.example.moneybook.daily.RegMoneyBookActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static android.R.id.home;

public class CalendarFragment extends Fragment {

    TextView titleText;
    GridView gridView, gridWeek;
    RecyclerView recyclerView;
    Toolbar toolbar;
    Date date;
    String yearStr, monthStr, dayStr;
    ArrayList<String> weekList, dayList;
    Calendar calendar;
    int dayNum;
    GridAdapter gridAdapter, weekAdapter;
    int daycheck;
    String selectym="";
    String dateSql;
    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    DatePickerDialog datePickerDialog;
    CalendarAdapter adapter;
    String day;
    NumberFormat numberFormat;


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_calendar, container, false);

        setHasOptionsMenu(true);
        titleText = view.findViewById(R.id.titleText);
        gridView = view.findViewById(R.id.gridview);
        gridWeek = view.findViewById(R.id.gridWeek);
        recyclerView = view.findViewById(R.id.recyclerView);
        toolbar = view.findViewById(R.id.toolbar);
        numberFormat = NumberFormat.getInstance(Locale.getDefault());

        //상단바 설정(오늘버튼)
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        activity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.today);

        dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();

        Log.d("TAG", "onCreateView: " + dateSql);
        //오늘 년, 월 설정
        today();

        click();

        //타이틀바 클릭시(데이터피커)
        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YearMonthPicker picker = new YearMonthPicker();
                picker.setListener(listener);
                picker.show(getFragmentManager(), "YearMonthPicker");
            }
        });

        //+버튼클릭시 추가페이지로 넘어가는 버튼 이벤트
        final FloatingActionButton fab = view.findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("TAG", "onClick: ");
                Intent intent = new Intent(getContext(), RegMoneyBookActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("regDate", day);//그리드뷰 클릭시 그 데이터
                startActivity(intent);
            }
        });

        //상세내역에서 스크롤시 +버튼 사라지고 나타나고 이벤트
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if(dy>0){
                    fab.hide();
                } else {
                    fab.show();
                }
            }
        });

        //제일 처음 년, 월에 맞게 캘린더 설정
        yearStr = titleText.getText().toString().substring(0,4);
        monthStr = titleText.getText().toString().substring(6,8);
        dateSql = yearStr + "-" + monthStr + "-";
        calendar();

        adapter = new CalendarAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        //gridview클릭시 상세내역 볼 수 있음(현재 날짜로)
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dayStr = String.valueOf(position+2-dayNum);
                if(dayStr.length() == 1){
                    dayStr = "0" + dayStr;
                }
                day = yearStr + "-" + monthStr + "-" + dayStr;
                adapter.clear();
                select();
            }
        });

        Log.d("TAG", "onCreateView: " + yearStr + monthStr);
        return view;
    }

    private void click(){
        Bundle bundle = getArguments();
        if(bundle != null){
            String monthBC = bundle.getString("month");
            String yearBarChart = bundle.getString("year");
            Log.d("TAG", "m: " + monthBC + ", y" + yearBarChart);
            if(monthBC != null && yearBarChart != null) {
                dateSql = yearBarChart + "-" + monthBC + "-";
                titleText.setText(yearBarChart + "년 " + monthBC + "월");
                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        dayStr = String.valueOf(position + 2 - dayNum);
                        if (dayStr.length() == 1) {
                            dayStr = "0" + dayStr;
                        }
                        Log.d("TAG", "dateSql: " + dateSql);
                        day = dateSql + dayStr;
                        adapter.clear();
                    }
                });
            }
            bundle.clear();
        }
    }

    //상단바활성화
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.month_main, menu);
    }

    //현재오늘 년월을 설정함함
    private void today() {
        Log.d("TAG", "today: ");
        //현재 년월 지정
        long now = System.currentTimeMillis();
        date = new Date(now);
        final SimpleDateFormat curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
        final SimpleDateFormat curMonthFormat = new SimpleDateFormat("MM", Locale.KOREA);
        titleText.setText(curYearFormat.format(date) + "년 " + curMonthFormat.format(date) + "월");
    }

    //상세내역보는 것
    private void select() {
        if(database != null){
            Log.d("TAG", "상세내역: " + day);
            String exSql = "select expensecategory_name, amount, memo, expense_date from expense where expense_date = '"+day+"'";
            String inSql = "select incomecategory_name, amount, memo, income_date from income where income_date = '" + day + "'";

            Cursor cursorEx = database.rawQuery(exSql, null);
            Cursor cursorIn = database.rawQuery(inSql, null);

            //수입내용
            while (cursorIn.moveToNext()){
                String incomecategory_name = cursorIn.getString(0);
                int amount = cursorIn.getInt(1);
                String memo = cursorIn.getString(2);
                String income_date = cursorIn.getString(3);
                if(memo==null || memo.equals("")){
                    DailyInAndOut d = new DailyInAndOut(0, null, income_date, null, incomecategory_name, amount, null);
                    adapter.addItem(d);
                } else {
                    DailyInAndOut d = new DailyInAndOut(0, null, income_date, null, incomecategory_name, amount, memo);
                    adapter.addItem(d);
                }
            }
            adapter.notifyDataSetChanged();
            cursorIn.close();

            //지출내용
            while (cursorEx.moveToNext()){
                String expensecategory_name = cursorEx.getString(0);
                int amount = cursorEx.getInt(1);
                String memo = cursorEx.getString(2);
                String expense_date = cursorEx.getString(3);
                if(memo==null || memo.equals("")){
                    DailyInAndOut d = new DailyInAndOut(0, null, expense_date, null, expensecategory_name, amount, null);
                    adapter.addItem(d);
                } else {
                    DailyInAndOut d = new DailyInAndOut(0, null, expense_date, null, expensecategory_name, amount, memo);
                    adapter.addItem(d);
                }

            }
            adapter.notifyDataSetChanged();
            cursorEx.close();

        }
    }

    //데이터 피커 기능
    DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            adapter.clear();
            adapter.notifyDataSetChanged();
            yearStr = String.valueOf(year);
            monthStr = String.valueOf(monthOfYear);
            dayStr = String.valueOf(dayOfMonth);
            //월과 일 길이 계산
            if(monthStr.length() == 1){
                monthStr = "0" + monthStr;
            }
            if(dayStr.length() == 1){
                dayStr = "0" + dayStr;
            }
            titleText.setText(yearStr + "년 " + monthStr + "월");
            calendar();
            datePickerDialog = new DatePickerDialog(getContext(), listener, year, monthOfYear, dayOfMonth);
            datePickerDialog.getDatePicker().setCalendarViewShown(false);
            dateSql = yearStr + "-" + monthStr + "-";
        }
    };

    //캘린더 제작
    private void calendar(){
        weekList = new ArrayList<String>();
        weekList.add("일");
        weekList.add("월");
        weekList.add("화");
        weekList.add("수");
        weekList.add("목");
        weekList.add("금");
        weekList.add("토");

        dayList = new ArrayList<String>();
        calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, Integer.parseInt(yearStr));
        calendar.set(Calendar.MONTH, Integer.parseInt(String.valueOf(Integer.parseInt(monthStr)-1)));
        calendar.set(Calendar.DATE, 1);
        dayNum = calendar.get(Calendar.DAY_OF_WEEK);
        for(int i = 1; i < dayNum; i++){
            dayList.add("");
        }
        setCalendarDate(calendar.get(Calendar.MONTH)+1);
        gridAdapter = new GridAdapter(getContext(), dayList);
        weekAdapter = new GridAdapter(getContext(), weekList);
        gridView.setAdapter(gridAdapter);
        gridWeek.setAdapter(weekAdapter);
    }

    private void setCalendarDate(int month) {
        calendar.set(Calendar.MONTH, month - 1);
        for(int i = 0; i < calendar.getActualMaximum(Calendar.DAY_OF_MONTH); i++){
            dayList.add("" + (i + 1));
        }
    }

    //달력에 들어갈 내용
    private class GridAdapter extends BaseAdapter {

        private final List<String> list;
        private final LayoutInflater inflater;

        private  GridAdapter(Context context, List<String> list){
            this.list = list;
            this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;

            if(convertView == null){
                convertView = inflater.inflate(R.layout.item_calendar_gridview, parent, false);
                holder = new ViewHolder();
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder)convertView.getTag();
            }
            holder.tvItemGridView = convertView.findViewById(R.id.tv_item_gridview);
            holder.incomeText = convertView.findViewById(R.id.incomeText);
            holder.expenseText = convertView.findViewById(R.id.expenseText);

            holder.tvItemGridView.setText("" + getItem(position));

            calendar = Calendar.getInstance();
            String dayStr = "0";
            String monthStr = String.valueOf(calendar.get(Calendar.MONTH) + 1);
            if(monthStr.length() == 1){
                monthStr = "0" + monthStr;
            }
            String now = calendar.get(Calendar.YEAR) + "-" + monthStr + "-";
            daycheck = position - dayNum + 2;
            if(daycheck<10){
                dayStr=dayStr+daycheck;
            }else {
                dayStr=String.valueOf(daycheck);
            }
            if(dateSql == null){
                selectym = now+dayStr;
                Log.d("TAG", "sql: "  + selectym);
            } else {
                selectym = dateSql + dayStr;
            }

            //지출의 합계
            String exSql = "select sum(amount) from expense where expense_date = '" + selectym + "'";
            if(exSql != null) {
                Cursor cursor = database.rawQuery(exSql, null);
                while (cursor.moveToNext()) {
                    String amount = cursor.getString(0);
                    if (amount != null) {
                        holder.expenseText.setText(numberFormat.format(Integer.parseInt(amount)) + "\n");
                    } else {
                        holder.expenseText.setText("");
                    }
                }
            }

            //수입의 합계
            String inSql = "select sum(amount) from income where income_date = '" + selectym + "'";
            if(inSql != null){
                Cursor cursor = database.rawQuery(inSql, null);
                while (cursor.moveToNext()) {
                    String amount = cursor.getString(0);
                    if(amount != null) {
                        holder.incomeText.setText(numberFormat.format(Integer.parseInt(amount)) + "\n");
                    } else {
                        holder.incomeText.setText("");
                    }
                }
            }

            //오늘날짜에만 색칠
            calendar = Calendar.getInstance();
            String sToday = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
            String sMonth = String.valueOf(calendar.get(Calendar.MONTH) + 1);
            if(sMonth.length() == 1){
                sMonth = "0" + sMonth;
            }
            String sYear = String.valueOf(calendar.get(Calendar.YEAR));
            String cmy = titleText.getText().toString();
            String year = cmy.substring(0, 4);
            String month = cmy.substring(6, 8);

            if(sToday.equals(getItem(position)) && sMonth.equals(month) && sYear.equals(year)){
                holder.tvItemGridView.setTextColor(Color.parseColor(String.valueOf("#FB8989")));
                holder.tvItemGridView.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
            }

            return convertView;
        }
    }

    private class ViewHolder{
        TextView tvItemGridView;
        TextView incomeText;
        TextView expenseText;
    }

    //타이틀바에 있는 버튼 클릭시
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(home == item.getItemId()){
            adapter.clear();
            adapter.notifyDataSetChanged();
            today();
            yearStr = titleText.getText().toString().substring(0,4);
            monthStr = titleText.getText().toString().substring(6,8);
            dateSql = yearStr + "-" + monthStr + "-";//실제 view에 보이도록 하기 위해서 view의 변수를 넣어줌
            calendar();
            return true;
        } else if(R.id.tab5 == item.getItemId()){
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return true;
    }
}