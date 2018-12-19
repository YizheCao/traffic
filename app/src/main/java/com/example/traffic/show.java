package com.example.traffic;

import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class show extends AppCompatActivity {
    Context context=this;
    ListView listView;
    Handler handler;
    JsonAdapter jsonAdapter;
    String url="http://192.168.43.92:80/traffic/client/show.php";//这个url随你设置的php页面而变动。

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show);

        listView=(ListView)findViewById(R.id.thelist);
        handler=new Handler();//获得一个handler对象，为后面的各个线程提供处理UI的依据
        new JsonThread(context, listView, url,handler).start();
    }
}
