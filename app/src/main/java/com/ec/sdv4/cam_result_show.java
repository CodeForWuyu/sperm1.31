package com.ec.sdv4;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static android.webkit.ConsoleMessage.MessageLevel.LOG;

public class cam_result_show extends AppCompatActivity {


    private TextView showEnergyRate;
    private TextView showDefomityRate;
    private TextView showDensity;

    private ImageView imageView;
    private int ivWidth;
    private int ivHeight;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_result);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//隐藏输入法

        showDensity = (TextView)findViewById(R.id.density);
        showEnergyRate = (TextView)findViewById(R.id.energyRate);
        showDefomityRate = (TextView)findViewById(R.id.defomity);
        imageView = (ImageView)findViewById(R.id.ImageShow);
        imageView.post(new Runnable() {
            @Override
            public void run() {
                ivWidth = imageView.getWidth();
                ivHeight = imageView.getHeight();
                Log.i("ivWidth",String.valueOf(ivWidth));
                Log.i("ivHeight",String.valueOf(ivHeight));
            }
        });




        Intent intent = getIntent();
        String spermTotalNum = intent.getStringExtra("spermTotalNum");
        String energyRate = intent.getStringExtra("energyRate");
        String density = intent.getStringExtra("density");
        String picname = intent.getStringExtra("picture_name");
        String defomity = intent.getStringExtra("defomityRate");

        showEnergyRate.setText(energyRate);
        showDensity.setText(density);
        showDefomityRate.setText(defomity);

        Bitmap bitmap=getBitmapByName(picname);
        if(bitmap!=null){
            imageView.setImageBitmap(bitmap);
        }

        Button save  = (Button)findViewById(R.id.save);
        Button cancel = (Button)findViewById(R.id.cancel);

        save.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(cam_result_show.this,MainActivity.class);
                startActivity(intent);
            }
        });

        cancel.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent = new Intent(cam_result_show.this,MainActivity.class);
                startActivity(intent);
            }
        });
    }

    /**
     * 获取文件中的bitmap
     * @param name
     * @return
     */
    private Bitmap getBitmapByName(String name){
        Log.i("getBitmaoByName","test here");
        int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {
            //Toast.makeText(this,"请在设置中打开写内存权限！",Toast.LENGTH_SHORT).show();
            return null;
        }
        String DIR =  Environment.getExternalStorageDirectory().getAbsolutePath() +"/SpermDetection/picture";
        try{
            String pathName = DIR+"/"+name;
            FileInputStream fis = new FileInputStream(pathName);
            Bitmap bitmap = adjustBitmap(pathName);
            return bitmap;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private Bitmap adjustBitmap(String path){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path,options);
        int raw_width = options.outWidth;
        int raw_height = options.outHeight;
        int max = Math.max(raw_width, raw_height);
        int newWidth = raw_width;
        int newHeight = raw_height;
        int inSampleSize = 1;
        int max_size = 200;
        if(max > max_size) {
            newWidth = raw_width / 2;
            newHeight = raw_height / 2;
            while((newWidth/inSampleSize) > max_size || (newHeight/inSampleSize) > max_size) {
                inSampleSize *=2;
            }
        }
        options.inSampleSize = inSampleSize;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(path, options);
    }

    /**
     * 获取合适大小bitmap
     * @param pathName
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    public static Bitmap decodeSampledBitmapFromFile(String pathName, int reqWidth, int reqHeight) {
        Log.i("decodeSampledBitmap","test here");
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(pathName, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        Bitmap src = BitmapFactory.decodeFile(pathName, options);
        return createScaleBitmap(src, reqWidth, reqHeight);
    }
    /**
     * 计算压缩比
     * @param options
     * @param reqWidth
     * @param reqHeight
     * @return
     */
    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        Log.i("calculateInSampleSize","test here");
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;
            while ((halfHeight / inSampleSize) > reqHeight && (halfWidth / inSampleSize) > reqWidth) {
                inSampleSize *= 2;
            }
        }
        Log.i("inSampleSize",String.valueOf(inSampleSize));
        return inSampleSize;
    }

    /**
     * 获得适当大小bitmap
     * @param src
     * @param dstWidth
     * @param dstHeight
     * @return
     */
    private static Bitmap createScaleBitmap(Bitmap src, int dstWidth, int dstHeight) {
        Log.i("createScaleBitmap","test here");
        Bitmap dst = Bitmap.createScaledBitmap(src, dstWidth, dstHeight, false);
        if (src != dst) { // 如果没有缩放，那么不回收
            src.recycle(); // 释放Bitmap的native像素数组
        }
        return dst;
    }
}


