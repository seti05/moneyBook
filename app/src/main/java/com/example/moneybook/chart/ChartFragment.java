package com.example.moneybook.chart;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.example.moneybook.R;
import com.example.moneybook.SettingsActivity;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ChartFragment extends Fragment {

    ViewPager pager;
    TextView titleText;
    TabLayout chartTab;
    Bundle bundle;
    Toolbar toolbar;
    BarChartFragment barChartFragment;
    AssetChartFragment assetChartFragment;
    int yearNum;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_chart, container, false);

        pager = view.findViewById(R.id.pager);
        titleText = view.findViewById(R.id.titleText);
        chartTab = view.findViewById(R.id.chartTab);
        bundle = new Bundle();
        toolbar = view.findViewById(R.id.toolbar);

        pager.setSaveEnabled(false);

        //상단바에 원래 타이틀명 지움
        AppCompatActivity activity = (AppCompatActivity) getActivity();
        activity.setSupportActionBar(toolbar);
        activity.getSupportActionBar().setDisplayShowTitleEnabled(false);

        //프래그먼트 지정
        MyPagerAdapter adapter = new MyPagerAdapter(getChildFragmentManager());
        barChartFragment = new BarChartFragment();
        adapter.addItem(barChartFragment);
        assetChartFragment = new AssetChartFragment();
        adapter.addItem(assetChartFragment);
        pager.setAdapter(adapter);

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(chartTab));
        chartTab.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        //타이틀바 셋팅
        titleBar();

        return view;
    }

    //상단 셋팅
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.month_main,menu);
    }

    //설정버튼 클릭시
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(R.id.tab5 == item.getItemId()){
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return true;
    }

    private void titleBar(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        final SimpleDateFormat curYearFormat = new SimpleDateFormat("yyyy", Locale.KOREA);
        SimpleDateFormat curYear = curYearFormat;

        titleText.setText(curYear.format(date) + "년");

        titleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                YearPicker picker = new YearPicker();
                picker.setListener(listener);
                picker.show(getFragmentManager(), "YearPicker");

            }
        });
    }

    DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            yearNum = year;
            titleText.setText(year + "년");
        }
    };

    //페이저 이용
    private class MyPagerAdapter extends FragmentStatePagerAdapter {

        ArrayList<Fragment> items = new ArrayList<>();

        public MyPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        public void addItem(Fragment item){items.add(item);}

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return items.get(position);
        }

        @Override
        public int getCount() {
            return items.size();
        }
    }
}