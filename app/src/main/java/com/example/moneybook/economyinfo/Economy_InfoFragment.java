package com.example.moneybook.economyinfo;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.moneybook.MainActivity;
import com.example.moneybook.R;
import com.example.moneybook.SettingsActivity;
import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.R.id.home;

public class Economy_InfoFragment extends Fragment {
    ViewPager pager;
    private Context mContext;
    private TabLayout mTabLayout;
    Toolbar toolbar;
    TextView textView;
    TextView titleTextView;

    Handler handler = new Handler();
    ProgressDialog progressDialog;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_economy__info, container, false);

        mContext=getContext();
        mTabLayout=(TabLayout)view.findViewById(R.id.tablayout);

        pager = view.findViewById(R.id.viewpager);
        MyPagerAdapter adapter = new MyPagerAdapter(getChildFragmentManager());

        ExchangeRateFragment exchangeRateFragment = new ExchangeRateFragment();
        adapter.addPage(exchangeRateFragment);
        StockFragment stockFragment = new StockFragment();
        adapter.addPage(stockFragment);
        pager.setAdapter(adapter);
        pager.setSaveEnabled(false);

        pager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(mTabLayout));
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                pager.setCurrentItem(tab.getPosition());
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }
            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });
        toolbar = view.findViewById(R.id.toolbar);
        ((MainActivity)getActivity()).setSupportActionBar(toolbar);
        ActionBar actionBar = ((MainActivity)getActivity()).getSupportActionBar();
        actionBar.setDisplayShowTitleEnabled(false);//원래 상단바의 이름을 감춤
        titleTextView = view.findViewById(R.id.titleText);
        //제목입력
        titleTextView.setText("킴앤정보");


        return view;
    }//onCreateView끝

    class  MyPagerAdapter extends FragmentStatePagerAdapter {
        ArrayList<Fragment> plist = new ArrayList<>();

        public MyPagerAdapter(@NonNull FragmentManager fm) {
            super(fm);
        }

        public  void addPage(Fragment page){
            plist.add(page);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return plist.get(position);
        }

        @Override
        public int getCount() {
            return plist.size();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }
    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.month_main,menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(R.id.tab5 == item.getItemId()){
            //Toast.makeText(this, "설정 눌렀지" , Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}