package com.example.moneybook;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

public class loadingActivity extends AppCompatActivity implements AutoPermissionsListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AutoPermissions.Companion.loadAllPermissions(this,101);
//        Intent intent= new Intent(getApplicationContext(), PasswordConfirmActivity.class);
//        startActivity(intent);  //Loagin화면을 띄운다.
//        finish();   //현재 액티비티 종료

       // startLoading();
    }// onCreate()..

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        AutoPermissions.Companion.parsePermissions(this , requestCode,permissions,this);
    }

    @Override
    public void onDenied(int i, String[] strings) {
        Toast.makeText(getApplicationContext(),"권한 거부시 일부 서비스 이용에 문제가 생길수 있습니다.",Toast.LENGTH_SHORT).show();
        Intent intent= new Intent(getApplicationContext(), PasswordConfirmActivity.class);
        startActivity(intent);  //Loagin화면을 띄운다.
        finish();   //현재 액티비티 종료
    }

    @Override
    public void onGranted(int i, String[] strings) { Intent intent= new Intent(getApplicationContext(), PasswordConfirmActivity.class);
        startActivity(intent);  //Loagin화면을 띄운다.
        finish();   //현재 액티비티 종료
    }

}// MainActivity Class..