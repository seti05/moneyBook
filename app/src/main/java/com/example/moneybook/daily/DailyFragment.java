package com.example.moneybook.daily;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.moneybook.DatabaseHelper;
import com.example.moneybook.MainActivity;
import com.example.moneybook.R;
import com.example.moneybook.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
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
    ArrayList<DailyInAndOut> outList,inList;

    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;

    ScrollView scrollView;
    LinearLayout testlinearlayout;

    NumberFormat numberFormat;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    public DailyFragment() { }
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_daily, container, false);

        titleTextView = view.findViewById(R.id.titleText);
        numberFormat = NumberFormat.getInstance(Locale.getDefault());

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
                setCardView();
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onSwipeLeft() {
                Log.d("TAG", "onSwipeLeft: 다음날");
                String selectDayStr = titleTextView.getText().toString();
                LocalDate selectDay = LocalDate.parse(selectDayStr);
                titleTextView.setText(selectDay.plusDays(1).toString());
                setSevenDays();
                setCardView();
            }
        });//뷰.setOnTouchListener끝

        //데이터보여줄 함수들
        outList = new ArrayList<>();
        inList = new ArrayList<>();

        dbHelper = new DatabaseHelper(getContext());
        database = dbHelper.getWritableDatabase();

        incomeT= view.findViewById(R.id.textView13);
        totalT = view.findViewById(R.id.textView14);
        expenseT = view.findViewById(R.id.textView15);



        //스크롤뷰안쪽 레이아웃
        testlinearlayout = view.findViewById(R.id.testlinearlayout);
        setCardView();

        //받아온 데이터 지워주기
        getArguments().clear();

        //데이터 보여줄 스크롤뷰
        scrollView = view.findViewById(R.id.scrollview);
        return view;
    }//온크리에이트뷰 끝


    DailyInAndOut dailyInAndOut;
    private void setCardView() {
        testlinearlayout.removeAllViews();
        //Log.d("TAG", "셀렉프레그먼트에서 실행한 setCardView");
        //하루내역보여주기
        inList.clear();
        outList.clear();
        String[] expense = new String[]{"expense_date","asset_name","expensecategory_name","amount","memo"};
        String[] income = new String[]{"income_date","asset_name","incomecategory_name","amount","memo"};
        String selectDayStr = titleTextView.getText().toString();
        //Log.d("카드뷰세팅", "날짜: "+selectDayStr);
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
            dailyInAndOut = new DailyInAndOut(ex_id,"지출",date,asset,category,amount,memo);
            setCardTextView(category,asset,amount,memo,dailyInAndOut);
            outList.add(dailyInAndOut);
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
            dailyInAndOut = new DailyInAndOut(in_id,"수입",date,asset,category,amount,memo);
            setCardTextView(category,asset,amount,memo,dailyInAndOut);
            inList.add(dailyInAndOut);
        }
        cursor.close();
        //하루 총합구하기
        int incomeTotal=0;
        int expenseTotal=0;
        int dayTotal=0;
        for (DailyInAndOut ex: outList ) {
            //adapter.addItem(ex);
            expenseTotal+=ex.getAmount();
        }
        for (DailyInAndOut in: inList ) {
            // adapter.addItem(in);
            incomeTotal+=in.getAmount();
        }
        //하루 전체 내역
        dayTotal=incomeTotal-expenseTotal;
        incomeT.setText(numberFormat.format(incomeTotal)+" 원");
        expenseT.setText(numberFormat.format(expenseTotal)+" 원");

        if (dayTotal>0){
            totalT.setText(Html.fromHtml("<font color=\"#2196F3\">"
                    +numberFormat.format(dayTotal) +"</font>"
                    + "원"));
        }else if (dayTotal==0){
            totalT.setText(dayTotal+" 원");
        }else if (dayTotal<0){
            totalT.setText(Html.fromHtml("<font color=\"#ff0000\">"
                    +numberFormat.format(dayTotal) +"</font>"
                    + "원"));
        }

    }//스크롤뷰안쪽 리니어레이아웃에 카드뷰넣기

    private void setCardTextView(String category, String asset, int amount, String memo, final DailyInAndOut dailyInAndOut) {
        CardView cardView = new CardView(getContext());
        cardView.setCardElevation(5);
        LinearLayout innercardviewLinear= new LinearLayout(getContext());
        innercardviewLinear.setOrientation(LinearLayout.VERTICAL);
        TextView categoryT,assetT,memoT,amountT;
        categoryT= new TextView(getContext());
        categoryT.setText(category);
        assetT= new TextView(getContext());
        assetT.setText(asset);
        amountT= new TextView(getContext());
        amountT.setText(numberFormat.format(amount)+"원");
        memoT= new TextView(getContext());
        memoT.setText(memo);
        innercardviewLinear.addView(categoryT);
        innercardviewLinear.addView(assetT);
        innercardviewLinear.addView(amountT);
        innercardviewLinear.addView(memoT);
        cardView.addView(innercardviewLinear);
        testlinearlayout.addView(cardView);
        //카드뷰에 터치이벤트걸기
        cardView.setOnTouchListener(new OnSwipeTouchListener(getContext()){
            @SuppressLint("ClickableViewAccessibility")
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onSwipeRight() {
                //Log.d("스크롤뷰", "onSwipeRight: 전날");
                String selectDayStr = titleTextView.getText().toString();
                LocalDate selectDay = LocalDate.parse(selectDayStr);
                titleTextView.setText(selectDay.minusDays(1).toString());
                setSevenDays();
                setCardView();
            }
            @SuppressLint("ClickableViewAccessibility")
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onSwipeLeft() {
                //Log.d("스크롤뷰", "onSwipeLeft: 다음날");
                String selectDayStr = titleTextView.getText().toString();
                LocalDate selectDay = LocalDate.parse(selectDayStr);
                titleTextView.setText(selectDay.plusDays(1).toString());
                setSevenDays();
                setCardView();
            }
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick(View v) {
                Log.d("스크롤뷰클릭이벤트 제발", "onClick: "+dailyInAndOut.toString());
                Intent intent = new Intent(getContext(), UpdateMoneyBookActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("contents",dailyInAndOut);
                getContext().startActivity(intent);
            }
        });
    }//카드뷰 안쪽 텍스트뷰 만들고 리니어레이아웃에 뷰추가


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.month_main,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Date current = Calendar.getInstance().getTime();
        String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(current);
        if(home == item.getItemId()){
            titleTextView.setText(date);
            setSevenDays();
            setCardView();
            return true;
        }else if(R.id.tab5 == item.getItemId()){
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
            setCardView();
        }
    };

    //7날짜 보여주기
    @RequiresApi(api = Build.VERSION_CODES.O)
    public void setSevenDays(){
        String selectDayStr = titleTextView.getText().toString();
        LocalDate selecday = LocalDate.parse(selectDayStr);
        before3.setTextColor(Color.BLACK);
        before2.setTextColor(Color.BLACK);
        before1.setTextColor(Color.BLACK);
        select.setTextColor(Color.BLACK);
        next1.setTextColor(Color.BLACK);
        next2.setTextColor(Color.BLACK);
        next3.setTextColor(Color.BLACK);
        before3.setText(setWeekdayStr(selecday.minusDays(3).getDayOfWeek().toString())+"\n"+selecday.minusDays(3).getDayOfMonth()+"");
        if (selecday.minusDays(3).getDayOfWeek().toString().equals("SATURDAY")){
            before3.setTextColor(Color.BLUE);
        }else if (selecday.minusDays(3).getDayOfWeek().toString().equals("SUNDAY")){
            before3.setTextColor(Color.RED);
        }
        before2.setText(setWeekdayStr(selecday.minusDays(2).getDayOfWeek().toString())+"\n"+selecday.minusDays(2).getDayOfMonth()+"");
        if (selecday.minusDays(2).getDayOfWeek().toString().equals("SATURDAY")){
            before2.setTextColor(Color.BLUE);
        }else if (selecday.minusDays(2).getDayOfWeek().toString().equals("SUNDAY")){
            before2.setTextColor(Color.RED);
        }
        before1.setText(setWeekdayStr(selecday.minusDays(1).getDayOfWeek().toString())+"\n"+selecday.minusDays(1).getDayOfMonth()+"");
        if (selecday.minusDays(1).getDayOfWeek().toString().equals("SATURDAY")){
            before1.setTextColor(Color.BLUE);
        }else if (selecday.minusDays(1).getDayOfWeek().toString().equals("SUNDAY")){
            before1.setTextColor(Color.RED);
        }
        select.setText(Html.fromHtml(setWeekdayStr(selecday.getDayOfWeek().toString())+"<br>"
                +"<span style=\"background-color:#1cbaba;\"><u>"+selecday.getDayOfMonth() +"</span></u>"));
        if (selecday.getDayOfWeek().toString().equals("SATURDAY")){
            select.setTextColor(Color.BLUE);
        }else if (selecday.getDayOfWeek().toString().equals("SUNDAY")){
            select.setTextColor(Color.RED);
        }
        next1.setText(setWeekdayStr(selecday.plusDays(1).getDayOfWeek().toString())+"\n"+selecday.plusDays(1).getDayOfMonth()+"");
        if (selecday.plusDays(1).getDayOfWeek().toString().equals("SATURDAY")){
            next1.setTextColor(Color.BLUE);
        }else if (selecday.plusDays(1).getDayOfWeek().toString().equals("SUNDAY")){
            next1.setTextColor(Color.RED);
        }
        next2.setText(setWeekdayStr(selecday.plusDays(2).getDayOfWeek().toString())+"\n"+selecday.plusDays(2).getDayOfMonth()+"");
        if (selecday.plusDays(2).getDayOfWeek().toString().equals("SATURDAY")){
            next2.setTextColor(Color.BLUE);
        }else if (selecday.plusDays(2).getDayOfWeek().toString().equals("SUNDAY")){
            next2.setTextColor(Color.RED);
        }
        next3.setText(setWeekdayStr(selecday.plusDays(3).getDayOfWeek().toString())+"\n"+selecday.plusDays(3).getDayOfMonth()+"");
        if (selecday.plusDays(3).getDayOfWeek().toString().equals("SATURDAY")){
            next3.setTextColor(Color.BLUE);
        }else if (selecday.plusDays(3).getDayOfWeek().toString().equals("SUNDAY")){
            next3.setTextColor(Color.RED);
        }
    }

    public String setWeekdayStr(String str){
        String dayofweek="";
        switch (str){
            case "MONDAY":dayofweek="월"; break;
            case "TUESDAY":dayofweek= "화"; break;
            case "WEDNESDAY":dayofweek= "수"; break;
            case "THURSDAY":dayofweek= "목"; break;
            case "FRIDAY":dayofweek= "금"; break;
            case "SATURDAY":dayofweek= "토"; break;
            case "SUNDAY":dayofweek= "일"; break;

        }
        return dayofweek;
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
                setCardView();
            }
        });
        before2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.minusDays(2).toString());
                setSevenDays();
                setCardView();
            }
        });
        before1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.minusDays(1).toString());
                setSevenDays();
                setCardView();

            }
        });
        select.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.toString());
                setSevenDays();
                setCardView();

            }
        });
        next1.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.plusDays(1).toString());
                setSevenDays();
                setCardView();

            }
        });
        next2.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.plusDays(2).toString());
                setSevenDays();
                setCardView();
            }
        });
        next3.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onClick(View v) {
                thisday = LocalDate.parse(titleTextView.getText().toString());
                titleTextView.setText(thisday.plusDays(3).toString());
                setSevenDays();
                setCardView();
            }
        });

    }

}