package com.example.moneybook;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.moneybook.calendar.CalendarFragment;
import com.example.moneybook.chart.ChartFragment;
import com.example.moneybook.daily.DailyFragment;
import com.example.moneybook.economyinfo.Economy_InfoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

import java.util.Stack;

public class MainActivity extends AppCompatActivity {
    DailyFragment dailyFragment;
    Economy_InfoFragment economy_infoFragment;
    public static Activity activity;
    CalendarFragment calendarFragment;
    ChartFragment chartFragment;
    BottomNavigationView bottomNavigationView;
    public static FragmentTransaction fragmentTransaction;
    public static FragmentManager manager;

    private BackPressCloseHandler backPressCloseHandler;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = MainActivity.this;

        //일일 프레그먼트와 연결
        dailyFragment= new DailyFragment();
        economy_infoFragment = new Economy_InfoFragment();
        calendarFragment = new CalendarFragment();
        chartFragment = new ChartFragment();

        manager = getSupportFragmentManager();
        fragmentTransaction = manager.beginTransaction();

        getSupportFragmentManager().beginTransaction().add(R.id.main_container, dailyFragment).commit();

        //아래 네비게이션 바 클릭
        bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                switch (item.getItemId()){
                    //menu_bottom.xml에 있는 tab id로 구분함
                    case R.id.tab1:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, dailyFragment).commit();
                        if(getIntent().getStringExtra("date")!=null){
                            getIntent().removeExtra("date");
                        }
                        return true;
                    case R.id.tab2:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, calendarFragment).commit();
                        if(getIntent().getStringExtra("date")!=null){
                            getIntent().removeExtra("date");
                        }
                        return true;
                    case R.id.tab3:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, chartFragment).commit();
                        if(getIntent().getStringExtra("date")!=null){
                            getIntent().removeExtra("date");
                        }
                        return true;
                    case R.id.tab4:
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, economy_infoFragment).commit();
                        if(getIntent().getStringExtra("date")!=null){
                            getIntent().removeExtra("date");
                        }
                        return true;
                }
                return false;
            }
        });

        //프레그먼트에 정보주기
        Bundle bundle = new Bundle();
        bundle.putString("selectDay",getIntent().getStringExtra("date"));
        dailyFragment.setArguments(bundle);



        //차트 상세내역 클릭
        barchartClick();

        backPressCloseHandler = new BackPressCloseHandler(this);
    }//onCreate끝


    @Override
    public void onBackPressed() {
        backPressCloseHandler.onBackPressed();
    }

    private void barchartClick() {
        Intent barChartIntent = getIntent();
        if(barChartIntent != null) {
            String monthBarChart = barChartIntent.getStringExtra("month");
            if (monthBarChart != null) {
                String yearBarChart = monthBarChart.substring(0, 4);
                String monthBC = monthBarChart.substring(monthBarChart.indexOf("-") + 1, monthBarChart.lastIndexOf("-"));
                getSupportFragmentManager().beginTransaction().replace(R.id.main_container, calendarFragment).commit();
                bottomNavigationView.setSelectedItemId(R.id.tab2);
                Bundle bundle = new Bundle();
                bundle.putString("year", yearBarChart);
                bundle.putString("month", monthBC);
                calendarFragment.setArguments(bundle);
            }
        }
    }



    public class BackPressCloseHandler {
        //뒤로가기 버튼용 클래스

        private long backKeyPressedTime = 0;
        private Toast toast;

        private Activity activity;

        public BackPressCloseHandler(Activity context) {
            this.activity = context;
        }

        public void onBackPressed() {
            if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
                backKeyPressedTime = System.currentTimeMillis();
                showGuide();
                return;
            }
            if (System.currentTimeMillis() <= backKeyPressedTime + 2000) {
                activity.finish();
                toast.cancel();
            }
        }

        public void showGuide() {
            toast = Toast.makeText(activity,
                    "\'뒤로\'버튼을 한번 더 누르시면 종료됩니다.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }


}