package com.ec.sdv4;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EdgeEffect;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class cam_pro_activity2 extends Activity implements View.OnClickListener {

    private TextureView textureView;
    private Camera mCamera;
    private boolean isPreview = false;
    private SurfaceTexture mSurfaceTexture;
    private Button start;
    private ProgressBar progressBar;
    private Boolean isStartDetect = false;
    private int frameNumber =0;
    private AutoFocusCallback mAutoFocusCallback = new AutoFocusCallback();
    public Bitmap bitmap;
    public Mat mat;
    public Mat[] matArray = new Mat[100];

    private SeekBar mZoomBar = null;
    private EditText defomityRate;
    private TextView setDefomity;
    public camSpermDetection sd= new camSpermDetection();

    /**
     *更新ui的线程
     * @param savedInstanceState
     */
    private Handler mUIHandler = new Handler(){

        @Override
        public void handleMessage(Message msg) {
            Log.e("更新ui",String.valueOf(msg.what));
            progressBar.setVisibility(View.VISIBLE);
            progressBar.setProgress(msg.what);
            if(msg.what==progressBar.getMax()-1){
                showResult();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_pro2);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);//隐藏输入法

        //导入opencv
        staticLoadCVLibraries();
        initView();
        //添加ZoomBar
        mZoomBar = (SeekBar)findViewById(R.id.seekbar_zoom);
        mZoomBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
            }
            public void onProgressChanged(SeekBar seekBar, int progress,
                                          boolean fromUser) {
                // TODO Auto-generated method stub
                Camera.Parameters p = mCamera.getParameters();
                Log.i("此时的progress",String.valueOf(progress));
                p.setZoom(progress);
                mCamera.setParameters(p);
            }
        });
    }

    /**
     * 该callback运行于子线程
     * 调用检测函数并通知ui进行更新
     */
    class ChildCallback implements Handler.Callback {
        @Override
        public boolean handleMessage(Message msg) {
            //进行检测
            doDetect(msg.what);
            //通知主线程去更新UI
            Message msg1 = new Message();
            msg1.what = msg.what;
            mUIHandler.sendMessage(msg1);
            return false;
        }
    }

    /**
     * OpenCV库静态加载并初始化
     */
    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i("CV", "Open CV Libraries loaded...");
        }
    }

    public final class AutoFocusCallback implements
            android.hardware.Camera.AutoFocusCallback {
        public void onAutoFocus(boolean focused, Camera camera) {

        }
    };

    /**
     * ui 初始化
     */
    private void initView() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常亮
        textureView = (TextureView) this.findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(new MySurfaceTextureListener());
        start = (Button) this.findViewById(R.id.start);
        start.setOnClickListener(this);
        setDefomity = (TextView)findViewById(R.id.setDeformityrate);
        setDefomity.setBackgroundColor(Color.argb(0, 0, 255, 0));
        setDefomity.setOnClickListener(this);

        defomityRate = (EditText)findViewById(R.id.Deformityrate);
        defomityRate.setBackgroundColor(Color.argb(0, 0, 255, 0));
        defomityRate.setVisibility(View.GONE);

        progressBar = (ProgressBar)findViewById(R.id.progressbar2);
        progressBar.setVisibility(ProgressBar.GONE);
    }

    /**
     * camera 初始化
     */
    public void initCamera(){
        if(!isPreview && null != mSurfaceTexture){
            mCamera = Camera.open();
            mCamera.setDisplayOrientation(90);
        }
        Camera.Parameters parameters = mCamera.getParameters();
        Camera.Size mSize = parameters.getSupportedPreviewSizes().get(0);
        parameters.setPreviewSize(mSize.width, mSize.height);
        parameters.setPreviewFpsRange(4, 10);
        parameters.setPictureFormat(ImageFormat.JPEG);
        parameters.setJpegQuality(80);
        parameters.setPictureSize(mSize.width, mSize.height);
        //parameters.setFocusMode(Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        try {
            mCamera.setPreviewTexture(mSurfaceTexture);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        //mCamera.setParameters(parameters);
        mCamera.startPreview();
        isPreview = true;
    }

    /**
     * textureview 方法
     */
    private final class MySurfaceTextureListener implements TextureView.SurfaceTextureListener {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface,
                                              int width, int height) {
            mSurfaceTexture = surface;
            initCamera();
        }
        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            if(null != mCamera){
                if(isPreview){
                    mCamera.stopPreview();
                }
                mCamera.release();
                mCamera = null;
            }
            return true;
        }
        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface,
                                                int width, int height) {

        }

        //================获取检测mat=======================
        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // TODO Auto-generated method
            //自动对焦
            Log.i("帧数",String.valueOf(sd.frameNumber));
            //mCamera.autoFocus(mAutoFocusCallback);
            //创建异步HandlerThread
            HandlerThread handlerThread = new HandlerThread("downloadImage");
            //必须先开启线程
            handlerThread.start();
            //子线程Handler
            Handler childHandler = new Handler(handlerThread.getLooper(),new ChildCallback());

            if(sd.frameNumber==100){
                sd.frameNumber++;
                isStartDetect = false;
            }
            if(isStartDetect){
                mZoomBar.setVisibility(View.GONE);
                bitmap = textureView.getBitmap();
                try{
                    matArray[sd.frameNumber] = new Mat();
                    Utils.bitmapToMat(bitmap,matArray[sd.frameNumber]);
                    Log.i("已存储的mat",String.valueOf(sd.frameNumber));
                }catch (Exception e){
                    e.printStackTrace();
                    Log.e("失败","why???");

                }
                childHandler.sendEmptyMessage(sd.frameNumber);
                sd.frameNumber+=1;
            }
        }
    }


    /**
     * 检测
     */
    public void doDetect(int i){
        try {
            if(i==0){
                sd.getthisMat(matArray[i],i);
                sd.getTotalNum(i);
            } else {
                    sd.getpreMat();
                    sd.getthisMat(matArray[i],i);
                    sd.getTotalNum(i);
                    sd.getDiff(i);
                }
            if(i==99){
                sd.getEnergyRate();
                sd.getDensity();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示结果 go to camera result
     */
    private void showResult(){
        Utils.matToBitmap(matArray[99],bitmap);

        SimpleDateFormat dfname = new SimpleDateFormat("yyyy_MM_dd_HH_mm");//设置日期格式
        String picname = dfname.format(new Date())+".jpg";// new Date()为获取当前系统时间，也可使用当前时间戳
        saveBitmap(bitmap,picname);

        //获得数据 并修改格式
        String spermTotalNum = String.valueOf(sd.finalTotalNum);
        DecimalFormat df = new DecimalFormat("##.00%");//百分数保留两位小数
        DecimalFormat df1 = new DecimalFormat("0.00");//保留两位小数
        String energyRate = df.format(sd.energyRate);
        String density = df1.format(sd.density);
        //向下个活动传递
        Intent intent = new Intent(cam_pro_activity2.this, cam_result_show.class);
        if(energyRate.equals(".00%")){
            energyRate = "0.00%";
        }
        if(density.equals(".00%")){
            density = "0.00%";
        }
        Log.e("energyRate",energyRate);
        Log.e("totalNum",spermTotalNum);

        String defomityrate = String.valueOf(defomityRate.getText());

        intent.putExtra("energyRate", energyRate);
        intent.putExtra("density",density);
        intent.putExtra("spermTotalNum", spermTotalNum);
        intent.putExtra("picture_name",picname);
        intent.putExtra("defomityRate",defomityrate);

        startActivity(intent);
    }

    /**
     * 保存图片到指定文件夹
     *
     * @param mybitmap
     * @param name
     * @return
     */
    private boolean saveBitmap(Bitmap mybitmap,String name){
        int checkSelfPermission = checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (checkSelfPermission == PackageManager.PERMISSION_DENIED) {

            Toast.makeText(this,"请在设置中打开写内存权限！",Toast.LENGTH_SHORT).show();
            return false;
        }
        String DIR =  Environment.getExternalStorageDirectory().getAbsolutePath() +"/SpermDetection/picture";
        File localFile = new File(DIR);
        if (!localFile.exists()) {
            localFile.mkdirs();
        }
        Log.i("savebitmap","right here");
        boolean result = false;
        //创建位图保存目录
        File file = new File(localFile+"/"+name);
        FileOutputStream fileOutputStream = null;
        try{
            fileOutputStream = new FileOutputStream(file);
            mybitmap.compress(Bitmap.CompressFormat.JPEG,100,fileOutputStream);
            fileOutputStream.flush();
            fileOutputStream.close();
            //update gallery
            Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            Uri uri = Uri.fromFile(file);
            intent.setData(uri);
            this.sendBroadcast(intent);
            Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
            result = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return result;
    }

    /**
     * onclick
     * @param v
     */
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.start:
                isStartDetect = true;
                //Toast.makeText(this,"开始检测",Toast.LENGTH_SHORT).show();
                break;
            case R.id.setDeformityrate:
                setDefomity.setVisibility(View.GONE);
                defomityRate.setVisibility(View.VISIBLE);
                break;
            default:

                break;
        }
    }
}
