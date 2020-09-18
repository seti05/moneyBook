package com.example.moneybook.daily;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.text.Html;
import android.util.Log;
import android.view.Gravity;
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
import android.widget.Toast;

import com.example.moneybook.DatabaseHelper;
import com.example.moneybook.MainActivity;
import com.example.moneybook.R;
import com.example.moneybook.SettingsActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;

import static android.R.id.home;

@RequiresApi(api = Build.VERSION_CODES.O)
public class DailyFragment extends Fragment {
    TextView before3,before2,before1,select,next1,next2,next3;
    TextView titleTextView,todayTextView;
    TextView incomeT,totalT,expenseT;

    Toolbar toolbar;
    LocalDate today =LocalDate.now();
    String titleStr="";
    ArrayList<DailyInAndOut> outList,inList;
    ArrayList<DailyInAndOut> alldayList=new ArrayList<>();

    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;

    ScrollView scrollView;
    LinearLayout testlinearlayout;

    NumberFormat numberFormat;

    FloatingActionButton fab;
    Handler handler = new Handler();



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
        todayTextView=view.findViewById(R.id.todaytextview);

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
                DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), listener, today.getYear(), today.getMonthValue()-1, today.getDayOfMonth());
                datePickerDialog.getDatePicker().setCalendarViewShown(false);
                datePickerDialog.show();
            }
        });

        todayTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Date current = Calendar.getInstance().getTime();
                String date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(current);
                titleTextView.setText(date);
                setSevenDays();
                setCardView();
            }
        });

        //입력+버튼
        fab = view.findViewById(R.id.reg_fabButton);
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
                String selectDayStr = titleTextView.getText().toString();
                LocalDate selectDay = LocalDate.parse(selectDayStr);
                titleTextView.setText(selectDay.minusDays(1).toString());
                setSevenDays();
                setCardView();
            }
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onSwipeLeft() {
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
        //하루내역보여주기
        inList.clear();
        outList.clear();
        alldayList.clear();
        String[] expense = new String[]{"expense_date","asset_name","expensecategory_name","amount","memo"};
        String[] income = new String[]{"income_date","asset_name","incomecategory_name","amount","memo"};
        String selectDayStr = titleTextView.getText().toString();
        //지출 넣기
        cursor = database.rawQuery("select expense_id,expense_date,asset_name,expensecategory_name,amount,reg_date_time,memo"+
                " from expense where expense_date=?",new String[]{selectDayStr});
        while(cursor.moveToNext()){
            int ex_id= cursor.getInt(0);
            String date =cursor.getString(cursor.getColumnIndex(expense[0]));
            String asset = cursor.getString(cursor.getColumnIndex(expense[1]));
            String category= cursor.getString(cursor.getColumnIndex(expense[2]));
            int amount=cursor.getInt(cursor.getColumnIndex(expense[3]));
            String memo =cursor.getString(cursor.getColumnIndex(expense[4]));
            String regDateTime = cursor.getString(cursor.getColumnIndex("reg_date_time"));
            dailyInAndOut = new DailyInAndOut(ex_id,"지출",date,asset,category,amount,memo,regDateTime);
            alldayList.add(dailyInAndOut);
            outList.add(dailyInAndOut);
        }
        //수입넣기
        cursor = database.rawQuery("select income_id,income_date,asset_name,incomecategory_name,amount,reg_date_time,memo"+
                " from income where income_date=?",new String[]{selectDayStr});
        while(cursor.moveToNext()){
            int in_id= cursor.getInt(0);
            String date =cursor.getString(cursor.getColumnIndex(income[0]));
            String asset = cursor.getString(cursor.getColumnIndex(income[1]));
            String category= cursor.getString(cursor.getColumnIndex(income[2]));
            int amount=cursor.getInt(cursor.getColumnIndex(income[3]));
            String memo =cursor.getString(cursor.getColumnIndex(income[4]));
            String regDateTime = cursor.getString(cursor.getColumnIndex("reg_date_time"));
            dailyInAndOut = new DailyInAndOut(in_id,"수입",date,asset,category,amount,memo,regDateTime);
            alldayList.add(dailyInAndOut);
            inList.add(dailyInAndOut);
        }
        cursor.close();
        MyComparator myComparator = new MyComparator();
        Collections.sort(alldayList,myComparator);
        if(alldayList!=null){
            for (DailyInAndOut dayData: alldayList) {
                setCardTextView(dayData);
            }
        }

        //하루 총합구하기
        int incomeTotal=0;
        int expenseTotal=0;
        int dayTotal=0;
        for (DailyInAndOut ex: outList ) {
            expenseTotal+=ex.getAmount();
        }
        for (DailyInAndOut in: inList ) {
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

    ///////////////어레이리스트 정렬시키기기
   class MyComparator implements Comparator<DailyInAndOut> {
        @Override
        public int compare(DailyInAndOut o1, DailyInAndOut o2) {
            if(Double.parseDouble(o1.getRegDateTime())>Double.parseDouble(o2.getRegDateTime())){
                return 1;
            }
            return -1;
        }
    }

    ////////////////

    private void setCardTextView(final DailyInAndOut dailyInAndOut) {
        String category=dailyInAndOut.getCategoryName();
        String asset=dailyInAndOut.getAssetName();
        int amount=dailyInAndOut.getAmount();
        String memo=dailyInAndOut.getMemo();
        CardView cardView = new CardView(getContext());
        cardView.setRadius(20);
        LinearLayout innercardviewLinear= new LinearLayout(getContext());
        innercardviewLinear.setOrientation(LinearLayout.VERTICAL);
        TextView categoryT,memoT,amountT;
        categoryT= new TextView(getContext());
        categoryT.setText("  "+category+" | "+asset);
        amountT= new TextView(getContext());
        amountT.setGravity(Gravity.END);
        if (dailyInAndOut.getType().equals("수입")){
            amountT.setText(
                    Html.fromHtml("<font color=\"#0000FF\">"+numberFormat.format(amount)
                            +"</font> 원  "));
        }else if (dailyInAndOut.getType().equals("지출")){
            amountT.setText(
                    Html.fromHtml("<font color=\"#FF0000\">"+numberFormat.format(amount)
                            +"</font> 원  "));
        }
        amountT.setTextSize(18);
        memoT= new TextView(getContext());
        memoT.setText("  "+memo);
        innercardviewLinear.addView(categoryT);
        innercardviewLinear.addView(amountT);
        innercardviewLinear.addView(memoT);
        cardView.addView(innercardviewLinear);

        testlinearlayout.addView(cardView);
        if(cardView.getLayoutParams()==null){
        }else {
            LinearLayout.LayoutParams layoutParams= (LinearLayout.LayoutParams) cardView.getLayoutParams();
            layoutParams.bottomMargin=10;
            layoutParams.leftMargin=18;
            layoutParams.rightMargin=18;
            cardView.setLayoutParams(layoutParams);
        }
        cardView.setElevation(0);
        cardView.setContentPadding(0,10,0,10);
        if (dailyInAndOut.getType().equals("지출")){
            cardView.setCardBackgroundColor(Color.parseColor("#4DFDB3AE"));
        }else {
            cardView.setCardBackgroundColor(Color.parseColor("#8DBFE8FA"));
        }
        //카드뷰에 터치이벤트걸기
        cardView.setOnTouchListener(new OnSwipeTouchListener(getContext()){
            @SuppressLint("ClickableViewAccessibility")
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onSwipeRight() {
                String selectDayStr = titleTextView.getText().toString();
                LocalDate selectDay = LocalDate.parse(selectDayStr);
                titleTextView.setText(selectDay.minusDays(1).toString());
                setSevenDays();
                setCardView();
            }
            @SuppressLint("ClickableViewAccessibility")
            @RequiresApi(api = Build.VERSION_CODES.O)
            public void onSwipeLeft() {
                String selectDayStr = titleTextView.getText().toString();
                LocalDate selectDay = LocalDate.parse(selectDayStr);
                titleTextView.setText(selectDay.plusDays(1).toString());
                setSevenDays();
                setCardView();
            }
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void onClick() {
                Intent intent = new Intent(getContext(), UpdateMoneyBookActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP);
                intent.putExtra("contents",dailyInAndOut);
                getContext().startActivity(intent);
            }

            @Override
            public void hidefabAction() {
                fab.hide();
            }

            @SuppressLint("ClickableViewAccessibility")
            @Override
            public void showfabAction() {
                BackThread thread = new BackThread();
                thread.start();
            }
            @Override
            public void showfabOnScroll() {
                fab.show();
            }

            @Override
            public void onConfirmDelete() {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(),R.style.Theme_AppCompat_Light_Dialog_Alert);
                builder.setTitle("삭제확인");
                builder.setMessage("정말 삭제하시겠습니까?");
                builder.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteMoneybook(dailyInAndOut);
                        database.close();
                        MainActivity MA = (MainActivity) MainActivity.activity;
                        MA.finish();
                        Intent intent = new Intent(getContext(),MainActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP
                                |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("date",titleTextView.getText().toString()+"");
                        MA.startActivity(intent);
                    }
                });
                builder.setNegativeButton("삭제 안함", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }//카드뷰 안쪽 텍스트뷰 만들고 리니어레이아웃에 뷰추가

    //플러스버튼 나타나게하는 스레드
    private class BackThread extends Thread{
        @Override
        public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {}

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        fab.show();
                    }
                });
        }
    }

    private void deleteMoneybook(DailyInAndOut dailyInAndOut) {
        String exDelsql="delete from expense where expense_id="+dailyInAndOut.getId();
        String inDelsql="delete from income where income_id="+dailyInAndOut.getId();
        try {
            if(dailyInAndOut.getType().equals("지출")){
                database.execSQL(exDelsql);
            }else if (dailyInAndOut.getType().equals("수입")) {
                database.execSQL(inDelsql);
            }
            Toast.makeText(getContext(),"삭제 성공했습니다",Toast.LENGTH_SHORT).show();
        }catch (Exception e){
            Toast.makeText(getContext(),"삭제 실패했습니다",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.month_main,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

       if(R.id.tab5 == item.getItemId()){
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
        String b3text="";String b2text="";String b1text="";String setext="";String a1text="";String a2text="";String a3text="";

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
        select.setText(setWeekdayStr(selecday.getDayOfWeek().toString())+"\n"
                +selecday.getDayOfMonth());
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


        if (today.isEqual(selecday.minusDays(3))){
            b3text=setWeekdayStr(selecday.minusDays(3).getDayOfWeek().toString())+"<br>"
                    +"<u>"+selecday.minusDays(3).getDayOfMonth() +"</u>";
            before3.setText(Html.fromHtml(b3text));
        }else  if (today.isEqual(selecday.minusDays(2))){
            b2text=setWeekdayStr(selecday.minusDays(2).getDayOfWeek().toString())+"<br>"
                    +"<u>"+selecday.minusDays(2).getDayOfMonth() +"</u>";
            before2.setText(Html.fromHtml(b2text));
        }else if (today.isEqual(selecday.minusDays(1))){
            b1text=setWeekdayStr(selecday.minusDays(1).getDayOfWeek().toString())+"<br>"
                    +"<u>"+selecday.minusDays(1).getDayOfMonth() +"</u>";
            before1.setText(Html.fromHtml(b1text));
        }else if (today.isEqual(selecday.minusDays(0))){
            setext=setWeekdayStr(selecday.minusDays(0).getDayOfWeek().toString())+"<br>"
                    +"<u>"+selecday.minusDays(0).getDayOfMonth() +"</u>";
            select.setText(Html.fromHtml(setext));
        }else if (today.isEqual(selecday.minusDays(-1))){
            a1text=setWeekdayStr(selecday.minusDays(-1).getDayOfWeek().toString())+"<br>"
                    +"<u>"+selecday.minusDays(-1).getDayOfMonth() +"</u>";
            next1.setText(Html.fromHtml(a1text));
        }else if (today.isEqual(selecday.minusDays(-2))){
            a2text=setWeekdayStr(selecday.minusDays(-2).getDayOfWeek().toString())+"<br>"
                    +"<u>"+selecday.minusDays(-2).getDayOfMonth() +"</u>";
            next2.setText(Html.fromHtml(a2text));
        }else if (today.isEqual(selecday.minusDays(-3))){
            a3text=setWeekdayStr(selecday.minusDays(-3).getDayOfWeek().toString())+"<br>"
                    +"<u>"+selecday.minusDays(-3).getDayOfMonth() +"</u>";
            next3.setText(Html.fromHtml(a3text));
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