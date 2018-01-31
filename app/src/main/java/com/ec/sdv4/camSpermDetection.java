package com.ec.sdv4;

/**
 * Created by 樊璐 on 2018/1/25.
 */
import android.util.Log;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Vector;


public class camSpermDetection {

    public Mat thisMat;
    public Mat preMat;
    public int finalEnergyNum = 0;
    public int finalTotalNum = 0;
    public int frameNumber = 0;
    public double energyRate =0;
    public double density = 0;
    public Mat diffMat;
    public Mat diffMatWithColor;
    private double rate;
    private int[] totalNumArray = new int[100];
    private int[] energySpermNumArray = new int[99];

    //获取当前帧
    public void getthisMat(Mat newMat,int frame){
        thisMat = new Mat();
        thisMat = newMat;
        //frameNumber++;
        if(frame==0){
            rate = getRate();
        }
    }
    //获取上一帧
    public void getpreMat(){
        preMat = new Mat();
        preMat = thisMat;
    }
    //获取每一帧精子数
    void getTotalNum(int frame){
        Mat grayMat = new Mat();
        Imgproc.cvtColor(thisMat,grayMat,Imgproc.COLOR_BGRA2GRAY);
        Imgproc.threshold(grayMat,grayMat,220,255,Imgproc.THRESH_BINARY_INV);

        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroid = new Mat();
        int m = Imgproc.connectedComponentsWithStats(grayMat,labels,stats,centroid);

        int thisTotalNum = m - 1;
        thisTotalNum = (int)(thisTotalNum/rate);
        totalNumArray[frame] = thisTotalNum;
    }

    //获取每一帧差的活力精子数
    public void getDiff(int i){
        Mat thisGray = new Mat();
        Mat preGray = new Mat();
//        Mat diffMat = new Mat();
        diffMat = new Mat();

        Imgproc.cvtColor(this.thisMat,thisGray,Imgproc.COLOR_BGRA2GRAY);
        Imgproc.cvtColor(this.preMat,preGray,Imgproc.COLOR_BGRA2GRAY);

        Core.absdiff(thisGray,preGray,diffMat);

        Imgproc.threshold(diffMat,diffMat,40,255,Imgproc.THRESH_BINARY);

        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroid = new Mat();
        int m = Imgproc.connectedComponentsWithStats(diffMat,labels,stats,centroid);
      //  getDiffWithColor(centroid);
        energySpermNumArray[i-1] = m-1;
        Log.e("energyNum", Arrays.toString(energySpermNumArray));
    }

    //获取中位数
    private int getMidValue(int[] a,int length){
        Arrays.sort(a);
        Log.e("midValue", Arrays.toString(a));
        int mid = (length+1)/2;
        return a[mid];
    }

    //获取活力率
    public void getEnergyRate(){
        finalEnergyNum = getMidValue(energySpermNumArray,99);//final energy sperm number
        finalTotalNum = getMidValue(totalNumArray,100);//final total sperm number
        if(finalTotalNum!=0&&finalEnergyNum>5){
            energyRate = (double)finalEnergyNum*9.5/(double)finalTotalNum;
        }
        if(energyRate>0.96){
            energyRate=0.96;
        }
    }

    //获取密度
    public void getDensity(){
        finalEnergyNum = getMidValue(energySpermNumArray,99);//final energy sperm number
        finalTotalNum = getMidValue(totalNumArray,100);//final total sperm number
        double pi = 3.1415926;
        double totalArea = pi * 1 * 1*0.01/1000;//毫升
        //this.density = finalTotalNum*7.5 / totalArea;
        if(finalTotalNum!=0){
            this.density = (double)finalTotalNum/totalArea;
            this.density = this.density/100000000;
        }
    }

    //获得比例
    private double getRate(){
        Mat thresh = new Mat();
        Mat gray = new Mat();
        Imgproc.cvtColor(thisMat,gray,Imgproc.COLOR_BGRA2GRAY);
        Imgproc.threshold(gray,thresh,10,255,Imgproc.THRESH_BINARY);

        Mat labels = new Mat();
        Mat stats = new Mat();
        Mat centroid = new Mat();
        int m = Imgproc.connectedComponentsWithStats(thresh,labels,stats,centroid);

        int maxLabel = 0;
        double maxArea = 0;
        for(int i=1;i<m;i++){
            double[] data = stats.get(i, 4);
            if (data[0] > maxArea) {
                maxArea = data[0];
                maxLabel = i;
            }
        }

        int radius = 350;
        double pi = 3.1415926;
        double detectAreawithPixel = pi*radius*radius;//the area we detected(use pixel)
        double rate = detectAreawithPixel / maxArea/2;
        return  rate;
    }

    //帧差标记
    public void getDiffWithColor(Mat centroid){
        diffMatWithColor = new Mat();
        thisMat.copyTo(diffMatWithColor);
        //画红圈
        double[] red = {255,0,0,255};
        double r,g,b;
        for(int i=0;i<centroid.rows();i++){
            double[] x = centroid.get(i,0);
            double[] y = centroid.get(i,1);
            int cen_x = (int)y[0];
            int cen_y = (int)x[0];
            Imgproc.circle(diffMatWithColor,new Point(cen_y,cen_x),5, new Scalar(255,0,0),2,8,0);
        }
    }

}

