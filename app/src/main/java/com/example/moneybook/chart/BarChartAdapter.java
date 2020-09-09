package com.example.moneybook.chart;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneybook.MainActivity;
import com.example.moneybook.R;
import com.example.moneybook.daily.DailyInAndOut;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;


public class BarChartAdapter extends RecyclerView.Adapter<BarChartAdapter.ViewHolder> {

    ArrayList<DailyInAndOut> items = new ArrayList<>();
    ArrayList<DailyInAndOut> incomeArr = new ArrayList<>();

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chart_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final DailyInAndOut item = items.get(position);
        holder.setItem(item, position);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(v.getContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                String month = item.getDate() + "-01";
                intent.putExtra("month", month);
                v.getContext().startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        TextView month, income, expense;
        NumberFormat numberFormat;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            month = itemView.findViewById(R.id.charMonth);
            income = itemView.findViewById(R.id.chartIncomeText);
            expense = itemView.findViewById(R.id.chartExpenseText);
        }

        public void setItem(DailyInAndOut item, int position){
            numberFormat = NumberFormat.getInstance(Locale.getDefault());
            String date = String.valueOf(item.getDate());
            String monthStr = date.substring(date.indexOf("-")+1);//년 부분만 짤라옴
            month.setText(monthStr + "월");
            income.setText(String.valueOf(numberFormat.format(incomeArr.get(position).getAmount())));
            expense.setText(numberFormat.format(item.getAmount()));
        }
    }

    public void incomeItem(DailyInAndOut item){incomeArr.add(item);}

    public void addItem(DailyInAndOut item){items.add(item);}

    public void setItems(ArrayList<DailyInAndOut> item) {this.items = items;}

    public DailyInAndOut getItem(int position) {return items.get(position);}

    public void setItem(int position, DailyInAndOut item) {items.set(position, item);}

    public void clear(){
        items.clear();
        incomeArr.clear();
    }
}
