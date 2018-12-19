package com.example.traffic;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class upload extends AppCompatActivity {
    Button photo;
    Button upload;
    EditText des;
    String filepath;
    String description;
    String site;
    String fileName;
    Context context = this;
    boolean flag;
    String imagePath;
    String savePath = "";
    JSONParser jsonParser = new JSONParser();
    private String actionUrl = "http://192.168.43.92:80/traffic/client/upload.php";
    private String insertUrl = "http://192.168.43.92:80/traffic/client/insert.php";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        photo = (Button) findViewById(R.id.Photo);
        upload = (Button) findViewById(R.id.Upload);
        des = (EditText) findViewById(R.id.Description);

        site = getLngAndLat(context);
        filepath = null;

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (PermissionUtils.isCameraPermission(upload.this, 0x007)){
                    takePhoto();
                }
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile(actionUrl);
                new CreateNewProduct().execute();
            }
        });
    }

    public void takePhoto() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
            StrictMode.setVmPolicy(builder.build());
        }

        String storageState = Environment.getExternalStorageState();
        if (storageState.equals(Environment.MEDIA_MOUNTED)) {
            savePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/camera/";
            File savedir = new File(savePath);
            if (!savedir.exists()) {
                savedir.mkdirs();
            }
        }

        // 没有挂载SD卡，无法保存文件
        if (savePath == null || "".equals(savePath)) {
            System.out.println("无法保存照片，请检查SD卡是否挂载");
            return;
        }

        String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        //照片命名
        fileName = timeStamp + ".jpg";
        File out = new File(savePath, fileName);
        Uri uri = Uri.fromFile(out);
        //该照片的绝对路径
        imagePath = savePath + fileName;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        startActivityForResult(intent, 0x008);
    }

    //请求权限回调
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 0x007:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhoto();
                } else {
                    Toast.makeText(this, "拍照权限被拒绝", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        initPermission();//针对6.0以上版本做权限适配
    }

    private void initPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            //检查权限
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //请求权限
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            } else {
                flag = true;
            }
        } else {
            flag = true;
        }
    }

    class CreateNewProduct extends AsyncTask<String, String, String> {
        protected String doInBackground(String... args) {
            try{
                description = des.getText().toString();
                List<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("site", site));
                params.add(new BasicNameValuePair("img", fileName));
                params.add(new BasicNameValuePair("description", description));

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(insertUrl);
                httpPost.addHeader("Content-Type", "application/x-www-form-urlencoded");
                HttpEntity entity = new UrlEncodedFormEntity(params, "UTF_8");
                httpPost.setEntity(entity);
                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity responseEntity = response.getEntity();

                Log.i("~peter", EntityUtils.toString(responseEntity, "utf-8"));
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    /* 上传文件至Server，uploadUrl：接收文件的处理页面 */
    private void uploadFile(String uploadUrl) {
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        String end = "\r\n";
        String twoHyphens = "--";
        String boundary = "******";
        description = des.getText().toString();
        try {
            URL url = new URL(uploadUrl);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url
                    .openConnection();
            // 设置每次传输的流大小，可以有效防止手机因为内存不足崩溃
            // 此方法用于在预先不知道内容长度时启用没有进行内部缓冲的 HTTP 请求正文的流。
            httpURLConnection.setChunkedStreamingMode(128 * 1024);// 128K
            // 允许输入输出流
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Connection", "Keep-Alive");
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);

            DataOutputStream dos = new DataOutputStream(httpURLConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + end);
            dos.writeBytes("Content-Disposition: form-data; name=\"img\"; filename=\""
                    + imagePath.substring(imagePath.lastIndexOf("/") + 1)
                    + "\""
                    + end);
            dos.writeBytes(end);

            FileInputStream fis = new FileInputStream(imagePath);
            byte[] buffer = new byte[8192]; // 8k
            int count = 0;
            // 读取文件
            while ((count = fis.read(buffer)) != -1) {
                dos.write(buffer, 0, count);
            }
            fis.close();

            dos.writeBytes(end);
            dos.writeBytes(twoHyphens + boundary + end);

            dos.writeBytes(twoHyphens + boundary + twoHyphens + end);
            dos.flush();

            //接收服务器返回信息
            InputStream is = httpURLConnection.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "utf-8");
            BufferedReader br = new BufferedReader(isr);
            String result = br.readLine();

            Toast.makeText(this, result, Toast.LENGTH_LONG).show();
            dos.close();
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            setTitle(e.getMessage());
        }
    }

    /**
     * 获取经纬度
     *
     * @param context
     * @return
     */
    private String getLngAndLat(Context context) {
        initPermission();

        double latitude = 0.0;
        double longitude = 0.0;
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER); //从gps获取经纬度
        if(flag) {
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            latitude = location.getLatitude();
            longitude = location.getLongitude();
        }
        return longitude + "," + latitude;
    }
}
