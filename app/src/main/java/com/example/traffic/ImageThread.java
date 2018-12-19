package com.example.traffic;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.widget.ImageView;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by CAPTON on 2016/11/25.
 */
//根据url读取图片数据，把图片Bitmap保存下来等待 JsonAdapter调用
public class ImageThread extends Thread {
    String url;
    ImageView imageView;
    Handler handler;
    public ImageThread(String url, Handler handler,ImageView imageView) {
        this.url = url;
        this.imageView=imageView;
        this.handler=handler;
    }

    @Override
    public void run() {
        try {
            //final Bitmap bitmap= BitmapFactory.decodeStream(new URL(url).openStream());//简单粗暴，可能有问题，自己做的时候注意
            final Bitmap bitmap;
            URL imageUrl=new URL("http://192.168.43.92:80/traffic/store/" + url);
            HttpURLConnection conn=(HttpURLConnection)imageUrl.openConnection();
            conn.connect();
            InputStream is=conn.getInputStream();
            bitmap=BitmapFactory.decodeStream(is);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    imageView.setImageBitmap(bitmap);
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        super.run();
    }
}
