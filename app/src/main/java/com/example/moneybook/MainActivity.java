package com.example.moneybook;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.example.moneybook.daily.DailyFragment;
import com.example.moneybook.daily.RegMoneyBookActivity;
import com.example.moneybook.economyinfo.Economy_InfoFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

public class MainActivity extends AppCompatActivity implements AutoPermissionsListener {
    DailyFragment dailyFragment;
    Economy_InfoFragment economy_infoFragment;
    public static Activity activity;


    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = MainActivity.this;

        //일일 프레그먼트와 연결
        dailyFragment= new DailyFragment();
        economy_infoFragment = new Economy_InfoFragment();

        getSupportFragmentManager().beginTransaction().add(R.id.main_container, dailyFragment).commit();

        //아래 네비게이션 바 클릭
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()){
                    //menu_bottom.xml에 있는 tab id로 구분함
                    case R.id.tab1:
                        Toast.makeText(getApplicationContext(),"첫번째 탭",Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, dailyFragment).commit();
                        return true;
                    case R.id.tab2:
                        Toast.makeText(getApplicationContext(),"두째 탭",Toast.LENGTH_SHORT).show();

                        return true;

                    case R.id.tab3:
                        Toast.makeText(getApplicationContext(),"세번째 탭",Toast.LENGTH_SHORT).show();

                        return true;
                    case R.id.tab4:
                        Toast.makeText(getApplicationContext(),"네번째 탭",Toast.LENGTH_SHORT).show();
                        getSupportFragmentManager().beginTransaction().replace(R.id.main_container, economy_infoFragment).commit();
                        return true;
                }
                return false;
            }
        });

        //프레그먼트에 정보주기
        Bundle bundle = new Bundle();
        bundle.putString("selectDay",getIntent().getStringExtra("date"));
        dailyFragment.setArguments(bundle);

        AutoPermissions.Companion.loadAllPermissions(this,101);
    }//onCreate끝

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this , requestCode,permissions,this);
    }

    @Override
    public void onDenied(int i, String[] strings) { }

    @Override
    public void onGranted(int i, String[] strings) { }
}