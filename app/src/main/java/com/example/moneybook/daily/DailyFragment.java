package com.example.moneybook.daily;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import com.example.moneybook.DatabaseHelper;
import com.example.moneybook.MainActivity;
import com.example.moneybook.R;
import com.example.moneybook.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.R.id.home;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DailyFragment extends Fragment {
    TextView before3,before2,before1,select,next1,next2,next3;
    TextView titleTextView;
    TextView incomeT,totalT,expenseT;

    Toolbar toolbar;
    LocalDate today =LocalDate.now();
    String titleStr="";
    RecyclerView daily_recycler_View;
    RecyclerView recyclerView;
    ArrayList<DailyInAndOut> outList,inList;
    DailyAdapter adapter;

    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public DailyFragment() { }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_daily, container, false);

        titleTextView = view.findViewById(R.id.titleText);

        //상단바
        toolbar = view.findViewById(R.id.toolbar);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);//원래 상단바의 이름을 감춤
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeAsUpIndicator(R.drawable.today);

        Date current = Calendar.getInstance().getTime();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(current);
        String deliverdDate=getArguments().getString("selectDay");
        if(deliverdDate!= null && deliverdDate !=""){
            titleTextView.setText(deliverdDate);
        }else {
            titleTextView.setText(date);
        }
        titleStr= titleTextView.getText().toString();

        //상단바가운데 클릭시
        titleTextView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                Log.d("TAG", "셀렉프레그먼크 클릭이벤트");
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), listener, today.getYear(), today.getMonthValue()-1, today.getDayOfMonth());
                datePickerDialog.getDatePicker().setCalendarViewShown(false);
                datePickerDialog.show();
            }
        });

        //입력+버튼
        FloatingActionButton fab = view.findViewById(R.id.reg_fabButton);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(),RegMoneyBookActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("regDate",titleTextView.getText().toString());
                startActivity(intent);
            }
        });

        //7개 날짜 나오게
        before3 = view.findViewById(R.id.textView4);
        before2 = view.findViewById(R.id.textView5);
        before1 = view.findViewById(R.id.textView6);
        select = view.findViewById(R.id.textView7);
        next1 = view.findViewById(R.id.textView8);
        next2 = view.findViewById(R.id.textView9);
        next3 = view.findViewById(R.id.textView10);
        setSevenDays();
        //7일 클릭시 클릭한 데이터나오게
        dayclickEvent();

        view.setOnTouchListener(new OnSwipeTouchListener(getContext()){
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onSwipeRight() {
                Log.d("TAG", "onSwipeRight: 전날");
                String selectDayStr = titleTextView.getText().toString();
                LocalDate selectDay = LocalDate.parse(selectDayStr);
                titleTextView.setText(selectDay.minusDays(1).toString());
                setSevenDays();
                showDailyResult();
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onSwipeLeft() {
                Log.d("TAG", "onSwipeLeft: 다음날");
                String selectDayStr = titleTextView.getText().toString();
                LocalDate selectDay = LocalDate.parse(selectDayStr);
                titleTextView.setText(selectDay.plusDays(1).toString());
                setSevenDays();
                showDailyResult();
            }
        });//뷰.setOnTouchListener끝

        //데이터보여줄 함수들
        outList = new ArrayList<>();
        inList = new ArrayList<>();

        daily_recycler_View = view.findViewById(R.id.daily_recycler_View);
        adapter = new DailyAdapter();

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext(),LinearLayoutManager.VERTICAL,false);
        daily_recycler_View.setLayoutManager(layoutManager);
        daily_recycler_View.setAdapter(adapter);
        daily_recycler_View.setHasFixedSize(true);

        dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();

        incomeT= view.findViewById(R.id.textView13);
        totalT = view.findViewById(R.id.textView14);
        expenseT = view.findViewById(R.id.textView15);

        showDailyResult();
        return view;
    }//온크리에이트뷰 끝


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.month_main,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        //Log.d("TAG", "셀렉프레그먼트에서 실행한 오늘클릭: ");
        Date current = Calendar.getInstance().getTime();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(current);
        //Log.d("TAG", "date:"+date);
        if(home == item.getItemId()){
            titleTextView.setText(date);
            setSevenDays();
            showDailyResult();
            return true;
        }else if(R.id.tab5 == item.getItemId()){
            //Toast.makeText(this, "설정 눌렀지" , Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //헤드 가운데 날짜 선택했을때
    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @RequiresApi(api = Build.VERSION_CODES.O)
        @Override
        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            String day = String.valueOf(dayOfMonth);
            String month = String.valueOf(monthOfYear+1);
            if(day.length()==1){
                day="0"+day;
            }
            if(month.length()==1){
                month="0"+month;
            }
            titleTextView.setText(year + "-" + (month) + "-"+ day);
            setSevenDays();
            showDailyResult();
//            onFragementChanged(2);
        }
    };

    //7날짜 보여주기
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setSevenDays(){
        String selectDayStr = titleTextView.getText().toString();
        LocalDate selecday = LocalDate.parse(selectDayStr);
        before3.setText(selecday.minusDays(3).getDayOfMonth()+"");
        before2.setText(selecday.minusDays(2).getDayOfMonth()+"");
        before1.setText(selecday.minusDays(1).getDayOfMonth()+"");
        select.setText(selecday.getDayOfMonth()+"");
        next1.setText(selecday.plusDays(1).getDayOfMonth()+"");
        next2.setText(selecday.plusDays(2).getDayOfMonth()+"");
        next3.setText(selecday.plusDays(3).getDayOfMonth()+"");
    }

    LocalDate thisday;
    //7개 날짜 클릭이벤트
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void dayclickEvent() {
        before3.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.minusDays(3).toString());
                setSevenDays();
                showDailyResult();
            }
        });
        before2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.minusDays(2).toString());
                setSevenDays();
                showDailyResult();
            }
        });
        before1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.minusDays(1).toString());
                setSevenDays();
                showDailyResult();

            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.toString());
                setSevenDays();
                showDailyResult();

            }
        });
        next1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.plusDays(1).toString());
                setSevenDays();
                showDailyResult();

            }
        });
        next2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.plusDays(2).toString());
                setSevenDays();
                showDailyResult();
            }
        });
        next3.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.plusDays(3).toString());
                setSevenDays();
                showDailyResult();
            }
        });

    }

    //리사이클뷰에 데이터입력하기
    private void showDailyResult() {
        Log.d("TAG", "셀렉프레그먼트에서 실행한 showDailyResult");
        //하루내역보여주기
        inList.clear();
        outList.clear();
        String[] expense = new String[]{"expense_date","asset_name","expensecategory_name","amount","memo"};
        String[] income = new String[]{"income_date","asset_name","incomecategory_name","amount","memo"};
        String selectDayStr = titleTextView.getText().toString();
        //지출 넣기
        cursor = database.rawQuery("select expense_id,expense_date,asset_name,expensecategory_name,amount,memo"+
                " from expense where expense_date=?",new String[]{selectDayStr});
        while(cursor.moveToNext()){
            int ex_id= cursor.getInt(0);
            String date =cursor.getString(cursor.getColumnIndex(expense[0]));
            String asset = cursor.getString(cursor.getColumnIndex(expense[1]));
            String category= cursor.getString(cursor.getColumnIndex(expense[2]));
            int amount=cursor.getInt(cursor.getColumnIndex(expense[3]));
            String memo =cursor.getString(cursor.getColumnIndex(expense[4]));
            DailyInAndOut d = new DailyInAndOut(ex_id,"지출",date,asset,category,amount,memo);
            outList.add(d);
        }
        //수입넣기
        cursor = database.rawQuery("select income_id,income_date,asset_name,incomecategory_name,amount,memo"+
                " from income where income_date=?",new String[]{selectDayStr});
        while(cursor.moveToNext()){
            int in_id= cursor.getInt(0);
            String date =cursor.getString(cursor.getColumnIndex(income[0]));
            String asset = cursor.getString(cursor.getColumnIndex(income[1]));
            String category= cursor.getString(cursor.getColumnIndex(income[2]));
            int amount=cursor.getInt(cursor.getColumnIndex(income[3]));
            String memo =cursor.getString(cursor.getColumnIndex(income[4]));
            inList.add(new DailyInAndOut(in_id,"수입",date,asset,category,amount,memo));
        }
        cursor.close();
        adapter.clear();
        //Toast.makeText(getContext(),adapter.getItemCount()+"개있음",Toast.LENGTH_SHORT).show();

        int incomeTotal=0;
        int expenseTotal=0;
        int dayTotal=0;
        for (DailyInAndOut ex: outList ) {
            adapter.addItem(ex);
            expenseTotal+=ex.getAmount();
        }
        for (DailyInAndOut in: inList ) {
            adapter.addItem(in);
            incomeTotal+=in.getAmount();
        }
        for (DailyInAndOut test:adapter.getList()) {
            Log.d("moneybook", test.getDate()+" "+test.getAmount()+"원"+test.getMemo());
        }
        adapter.notifyDataSetChanged();
        //하루 전체 내역
        dayTotal=incomeTotal-expenseTotal;
        incomeT.setText(incomeTotal+" 원");
        expenseT.setText(expenseTotal+" 원");

        if (dayTotal>0){
            totalT.setText(Html.fromHtml("<font color=\"#2196F3\">"
                    +dayTotal +"</font>"
                    + "원"));
        }else if (dayTotal==0){
            totalT.setText(dayTotal+" 원");
        }else if (dayTotal<0){
            totalT.setText(Html.fromHtml("<font color=\"#ff0000\">"
                    +dayTotal +"</font>"
                    + "원"));
        }
    }

}