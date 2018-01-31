package com.ec.sdv4;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.text.DecimalFormat;
import java.util.Timer;

public class cam_proc_activity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2{

    private JavaCameraView camera_view;
    private int M_REQUEST_CODE = 203;
    private String[] permissions = {Manifest.permission.CAMERA};
    private boolean startCameraflag = false;
    //    private boolean showResult = false;
    private int SpermDetectionFrameNumber = 0;
    private Mat mRgba;
    Timer timer = new Timer();  //计时类
    private ProgressBar progressBar;

    public camSpermDetection spermDetection= new camSpermDetection();;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //导入opencv
        staticLoadCVLibraries();

        Window window = getWindow();
        //隐藏标题栏
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //隐藏状态栏
        //定义全屏参数
        int flag= WindowManager.LayoutParams.FLAG_FULLSCREEN;
        //设置当前窗体为全屏显示
        window.setFlags(flag, flag);
        setContentView(R.layout.activity_cam_proc);
        progressBar = (ProgressBar)findViewById(R.id.bar);
        camera_view = (JavaCameraView) findViewById(R.id.cameraShow);
        int width = camera_view.getWidth();
        int height = camera_view.getHeight();
        Log.e("width",String.valueOf(width));
        Log.e("height",String.valueOf(height));


        camera_view.setCameraIndex(CameraBridgeViewBase.CAMERA_ID_BACK);
        Button startDetect = (Button)findViewById(R.id.turnOn);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);//屏幕常亮

        progressBar.setVisibility(ProgressBar.GONE);//隐藏进度条
        startDetect.setOnClickListener(new Button.OnClickListener(){
            @Override
            public void onClick(View v){
                startCameraflag = true;
//                cdTimer.start();
                progressBar.setVisibility(ProgressBar.VISIBLE);//显示进度条
                progressBar.setProgress(0);
                Toast.makeText(getApplicationContext(),R.string.toaststart,Toast.LENGTH_SHORT).show();
            }
        });
        RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(1080,720);
        params.leftMargin = 0;
        params.topMargin = 0;
        camera_view.setCvCameraViewListener(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(permissions, M_REQUEST_CODE);
        }
    }

    //----------------opencv库--------------------------------
    //OpenCV库静态加载并初始化
    private void staticLoadCVLibraries(){
        boolean load = OpenCVLoader.initDebug();
        if(load) {
            Log.i("CV", "Open CV Libraries loaded...");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera_view != null) {
            camera_view.disableView();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (OpenCVLoader.initDebug()) {
            camera_view.enableView();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (camera_view != null) {
            camera_view.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height,width, CvType.CV_8UC4);
    }

    @Override
    public void onCameraViewStopped() {
    }

    //====================算法================================================
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mRgba = inputFrame.rgba();
        //检测核心算法
        if(mRgba!=null&&startCameraflag==true&&spermDetection.frameNumber <=100){
            if(spermDetection.frameNumber ==0 ){
                spermDetection.getthisMat(mRgba,spermDetection.frameNumber);
                spermDetection.getTotalNum(spermDetection.frameNumber);
                spermDetection.frameNumber++;
                progressBar.setProgress(spermDetection.frameNumber);
            }else{
                spermDetection.getpreMat();
                spermDetection.getthisMat(mRgba,spermDetection.frameNumber);
                spermDetection.getTotalNum(spermDetection.frameNumber);
                spermDetection.getDiff(spermDetection.frameNumber);
                spermDetection.frameNumber++;
                Log.e("FrameNumber",String.valueOf(spermDetection.frameNumber));
                progressBar.setProgress(spermDetection.frameNumber);//进度条更新
            }
        }
        if(spermDetection.frameNumber >=100) {
            spermDetection.getEnergyRate();
            spermDetection.getDensity();
            //获得数据 并修改格式
            String spermTotalNum = String.valueOf(spermDetection.finalTotalNum);
            DecimalFormat df = new DecimalFormat("##.00%");//百分数保留两位小数
            DecimalFormat df1 = new DecimalFormat("0.00");//保留两位小数
            String energyRate = df.format(spermDetection.energyRate);
            String density = df1.format(spermDetection.density);
            //向下个活动传递
            Intent intent = new Intent(cam_proc_activity.this, cam_result_show.class);
            if(energyRate.equals(".00%")){
                energyRate = "0.00%";
            }
            if(density.equals(".00%")){
                density = "0.00%";
            }
            Log.e("energyRate",energyRate);
            intent.putExtra("energyRate", energyRate);
            intent.putExtra("density",density);
            intent.putExtra("spermTotalNum", spermTotalNum);
            startActivity(intent);
        }
//        if(spermDetection.frameNumber>=2&&startCameraflag==true){
//            return spermDetection.diffMatWithColor;
//        }
        return mRgba;
    }
}

