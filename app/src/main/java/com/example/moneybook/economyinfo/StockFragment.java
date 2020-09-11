package com.example.moneybook.economyinfo;

import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.moneybook.R;

public class StockFragment extends Fragment {
    static RequestQueue requestQueue;
    TextView kospiTextView,kosdaqTextView;
    TextView spiDaytopTV,spiDayBottomTV,spiDayTradeTV,spiTMoneyTV;
    TextView sdaqDaytopTV,sdaqDayBottomTV,sdaqDayTradeTV,sdaqTMoneyTV;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup)inflater.inflate(R.layout.fragment_stock, container, false);
        if(requestQueue == null){
            requestQueue = Volley.newRequestQueue(getContext());
        }
        kosdaqTextView=view.findViewById(R.id.KOSDAQTextView);
        kospiTextView=view.findViewById(R.id.kospiTextview);

        spiDaytopTV=view.findViewById(R.id.kospiDayTOPtextView);
        spiDayBottomTV=view.findViewById(R.id.kospiDayBottomtextView);
        spiDayTradeTV=view.findViewById(R.id.kospiDayTradetextView);
        spiTMoneyTV=view.findViewById(R.id.kospiDayTMoneytextView);
        sdaqDaytopTV=view.findViewById(R.id.kosdaqDayTOPtextView);
        sdaqDayBottomTV=view.findViewById(R.id.kosdaqDayBottomtextView);
        sdaqDayTradeTV=view.findViewById(R.id.kosdaqDayTradetextView);
        sdaqTMoneyTV=view.findViewById(R.id.kosdaqDayTMoneytextView);
        sendStockRequest();
        return view;
    }

    private void sendStockRequest() {
        String urlKospiStr="http://ds.gscms.co.kr:8888/Rest/StockIndex/KOSPI?type=xml&sessionID=test";
        String urlKOSDAQStr="http://ds.gscms.co.kr:8888/Rest/StockIndex/KOSDAQ?type=xml&sessionID=test";

        StringRequest request = new StringRequest(
                Request.Method.GET,
                urlKospiStr,
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
                        Log.d("에러났다으아", "에러: " + error.getMessage());
                    }
                }
        );
        //위에서 만든 request객체를 큐에 추가해준다


        request.setShouldCache(false);//같은요청이 들어와도 계속 요청함
        requestQueue.add(request);

        StringRequest requestkosdaq = new StringRequest(
                Request.Method.GET,
                urlKOSDAQStr,
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
                        Log.d("에러났다으아", "에러: " + error.getMessage());
                    }
                }
        );
        requestkosdaq.setShouldCache(false);//같은요청이 들어와도 계속 요청함
        requestQueue.add(requestkosdaq);
    }

    private void processResponse(String response) {
        String confirmkospi="KOSPI";
        String stockdata=response.substring(response.indexOf("<지수정보>"),response.indexOf("</지수등락구분>"));
        String finalStockResult=setDataResult(stockdata);
        String uandDPercent=stockdata.substring((stockdata.indexOf("율")+3),stockdata.indexOf("</지수등락율"));
        if(response.indexOf(confirmkospi)!= -1){//코스피라는 단어가 있을때
            //kospiTextView.setText("KOSPI \n"+finalStockResult);
            if (uandDPercent.indexOf("-")==-1) {//상승이면
                //kospiTextView.setTextColor(Color.RED);
                kospiTextView.setText(Html.fromHtml("KOSPI<br><font color=\"#FF0000\">"
                                +finalStockResult+"</font>"));
            }else {
                //kospiTextView.setTextColor(Color.BLUE);
                kospiTextView.setText(Html.fromHtml("KOSPI<br><font color=\"#0000FF\">"
                        +finalStockResult+"</font>"));
            }
        }else {
            //kosdaqTextView.setText("KOSDAQ \n"+finalStockResult);
            if (uandDPercent.indexOf("-")==-1) {//상승이면
                //kosdaqTextView.setTextColor(Color.RED);
                kosdaqTextView.setText(Html.fromHtml("KOSDAQ<br><font color=\"#FF0000\">"
                        +finalStockResult+"</font>"));
            }else {
                //kosdaqTextView.setTextColor(Color.BLUE);
                kosdaqTextView.setText(Html.fromHtml("KOSDAQ<br><font color=\"#0000FF\">"
                        +finalStockResult+"</font>"));
            }
        }
        setStockOtherData(response);
    }

    //나머지 주식정보 채우는 곳
    private void setStockOtherData(String response) {
        String topstr=response.substring((response.indexOf("최고")+3),response.indexOf("</장중최고>"));
        String bottomstr=response.substring((response.indexOf("최저")+3),response.indexOf("</장중최저>"));
        String tradestr=response.substring((response.indexOf("천주")+3),response.indexOf("</거래량천주>"));
        String tmoneystr=response.substring((response.indexOf("백만")+3),response.indexOf("</거래대금백만>"));
        String confirmkospi="KOSPI";
        if(response.indexOf(confirmkospi)!= -1){
            spiDaytopTV.setText(topstr);
            spiDayBottomTV.setText(bottomstr);
            spiDayTradeTV.setText(tradestr+" 천주");
            spiTMoneyTV.setText(tmoneystr+" 백만");
        }else {
            sdaqDaytopTV.setText(topstr);
            sdaqDayBottomTV.setText(bottomstr);
            sdaqDayTradeTV.setText(tradestr+" 천주");
            sdaqTMoneyTV.setText(tmoneystr+" 백만");
        }
    }

    private String setDataResult(String stockdata){
        String stockScore=stockdata.substring(6,stockdata.indexOf("</지수정보"));
        String uandDScore=stockdata.substring((stockdata.indexOf("수치")+3),stockdata.indexOf("</지수등락"));
        String uandDPercent=stockdata.substring((stockdata.indexOf("율")+3),stockdata.indexOf("</지수등락율"));
        String setDataResult="";
        if (uandDPercent.indexOf("-")==-1) {//상승이면
            setDataResult=stockScore+"<br>"+"▲"+uandDScore+"<br>"+uandDPercent+"<br>";
        }else {
            setDataResult=stockScore+"<br>"+"▼"+uandDScore+"<br>"+uandDPercent+"<br>";
        }
        return setDataResult;
    }
}