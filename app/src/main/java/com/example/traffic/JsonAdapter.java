package com.example.traffic;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by CAPTON on 2016/11/25.
 */
// 适配器，等待被JsonThread调用
public class JsonAdapter extends BaseAdapter {

    List<traffic> traffics;
    Context context;
    LayoutInflater inflater;
    Handler handler;

    public JsonAdapter(Context context,Handler handler,List<traffic> traffics) {
        this.handler=handler;
        this.context=context;
        this.traffics=traffics;
        inflater=LayoutInflater.from(context);//从MainActivity中上下文对象中获取LayoutInflater；所以说这个context,和handler对象很重要，贯穿整项目
    }

    @Override
    public int getCount() {
        return traffics.size();
    }

    @Override
    public Object getItem(int position) {
        return traffics.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    //重写getView方法，即设置ListView每一项的视图
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder=null;

        if(convertView==null){
            convertView=inflater.inflate(R.layout.picture,null);
            holder=new ViewHolder(convertView);
            convertView.setTag(holder);
        }else {
            holder= (ViewHolder) convertView.getTag();
        }
        holder.thetitle.setText(String.valueOf(traffics.get(position).description));
        holder.site.setText(traffics.get(position).site);
        new ImageThread(traffics.get(position).picture, handler,holder.image).start();//开启新线程下载图片并在新线程中更新UI，所以要传递handler对象
        return convertView;
    }

    //用于暂时保存视图对象
    class ViewHolder{
        public TextView thetitle;
        public TextView site;
        public ImageView image;

        public ViewHolder(View view){
            thetitle = (TextView) view.findViewById(R.id.thetitle);
            site = (TextView) view.findViewById(R.id.site);
            image = (ImageView) view.findViewById(R.id.image);
        }
    }
}
