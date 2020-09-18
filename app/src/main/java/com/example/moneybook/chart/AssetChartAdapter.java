package com.example.moneybook.chart;


import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneybook.R;
import com.example.moneybook.SettingsActivity;
import com.example.moneybook.daily.DailyInAndOut;
import com.example.moneybook.settings.CateUpdateActivity;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;

public class AssetChartAdapter extends RecyclerView.Adapter<AssetChartAdapter.ViewHolder> {

    ArrayList<DailyInAndOut> items = new ArrayList<>();
    ArrayList<String> perList = new ArrayList<>();
    String year_month_day, year_month,assetDate,assetType;

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.pie_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final DailyInAndOut item = items.get(position);
        holder.setItem(item, position);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView assetTypeText, assetText, perText;
        NumberFormat numberFormat;

        public ViewHolder(@NonNull final View itemView) {
            super(itemView);
            assetTypeText = itemView.findViewById(R.id.assetTypeText);
            assetText = itemView.findViewById(R.id.assetText);
            perText = itemView.findViewById(R.id.assetPer);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Log.d("월별 내역", items.get(getAdapterPosition()).getAssetName()+"눌렀지눌럿어");
                    Intent cateIntent = new Intent(itemView.getContext(), AssetDataListActivity.class);
                    cateIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    cateIntent.putExtra("assetName",items.get(getAdapterPosition()).getAssetName());
                    cateIntent.putExtra("year_month",year_month);
                    cateIntent.putExtra("year_month_day",year_month_day);
                    cateIntent.putExtra("assetDate",assetDate);
                    cateIntent.putExtra("assetType",assetType);
                    itemView.getContext().startActivity(cateIntent);
                }
            });
        }

        public void setItem(DailyInAndOut item, int position) {
            numberFormat = NumberFormat.getInstance(Locale.getDefault());
            assetTypeText.setText(item.getAssetName());
            assetText.setText(numberFormat.format(item.getAmount()) + " 원");
            perText.setText(" ( " + perList.get(position) + " )");
        }
    }

    public void addItem(DailyInAndOut item){items.add(item);}

    public void addPer(String per){perList.add(per);}

    public void setItems(ArrayList<DailyInAndOut> item) {this.items = items;}

    public DailyInAndOut getItem(int position) {return items.get(position);}

    public void setItem(int position, DailyInAndOut item) {items.set(position, item);}

    public void clear(){
        items.clear();
        perList.clear();
    }

    public void getPeriod(String ym,String ymd,String ad,String at){
        year_month=ym;
        year_month_day=ymd;
        assetDate=ad;
        assetType=at;
    }
}
