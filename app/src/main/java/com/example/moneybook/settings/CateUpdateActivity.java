package com.example.moneybook.settings;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.moneybook.DatabaseHelper;
import com.example.moneybook.R;

import java.util.ArrayList;

public class CateUpdateActivity extends Activity {

    CheckBox exCheckBox,inCheckBox;

    RecyclerView cateRecyclerView;
    CategoryAdapter cateAdapter;
    DatabaseHelper dbHelper;
    SQLiteDatabase database;
    Cursor cursor;
    boolean isExChecked=true;
    Button addCateButton;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_cate_update);

        cateRecyclerView = findViewById(R.id.cateItemRecyclerView);
        cateAdapter = new CategoryAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        cateRecyclerView.setLayoutManager(layoutManager);
        cateRecyclerView.setAdapter(cateAdapter);
        dbHelper = new DatabaseHelper(getApplicationContext());
        database = dbHelper.getWritableDatabase();

        exCheckBox= findViewById(R.id.excheckBox);
        inCheckBox = findViewById(R.id.incheckBox);
        exCheckBox.setChecked(true);
        addCateButton= findViewById(R.id.addCateButton);
        addCateButton.setText("지출 카테고리 추가");

        //지출 선택했을때
        exCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    inCheckBox.setChecked(false);
                }
                if(inCheckBox.isChecked()==false && isChecked==false){

                    exCheckBox.setChecked(true);
                }
                isExChecked=true;
                if (isChecked){
                    addCateButton.setText("지출 카테고리 추가");
                    setCategoryName();
                }
            }
        });
        //수입 선택했을때
        inCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    exCheckBox.setChecked(false);
                }
                if(exCheckBox.isChecked()==false && isChecked==false){
                    inCheckBox.setChecked(true);
                }
                isExChecked=false;
                if (isChecked){
                    addCateButton.setText("수입 카테고리 추가");
                    setCategoryName();
                }
            }
        });
        //카테추가 버튼
        addCateButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onClick(View v) {
                regDBCategory();
            }
        });

        setCategoryName();

    }//온크리에이트 끝



    //리사이클러 뷰에 카테고리 이름 뿌려주기
    private void setCategoryName() {

        cateAdapter.clear();
        String exSelecSql="select category_id,expensecategory_name from expensecategory";
        String inSelecSql="select category_id,incomecategory_name from incomecategory";
        if(isExChecked){//지출 선택시
            cursor= database.rawQuery(exSelecSql,null);
            while(cursor.moveToNext()){
                int cateId= cursor.getInt(0);
                String cateitem =cursor.getString(1);
                cateAdapter.addItem(new UpdateSetting(cateId,"지출",cateitem));
            }

        }else {
            cursor= database.rawQuery(inSelecSql,null);
            while(cursor.moveToNext()){
                int cateId= cursor.getInt(0);
                String cateitem =cursor.getString(1);
                cateAdapter.addItem(new UpdateSetting(cateId,"수입",cateitem));
            }
        }
        cateAdapter.notifyDataSetChanged();
        cursor.close();
    }

    //카테고리 추가 다이얼로그로
    @RequiresApi(api = Build.VERSION_CODES.Q)
    void regDBCategory() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this,R.style.Theme_AppCompat_Light_Dialog_Alert);
        final EditText addCateEditText = new EditText(CateUpdateActivity.this);
        addCateEditText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        addCateEditText.setBackgroundColor(Color.parseColor("#FFF1F1"));
        addCateEditText.setTextColor(Color.BLACK);
        addCateEditText.setTextCursorDrawable(R.drawable.dialog_cursor_color);
        InputFilter[] FilterArray = new InputFilter[1];
        FilterArray[0] = new InputFilter.LengthFilter(10);
        addCateEditText.setFilters(FilterArray);
        addCateEditText.addTextChangedListener(new TextWatcher(){
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {
                if (addCateEditText.getText().toString().length()>9)
                {
                    Toast.makeText(getApplicationContext(),"10자이상 입력할수 없습니다",Toast.LENGTH_SHORT).show();
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
            public void afterTextChanged(Editable s){}
        });
        builder.setView(addCateEditText);
        if (isExChecked){
            builder.setTitle("지출 카테고리 추가");
        }else {
            builder.setTitle("수입 카테고리 추가");
        }
        builder.setPositiveButton("추가", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {}
        });

        builder.setNegativeButton("돌아감", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        final AlertDialog dialog= builder.create();
        // 창 띄우기
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                Boolean wantToCloseDialog = false;
                String inputRegCategory= addCateEditText.getText().toString();
                if (inputRegCategory.equals("")) {
                    addCateEditText.post(new Runnable() {
                        @Override
                        public void run() {
                            addCateEditText.setFocusableInTouchMode(true);
                            addCateEditText.requestFocus();
                        }
                    });
                    Toast.makeText(CateUpdateActivity.this, "추가할 이름을 입력하세요", Toast.LENGTH_SHORT).show();
                }else {
                    String exInsertsql="insert into expensecategory(expensecategory_name) values('"+inputRegCategory+"')";
                    String inInsertsql="insert into incomecategory(incomecategory_name) values('"+inputRegCategory+"')";
                    try {
                        if (isExChecked){
                            database.execSQL(exInsertsql);
                        }else {
                            database.execSQL(inInsertsql);
                        }
                        Toast.makeText(CateUpdateActivity.this, "카테고리등록완료", Toast.LENGTH_SHORT).show();
                        setCategoryName();
                        wantToCloseDialog = true;
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                }
                if(wantToCloseDialog)
                    dialog.dismiss();

            }
        });
    }



    //////////////////////////////////////////////////////////////////

    public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {
        ArrayList<UpdateSetting> items = new ArrayList<>();


        public CategoryAdapter() {
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            LayoutInflater inflater = LayoutInflater.from(parent.getContext());
            View itemView = inflater.inflate(R.layout.cate_andasset_item, parent, false);
            return new ViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            UpdateSetting item = items.get(position);
            holder.setItem(item);
        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public void addItem(UpdateSetting item) {
            items.add(item);
        }

        public UpdateSetting getItem(int position) {
            return items.get(position);
        }

        public void setItem(int position, UpdateSetting item) {
            items.set(position, item);
        }

        public void clear() {
            items.clear();
        }

        public ArrayList<UpdateSetting> getList() {
            return items;
        }



        class ViewHolder extends RecyclerView.ViewHolder {

            TextView itembutton;


            public ViewHolder(@NonNull final View itemView) {
                super(itemView);
                itembutton = itemView.findViewById(R.id.settingItemsButton);



                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int pos = getAdapterPosition();
                        if (pos != RecyclerView.NO_POSITION) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                                updateCategory();
                            }
                        }
                    }

                    @RequiresApi(api = Build.VERSION_CODES.Q)
                    private void updateCategory() {
                        AlertDialog.Builder builder = new AlertDialog.Builder(itemView.getContext(),R.style.Theme_AppCompat_Light_Dialog_Alert);
                        final EditText updateCateEditText = new EditText(itemView.getContext());
                        updateCateEditText.setInputType(InputType.TYPE_CLASS_TEXT|InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
                        updateCateEditText.setBackgroundColor(Color.parseColor("#FFF1F1"));
                        updateCateEditText.setTextColor(Color.BLACK);
                        updateCateEditText.setTextCursorDrawable(R.drawable.dialog_cursor_color);
                        InputFilter[] FilterArray = new InputFilter[1];
                        FilterArray[0] = new InputFilter.LengthFilter(10);
                        updateCateEditText.setFilters(FilterArray);
                        updateCateEditText.addTextChangedListener(new TextWatcher(){
                            public void onTextChanged(CharSequence s, int start, int before, int count)
                            {
                                if (updateCateEditText.getText().toString().length()>9)
                                {
                                    Toast.makeText(itemView.getContext(),"10자이상 입력할수 없습니다",Toast.LENGTH_SHORT).show();
                                }
                            }
                            public void beforeTextChanged(CharSequence s, int start, int count, int after){}
                            public void afterTextChanged(Editable s){}
                        });
                        updateCateEditText.setText(items.get(getAdapterPosition()).getCategoryName());
                        builder.setView(updateCateEditText);
                        builder.setTitle("카테고리 수정");
                        builder.setPositiveButton("수정", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {}
                        });

                        builder.setNeutralButton("돌아감", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        if(items.get(getAdapterPosition()).getId()>1){//카테고리 하나는 반드시 있어야 함, 수정은 가능
                            builder.setNegativeButton("삭제", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String inDeleteSql="delete from incomecategory where category_id="+items.get(getAdapterPosition()).getId();
                                    String exDeleteSql="delete from expensecategory where category_id="+items.get(getAdapterPosition()).getId();
                                    try {
                                        if(isExChecked){
                                            database.execSQL(exDeleteSql);
                                        }else{
                                            database.execSQL(inDeleteSql);
                                        }
                                        Toast.makeText(itemView.getContext(), "카테고리 삭제", Toast.LENGTH_SHORT).show();
                                        setCategoryName();
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                            });
                        }

                        final AlertDialog dialog= builder.create();
                        // 창 띄우기
                        dialog.show();
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {
                                Boolean wantToCloseDialog = false;
                                String inputRegCategory= updateCateEditText.getText().toString();
                                if (inputRegCategory.equals("")) {
                                    updateCateEditText.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            updateCateEditText.setFocusableInTouchMode(true);
                                            updateCateEditText.requestFocus();
                                        }
                                    });
                                    Toast.makeText(itemView.getContext(), "수정할 이름을 입력하세요", Toast.LENGTH_SHORT).show();
                                }else {
                                    String exCateUpdatesql="update expensecategory set expensecategory_name='"+
                                            updateCateEditText.getText().toString()+"' where category_id="+items.get(getAdapterPosition()).getId();
                                    String inCateUpdatesql="update incomecategory set incomecategory_name='"+
                                            updateCateEditText.getText().toString()+"' where category_id="+items.get(getAdapterPosition()).getId();
                                    try {
                                        if(isExChecked){
                                            database.execSQL(exCateUpdatesql);
                                        }else{
                                            database.execSQL(inCateUpdatesql);
                                        }
                                        Toast.makeText(itemView.getContext(), "카테고리수정완료", Toast.LENGTH_SHORT).show();
                                        setCategoryName();
                                        wantToCloseDialog = true;
                                    }catch (Exception e){
                                        e.printStackTrace();
                                    }
                                }
                                if(wantToCloseDialog)
                                    dialog.dismiss();

                            }
                        });
                    }//업데이트 카테고리 함수 끝


                });


            }

            public void setItem(UpdateSetting item) {
                itembutton.setText(item.getCategoryName());
            }


        }
    }

}