package com.example.moneybook.economyinfo;

import android.app.ProgressDialog;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.moneybook.R;
import com.google.gson.Gson;

import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Locale;

public class ExchangeRateFragment extends Fragment {
    Handler handler = new Handler();
    ProgressDialog progressDialog;
    TextView USARateTextView,EuroTextView,ChinaTextView,EnglandTextView,JapanTextView,workingDayTextView,exchangeRateTitle;
    //ArrayList<String> list=new ArrayList<>();
    String result;
    static RequestQueue requestQueue;
    NumberFormat numberFormat;
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_exchange_rate, container, false);

        numberFormat =NumberFormat.getInstance(Locale.getDefault());

        USARateTextView = view.findViewById(R.id.USARateTextView);
        EuroTextView = view.findViewById(R.id.EuroRateTextView);
        ChinaTextView = view.findViewById(R.id.ChinaRateTextView);
        EnglandTextView = view.findViewById(R.id.EnglandRateTextView);
        JapanTextView = view.findViewById(R.id.JapanRaTetextView);
        workingDayTextView = view.findViewById(R.id.workingDayTextView);
        exchangeRateTitle = view.findViewById(R.id.exchangeRateTitle);
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getContext());
        }
        minusdays[0]=0;
        sendRequest();


        return view;
    }//onCreateView끝



    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendRequest() {
        progressDialog = ProgressDialog.show(getContext(),"ing...","환율정보 가져오는 중...",true,true);
        //요청 url을 받아옴
        LocalDate today = LocalDate.now();
        findworkingDay=today;
            String date="";
            String monthStr=today.getMonthValue()+"";
                if (monthStr.length()==1){
                    monthStr="0"+monthStr;
                }
                String dayStr=today.getDayOfMonth()+"";
                if (dayStr.length()==1){
                    dayStr="0"+dayStr;
                }
                date=today.getYear()+monthStr+dayStr;
            String urlStr="http://ds.gscms.co.kr:8888/Rest/ExchangeRates/081?type=json&sessionID=test&date="+date;

        //문자열 request객체를 생성 //문자열 요청을 함
        //인자: 요청방식, 주소, 응답리스터, 에러리스너
            StringRequest request = new StringRequest(
                    Request.Method.GET,
                    urlStr,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            //응답이 왔을때 실행할 내용
                            processResponse(response);
                        }

                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //에러가 왔을 때 실행할 내용
                            //에러를 화면에 뿌려준다
                            Log.d("응답에러", "에러: " + error.getMessage());
                        }
                    }
            );
            //위에서 만든 request객체를 큐에 추가해준다


        request.setShouldCache(false);//같은요청이 들어와도 계속 요청함
        requestQueue.add(request); //볼리가 알아서 스레드를 써

    }
//
    LocalDate findworkingDay; long[] minusdays = {0};
//    //json을 자바객체로 변환해주는 메서드
    @RequiresApi(api = Build.VERSION_CODES.O)
    private void processResponse(String response) {
        result=response;
        repeatFindWorkingDay(response);
    }

    public void realDataProcess(String response){
        Gson gson = new Gson();
        //제이슨으로 받은 내용을 ExchangeRates 변경하여 ExchangeRates 반환함
        ExchnageRateResult exchnageRateResult = gson.fromJson(response, ExchnageRateResult.class);//제이슨으로 받은 놈을 ExchangeRates로 변경해서 반환
        if(exchnageRateResult.ExchangeRates.Row!=null) {
            String s = "" + exchnageRateResult.ExchangeRates.Row.size();
            ArrayList<ExRate> exratelist = exchnageRateResult.ExchangeRates.Row;
            for (ExRate result : exratelist) {
                if (result.국명.equals("미국")) {
                    USARateTextView.setText(numberFormat.format(Double.parseDouble(result.매매기준율)) + "");
                }
                if (result.국명.equals("영국")) {
                    EnglandTextView.setText(numberFormat.format(Double.parseDouble(result.매매기준율)) + "");
                }
                if (result.국명.equals("일본")) {
                    JapanTextView.setText(numberFormat.format(Double.parseDouble(result.매매기준율)) + "");
                }
                if (result.국명.equals("중국")) {
                    ChinaTextView.setText(numberFormat.format(Double.parseDouble(result.매매기준율)) + "");
                }
                if (result.국명.equals("유로")) {
                    EuroTextView.setText(numberFormat.format(Double.parseDouble(result.매매기준율)) + "");
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void repeatFindWorkingDay(String response){
        int test= response.indexOf("\"@count\":\"0\"");
        if(test!=-1){
            minusdays[0]++;
            findworkingDay=LocalDate.now().minusDays(minusdays[0]);
            String monthStr=findworkingDay.getMonthValue()+"";
            if (monthStr.length()==1){
                monthStr="0"+monthStr;
            }
            String dayStr=findworkingDay.getDayOfMonth()+"";
            if (dayStr.length()==1){
                dayStr="0"+dayStr;
            }
            String date=findworkingDay.getYear()+monthStr+dayStr;
            String urlStr="http://ds.gscms.co.kr:8888/Rest/ExchangeRates/081?type=json&sessionID=test&date="+date;
            StringRequest request = new StringRequest(
                    Request.Method.GET,
                    urlStr,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            repeatFindWorkingDay(response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            //에러가 왔을 때 실행할 내용
                            //에러를 화면에 뿌려준다
                            Log.d("응답에러", "에러: " + error.getMessage());
                        }
                    }
            );
            request.setShouldCache(false);//같은요청이 들어와도 계속 요청함
            requestQueue.add(request); //볼리가 알아서 스레드를 써
        }else {
            workingDayTextView.append("\n"+findworkingDay.toString()+"일의 매매기준율");
            realDataProcess(response);
            progressDialog.dismiss();
        }
    }
}