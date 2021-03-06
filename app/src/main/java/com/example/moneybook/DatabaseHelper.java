package com.example.moneybook;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static String NAME = "moneybook.db";
    public static int VERSION = 1;//디비버전

    public DatabaseHelper(@Nullable Context context) {
        super(context, NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //유저테이블만들기
        String sqlUser = "create table if not exists user( "+
                " user_id integer primary key autoincrement, "+
                " password text) ";
        db.execSQL(sqlUser);
        //수입테이블만들기
        String sqlIncome = "create table if not exists income( "+
                " income_id integer primary key autoincrement, "+
                " income_date text, asset_name text,incomecategory_name text,amount integer,reg_date_time text,memo text) ";
        db.execSQL(sqlIncome);
        //지출테이블만들기
        String sqlExpense = "create table if not exists expense( "+
                " expense_id integer primary key autoincrement, "+
                " expense_date text, asset_name text,expensecategory_name text,amount integer,reg_date_time text,memo text) ";
        db.execSQL(sqlExpense);
        String sqlIncomeCategory = "create table if not exists incomecategory( "+
                " category_id integer primary key autoincrement, "+
                " incomecategory_name text) ";
        db.execSQL(sqlIncomeCategory);
        String sqlExpenseCategory = "create table if not exists expensecategory( "+
                " category_id integer primary key autoincrement, "+
                " expensecategory_name text) ";
        db.execSQL(sqlExpenseCategory);
        String sqlAsset = "create table if not exists asset( "+
                " asset_id integer primary key autoincrement, "+
                " asset_name text) ";
        db.execSQL(sqlAsset);
        String expenseCategoryinsertsql = "insert into expensecategory(expensecategory_name) " +
                "values('식비'),('교통비'),('통신비'),('경조사비'),('저축'),('기타')";
        db.execSQL(expenseCategoryinsertsql);
        String incomeCategoryinsertsql = "insert into incomecategory(incomecategory_name) values('급여'),('보너스'),('용돈'),('기타')";
        db.execSQL(incomeCategoryinsertsql);
        String assetinsertsql = "insert into asset(asset_name) values('현금'),('체크카드'),('신용카드')";
        db.execSQL(assetinsertsql);
        String inputUserSql = "insert into user(password)" +
                "values('')";
        db.execSQL(inputUserSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        printLog("onUpgrade() call: old-"+oldVersion+" => new -"+newVersion);
        if(newVersion >1){
            db.execSQL("DROP TABLE IF EXISTS user");
        }

    }

    public void printLog(String s){
        Log.d("MyDatabaseHelper", s);
    }
}
