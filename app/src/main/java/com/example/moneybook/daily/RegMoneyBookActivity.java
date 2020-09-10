package com.example.moneybook.daily;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.example.moneybook.DatabaseHelper;
import com.example.moneybook.MainActivity;
import com.example.moneybook.R;
import com.example.moneybook.settings.MinMaxFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.ArrayList;

@RequiresApi(api = Build.VERSION_CODES.O)
public class RegMoneyBookActivity extends AppCompatActivity {
    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    boolean isExpenseChecked=true;
    private Spinner spinner2,spinner;
    ArrayAdapter<String> arrayAdapter;
    ArrayAdapter<String> arrayAdapter2;

    ArrayList<String> incomeCat = new ArrayList<>();
    ArrayList<String> expenseCat = new ArrayList<>();
    ArrayList<String> assetList = new ArrayList<>();

    Button selecIncomeButton, selecExpenseButton,selecDayButton;
    Cursor cursor;
    LocalDate today = LocalDate.now();

    String inputDay,inputAsset,inputCategory,inputAmount,inputMemo;

    EditText amountEdit,memoEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reg_money_book);

        selecIncomeButton = findViewById(R.id.selectInButton);
        selecExpenseButton = findViewById(R.id.selectExButton);
        selecDayButton = findViewById(R.id.selectDayButton);
        spinner = findViewById(R.id.assetSpinner);
        spinner2 = findViewById(R.id.selectCategorySpinner);
        amountEdit = findViewById(R.id.editTextNumber);
        amountEdit.setFilters(new InputFilter[]{ new MinMaxFilter( "1" , "100000000" )});
        amountEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {  }
            @Override
            public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.length() > 7){
                    Toast.makeText(RegMoneyBookActivity.this,"최대1억까지 입력가능합니다",Toast.LENGTH_SHORT).show();
                }
            }
        });
        memoEdit = findViewById(R.id.editTextMemo);
        memoEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void afterTextChanged(Editable s) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (s.length() > 39){
                    Toast.makeText(RegMoneyBookActivity.this,"40자이상 입력할수 없습니다",Toast.LENGTH_SHORT).show();
                }
            }
        });

        if(getIntent().getStringExtra("regDate")==null){
            selecDayButton.setText(today.toString());
        }else {
            String selecDate=getIntent().getStringExtra("regDate");
            selecDayButton.setText(selecDate);
        }

        dbHelper = new DatabaseHelper(getApplicationContext());
        database = dbHelper.getWritableDatabase();

        setCategory();

        setCategoryName();
        setAsset();

        //날짜 버튼 클릭
        selecDayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog;
                if(getIntent().getStringExtra("regDate")==null){
                    dialog = new DatePickerDialog(RegMoneyBookActivity.this
                            ,listener, today.getYear(),today.getMonthValue()-1,today.getDayOfMonth());
                }else {
                    String selecDate=getIntent().getStringExtra("regDate");
                    String[] dates=selecDate.split("-");
                    Log.d("날짜등록", "우선 확인용"+dates[1].indexOf("0"));
                    String dayStr,monthStr;
                    if (dates[1].indexOf("0")==0){
                        monthStr=dates[1].substring(1);
                        Log.d("날짜등록", "변경한거"+monthStr);
                    }else{
                        monthStr=dates[1];
                    }
                    if (dates[2].indexOf("0")==0){
                        dayStr=dates[2].substring(1);
                        Log.d("날짜등록", "변경한거"+dayStr);
                    }else{
                        dayStr=dates[2];
                    }

                    dialog = new DatePickerDialog(RegMoneyBookActivity.this
                            ,listener, Integer.parseInt(dates[0]),Integer.parseInt(monthStr)-1,Integer.parseInt(dayStr));
                }

                dialog.show();
            }
        });


        //입력버튼
        Button regMoneybookButton = findViewById(R.id.button);
        regMoneybookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                inputDay = selecDayButton.getText().toString();
                if(amountEdit.getText()==null|amountEdit.getText().toString().equals("")){
                    amountEdit.post(new Runnable() {
                        @Override
                        public void run() {
                            amountEdit.setFocusableInTouchMode(true);
                            amountEdit.requestFocus();
                        }
                    });
                    Toast.makeText(getApplicationContext(),"금액입력하세요",Toast.LENGTH_SHORT).show();
                }else {
                    inputAmount = amountEdit.getText().toString();
                    inputMemo = memoEdit.getText().toString();
                    insertMoneybook();
                }
            }
        });

        selecIncomeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecIncomeButton.setBackgroundColor(Color.parseColor("#ffcccc"));
                selecExpenseButton.setBackgroundColor(Color.parseColor("#d6d7d7"));
                isExpenseChecked=false;
                setCategoryName();
            }
        });
        selecExpenseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecExpenseButton.setBackgroundColor(Color.parseColor("#ffcccc"));
                selecIncomeButton.setBackgroundColor(Color.parseColor("#d6d7d7"));
                isExpenseChecked=true;
                setCategoryName();
            }
        });
    }

    private void setAsset() {
        arrayAdapter2 = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_item,
                assetList);
        arrayAdapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(arrayAdapter2);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    inputAsset=assetList.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });
    }

    private void setCategoryName() {
        if(isExpenseChecked){
            arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                    android.R.layout.simple_spinner_item,
                    expenseCat);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(arrayAdapter);
            spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    inputCategory=expenseCat.get(position);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }else{
            arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                    android.R.layout.simple_spinner_item,
                    incomeCat);
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner2.setAdapter(arrayAdapter);
            spinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    inputCategory=incomeCat.get(position);
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }

    }

    private void setCategory() {
        cursor = database.rawQuery("select incomecategory_name from incomecategory",null);
        while(cursor.moveToNext()){
            String name = cursor.getString(0);
            incomeCat.add(name);
        }
        cursor = database.rawQuery("select expensecategory_name from expensecategory",null);
        while(cursor.moveToNext()){
            String name = cursor.getString(0);
            expenseCat.add(name);
        }
        cursor = database.rawQuery("select asset_name from asset",null);
        while(cursor.moveToNext()){
            String name = cursor.getString(0);
            assetList.add(name);
        }
        cursor.close();
    }

    private void insertMoneybook(){
        String sql="";
        if(isExpenseChecked){
            sql= "insert into expense(expense_date,asset_name,expensecategory_name,amount,reg_date_time,memo)"+
                    " values('"+inputDay+"','"+inputAsset+"','"+inputCategory+"',"+
                    Integer.parseInt(inputAmount)+",'"+System.currentTimeMillis()+"','"+inputMemo+"')";
            database.execSQL(sql);
        }else {
            sql= "insert into income(income_date, asset_name ,incomecategory_name,amount,reg_date_time,memo)"+
                    " values('"+inputDay+"','"+inputAsset+"','"+inputCategory+"',"+
                    Integer.parseInt(inputAmount)+",'"+System.currentTimeMillis()+"','"+inputMemo+"')";
            database.execSQL(sql);
        }
        confirmRegReview();
    }
    void confirmRegReview() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.Theme_AppCompat_Light_Dialog_Alert);
        builder.setTitle("추가확인");
        builder.setMessage("계속 추가하시겠습니까?");
        builder.setPositiveButton("계속추가", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                amountEdit.setText("");
                memoEdit.setText("");
            }
        });
        builder.setNegativeButton("추가안함", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity MA = (MainActivity) MainActivity.activity;
                MA.finish();
                Intent intent = new Intent(getApplicationContext(),MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_SINGLE_TOP
                        |Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("date",inputDay+"");
                finish();
                MA.startActivity(intent);
            }
        });
        builder.show();
    }

    private DatePickerDialog.OnDateSetListener listener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            String dayStr = String.valueOf(dayOfMonth);
            String monthStr = String.valueOf(month+1);
            if(dayStr.length()==1){
                dayStr="0"+dayStr;
            }
            if(monthStr.length()==1){
                monthStr="0"+monthStr;
            }
            selecDayButton.setText(year + "-" + monthStr + "-" + dayStr);

        }
    };
}