package com.example.moneybook;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Build;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class PasswordConfirmActivity extends AppCompatActivity {
    EditText password;
    String inputPassword;
    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;
    Button n1, n2, n3, n4, n5, n6, n7, n8, n9, n0;
    ImageButton del;
    ImageView pwCheck;
    TextView pwFalse;
    int checked;

    String dbPassword="";

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_confirm);

        password = findViewById(R.id.password);
        n1 = findViewById(R.id.num1);
        n2 = findViewById(R.id.num2);
        n3 = findViewById(R.id.num3);
        n4 = findViewById(R.id.num4);
        n5 = findViewById(R.id.num5);
        n6 = findViewById(R.id.num6);
        n7 = findViewById(R.id.num7);
        n8 = findViewById(R.id.num8);
        n9 = findViewById(R.id.num9);
        n0 = findViewById(R.id.num0);
        del = findViewById(R.id.removebutton);
        pwCheck = findViewById(R.id.pwCheck);
        pwFalse = findViewById(R.id.pwFalse);

        dbHelper = new DatabaseHelper(getApplicationContext());
        database= dbHelper.getReadableDatabase();

        password.setTextIsSelectable(true);
        password.setShowSoftInputOnFocus(false);

        //각종 숫자번호
        n1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.append("1");
            }
        });
        n2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.append("2");
            }
        });
        n3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.append("3");
            }
        });
        n4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.append("4");
            }
        });
        n5.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.append("5");
            }
        });
        n6.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.append("6");
            }
        });
        n7.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.append("7");
            }
        });
        n8.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.append("8");
            }
        });
        n9.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.append("9");
            }
        });
        n0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                password.append("0");
            }
        });

        //비밀번호보이도록
        pwCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!v.isActivated()){
                    password.setTransformationMethod(null);
                } else {
                    password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                }
                v.setActivated(!v.isActivated());
            }
        });

        cursor = database.rawQuery("select password from user",null);
        while(cursor.moveToNext()){
            dbPassword= cursor.getString(0);
        }
        if (dbPassword.equals("")){
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            cursor.close();
            finish();
        }

        //삭제버튼 눌렀을 때
        del.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int pwLength = password.getText().length();
                if(pwLength > 0){
                    password.getText().delete(pwLength - 1, pwLength);
                }
            }
        });

        //로그인버튼 눌렀을 때
        findViewById(R.id.loginbutton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(password.getText().toString().equals("")){
                    password.post(new Runnable() {
                        @Override
                        public void run() {
                            password.setFocusableInTouchMode(true);
                            password.requestFocus();
                        }
                    });
                    pwFalse.setVisibility(View.VISIBLE);
                    pwFalse.setText("암호를 입력하세요");
                }else{
                    inputPassword=password.getText().toString();
                    if (inputPassword.equals(dbPassword)){
                        pwFalse.setVisibility(View.GONE);
                        Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                        startActivity(intent);
                        cursor.close();
                        finish();
                    }else{
                        pwFalse.setVisibility(View.VISIBLE);
                        pwFalse.setText("비밀번호가 틀렸습니다!!");
                    }
                }
            }
        });

    }
}