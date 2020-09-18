package com.example.moneybook.chart;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.example.moneybook.DatabaseHelper;
import com.example.moneybook.R;
import com.example.moneybook.daily.DailyInAndOut;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AssetDataListActivity extends Activity {
    TextView assetNameTextView;
    RecyclerView assetListRecyclerView;
    OneAssetDataAdapter oneAssetDataAdapter;
    DatabaseHelper dbhelper;
    SQLiteDatabase database;
    String year_month_day, year_month,assetDate,assetType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_asset_data_list);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        assetNameTextView=findViewById(R.id.assetNameTextView);
        assetListRecyclerView=findViewById(R.id.oneAssetDataListRecyclerView);

        //선택된 자산이름 보여주기
        if(getIntent().getStringExtra("assetName")!=null){
            assetNameTextView.setText(getIntent().getStringExtra("assetName"));
        }

        oneAssetDataAdapter = new OneAssetDataAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL, false);
        assetListRecyclerView.setLayoutManager(layoutManager);
        assetListRecyclerView.setAdapter(oneAssetDataAdapter);
        dbhelper = new DatabaseHelper(this);
        database = dbhelper.getWritableDatabase();

        //자산사용내역보여주기
        showOneAssetData();

    }//onCreate끝



    private void showOneAssetData() {
        oneAssetDataAdapter.clear();
        if(getIntent().getStringExtra("year_month")!=null){
            year_month=getIntent().getStringExtra("year_month");
        }
        if(getIntent().getStringExtra("year_month_day")!=null){
            year_month_day=getIntent().getStringExtra("year_month_day");
        }
        if(getIntent().getStringExtra("assetDate")!=null){
            assetDate=getIntent().getStringExtra("assetDate");
        }
        if(getIntent().getStringExtra("assetType")!=null){
            assetType=getIntent().getStringExtra("assetType");
        }

        if(database != null){
            String sql="";
            if (assetType.equals("expense")){
                sql = "select expense_date,expensecategory_name,amount,reg_date_time,memo from " + assetType
                        + " where " + assetDate + " >= '" + year_month + "-01' and "
                        + assetDate + " <= " + "'" + year_month_day + "' and asset_name ='"+assetNameTextView.getText().toString()
                        +"' order by expense_date asc,reg_date_time desc";
            }else {
                sql = "select income_date,incomecategory_name,amount,reg_date_time,memo from " + assetType
                        + " where " + assetDate + " >= '" + year_month + "-01' and "
                        + assetDate + " <= " + "'" + year_month_day + "' and asset_name ='"+assetNameTextView.getText().toString()
                        +"' order by income_date asc,reg_date_time desc";
            }
            String date,cateName,regTime,amount,memo;
            if(sql != null){
                Cursor cursor = database.rawQuery(sql, null);
                try {
                    while (cursor.moveToNext()){
                        date=cursor.getString(0);
                        cateName = cursor.getString(1);
                        amount=cursor.getString(2);
                        regTime=cursor.getString(3);
                        memo=cursor.getString(4);
                        DailyInAndOut d = new DailyInAndOut(0, null, date, assetNameTextView.getText().toString(), cateName, Integer.parseInt(amount), memo,regTime);
                        oneAssetDataAdapter.addItem(d);
                    }
                }catch (Exception e){
                    Toast.makeText(getApplicationContext(),"불러오는 중에 오류가 발생했습니다.",Toast.LENGTH_SHORT);
                }
                cursor.close();
                oneAssetDataAdapter.notifyDataSetChanged();
            }
        }
    }

    public static void maxinumDialogWindowHeight(Window window) {
        WindowManager.LayoutParams layout = new WindowManager.LayoutParams();
        layout.copyFrom(window.getAttributes());
        layout.height = WindowManager.LayoutParams.FILL_PARENT;
        window.setAttributes(layout);
    }


    public class OneAssetDataAdapter extends RecyclerView.Adapter<OneAssetDataAdapter.ViewHolder> {

        ArrayList<DailyInAndOut> items = new ArrayList<>();

        @NonNull
        @Override
        public OneAssetDataAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.one_asset_data_item, parent, false);
            return new OneAssetDataAdapter.ViewHolder(itemView);
        }


        @Override
        public void onBindViewHolder(@NonNull OneAssetDataAdapter.ViewHolder holder, int position) {
            final DailyInAndOut item = items.get(position);
            holder.setItem(item, position);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView dateText, cateText, amountText,memoText;
            NumberFormat numberFormat;

            public ViewHolder(@NonNull final View itemView) {
                super(itemView);
                dateText = itemView.findViewById(R.id.useDateTextView);
                cateText = itemView.findViewById(R.id.categoryTextView);
                amountText = itemView.findViewById(R.id.amountTextView);
                memoText = itemView.findViewById(R.id.memoTextView);
                if (assetType.equals("expense")){
                    itemView.setBackgroundColor(Color.parseColor("#4DFDB3AE"));
                }else{
                    itemView.setBackgroundColor(Color.parseColor("#8DBFE8FA"));
                }
            }

            public void setItem(DailyInAndOut item, int position) {
                numberFormat = NumberFormat.getInstance(Locale.getDefault());
                dateText.setText(item.getDate());
                cateText.setText(item.getCategoryName());
                amountText.setText(numberFormat.format(item.getAmount()) + " 원");
                memoText.setText(item.getMemo());

            }
        }

        public void addItem(DailyInAndOut item){items.add(item);}


        public void setItems(ArrayList<DailyInAndOut> item) {this.items = items;}

        public DailyInAndOut getItem(int position) {return items.get(position);}

        public void setItem(int position, DailyInAndOut item) {items.set(position, item);}

        public void clear(){
            items.clear();
        }
    }


}