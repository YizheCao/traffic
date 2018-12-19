package com.example.traffic;

import android.content.Context;
import android.os.Handler;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class JsonThread extends Thread {
    Context context;
    ListView listView;
    String url;
    Handler handler;//关键参数 整个小项目中的核心之一，会在JsonThread和JsonAdapter，ImageThread中传递，用于更新UI界面
    List<traffic> traffics;
    JsonAdapter jsonAdapter;

    public JsonThread(Context context, ListView listView, String url,Handler handler ) {
        this.context=context;
        this.listView=listView;
        this.url=url;
        this.handler=handler;
    }

    private  List<traffic> getTraffics(String data){
        List<traffic> traffics=new ArrayList<traffic>();
        try {
            JSONObject object=new JSONObject(data);
            System.out.println(object);
            JSONArray jsonArray=object.getJSONArray("traffics");
            for(int i=0;i<jsonArray.length();i++){
                JSONObject studentObject= (JSONObject) jsonArray.get(i);
                traffic tr=new traffic();
                tr.picture=studentObject.optString("picture");
                tr.description=studentObject.optString("description");
                tr.site=studentObject.optString("site");
                traffics.add(tr);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return  traffics;
    }

    @Override
    public void run() {
        //从网络中获取数据，转换为String类型
        StringBuffer result=new StringBuffer();
        try {
            URL Url=new URL(url);
            HttpURLConnection connection= (HttpURLConnection) Url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            InputStream inputStream=connection.getInputStream();
            BufferedReader bufferedReader=new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line=bufferedReader.readLine())!=null){
                result.append(line);
            }
            traffics=getTraffics(result.toString());//调用解析方法
            System.out.println(traffics);
            inputStream.close();
            bufferedReader.close();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    jsonAdapter=new JsonAdapter(context,handler,traffics); //传递关键参数MainActivity上下文对象context，MainActivity主线程的handler对象,处理好的List<Student>对象
                    listView.setAdapter(jsonAdapter);
                }
            });
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.run();
    }
}
