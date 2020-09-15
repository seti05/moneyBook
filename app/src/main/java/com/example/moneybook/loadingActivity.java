package com.example.moneybook;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class loadingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent= new Intent(getApplicationContext(), PasswordConfirmActivity.class);
        startActivity(intent);  //Loagin화면을 띄운다.
        finish();   //현재 액티비티 종료
       // startLoading();
    }// onCreate()..
}// MainActivity Class..