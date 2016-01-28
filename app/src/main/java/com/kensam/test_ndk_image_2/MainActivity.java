package com.kensam.test_ndk_image_2;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;

public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener{

    private static final String    TAG = "OCVSample::Activity";

    private static final int       VIEW_MODE_RGBA     = 0;
//    private static final int       VIEW_MODE_GRAY     = 1;
//    private static final int       VIEW_MODE_CANNY    = 2;
    private static final int       VIEW_MODE_FEATURES = 5;
    private static final int       VIEW_MODE_TPLMATCH = 6;

    private int                    mViewMode;
    private Mat                    mRgba;
//    private Mat                    mIntermediateMat;
    private Mat                    mGrayMat;
    private Mat                    mytemplate;
//
    private MenuItem               mItemPreviewRGBA;
//    private MenuItem               mItemPreviewGray;
//    private MenuItem               mItemPreviewCanny;
    private MenuItem               mItemPreviewFeatures;

    private CameraBridgeViewBase mOpenCvCameraView;

    private MenuItem mSaveTemplate;
    private MenuItem mTracking;

    int startX = 0, endX = 0, startY = 0, endY = 0;
    Point start_point, end_point;
    Scalar border_colour = new Scalar(255,0,0,0);
    Rect roi_rect = new Rect();


    String filename = null;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
                    Log.i(TAG, "OpenCV loaded successfully");

                    // Load native library after(!) OpenCV initialization
                    System.loadLibrary("module");
                    System.loadLibrary("opencv_java");

                    mOpenCvCameraView.enableView();
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        TextView text = (TextView)findViewById(R.id.text);

        mOpenCvCameraView = (CameraBridgeViewBase) findViewById(R.id.surface_view);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this, mLoaderCallback);
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(TAG, "called onCreateOptionsMenu");
        mItemPreviewRGBA = menu.add("Preview RGBA");
//        mItemPreviewGray = menu.add("Preview GRAY");
//        mItemPreviewCanny = menu.add("Canny");
        mItemPreviewFeatures = menu.add("Find features");
        mSaveTemplate = menu.add("Save Template");
        mTracking = menu.add("TplMatch");
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

        if (item == mSaveTemplate){
            if (endX != 0 && endY != 0){
                mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
                SaveImage(mRgba,roi_rect);
            }else
                Toast.makeText(this, "Please select Region of Interest", Toast.LENGTH_LONG).show();
        }else if (item == mTracking){
            mViewMode = VIEW_MODE_TPLMATCH;
            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
            getPath();
        }
        else if (item == mItemPreviewRGBA) {
            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
            mViewMode = VIEW_MODE_RGBA;
//        } else if (item == mItemPreviewGray) {
//            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_GREY_FRAME);
//            mViewMode = VIEW_MODE_GRAY;
//        } else if (item == mItemPreviewCanny) {
//            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_GREY_FRAME);
//            mViewMode = VIEW_MODE_CANNY;
        }else if (item == mItemPreviewFeatures) {
            mViewMode = VIEW_MODE_FEATURES;
            mOpenCvCameraView.SetCaptureFormat(Highgui.CV_CAP_ANDROID_COLOR_FRAME_RGBA);
        }

        return true;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        mRgba = new Mat(height, width, CvType.CV_8UC4);
//        mIntermediateMat = new Mat(height, width, CvType.CV_8UC4);
        mGrayMat = new Mat(height, width, CvType.CV_8UC1);
    }

    @Override
    public void onCameraViewStopped() {
        mRgba.release();
        mGrayMat.release();
//        mIntermediateMat.release();
    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        mRgba=inputFrame;
        final int viewMode = mViewMode;
        if (startX!=0 && endX!=0 && mViewMode!=VIEW_MODE_TPLMATCH) {
            Core.rectangle(mRgba, start_point, end_point, border_colour, 0, 0, 0);
            roi_rect = new Rect(start_point, end_point);
        }

//
        switch (viewMode) {
//            case VIEW_MODE_GRAY:
//                // input frame has gray scale format
//                Imgproc.cvtColor(inputFrame, mRgba, Imgproc.COLOR_GRAY2RGBA, 4);
//                break;
            case VIEW_MODE_RGBA:
                // input frame has RBGA format
//                inputFrame.copyTo(mRgba);
                startX=0;
                break;
//            case VIEW_MODE_CANNY:
//                // input frame has gray scale format
//                Imgproc.Canny(inputFrame, mIntermediateMat, 80, 100);
//                Imgproc.cvtColor(mIntermediateMat, mRgba, Imgproc.COLOR_GRAY2BGRA, 4);
//                break;
            case VIEW_MODE_FEATURES:
                // input frame has RGBA format
//                inputFrame.copyTo(mRgba);
                Imgproc.cvtColor(mRgba, mGrayMat, Imgproc.COLOR_RGBA2GRAY);
                FindFeatures(mGrayMat.getNativeObjAddr(), mRgba.getNativeObjAddr());
                break;
            case VIEW_MODE_TPLMATCH:
                getPath();
                mytemplate = Highgui.imread(filename, Highgui.CV_LOAD_IMAGE_UNCHANGED);
                int result_cols =  mRgba.cols() - mytemplate.cols() + 1;
                int result_rows = mRgba.rows() - mytemplate.rows() + 1;

                Mat result = new Mat(result_rows, result_cols, CvType.CV_32FC1);
                TplMatch(mRgba.getNativeObjAddr(),mytemplate.getNativeObjAddr(),result.getNativeObjAddr());
                return result;
        }

        return mRgba;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        int action = event.getAction();
        int X = (int) event.getX();
        int Y = (int) event.getY();
        switch (action) {
            case MotionEvent.ACTION_DOWN:
                startX = X;
                startY = Y;
                break;
            case MotionEvent.ACTION_MOVE:
                endX = X;
                endY = Y;
                break;
            case MotionEvent.ACTION_UP:
                endX = X;
                endY = Y;
                break;
        }
        start_point = new Point(startX,startY);
        end_point = new Point(endX,endY);
        return true;
    }

    public void SaveImage (Mat mat, Rect roi) {
        Mat mIntermediateMat = new Mat();
        Imgproc.cvtColor(mat, mIntermediateMat, Imgproc.COLOR_RGBA2BGR, 3);

        getPath();

        mIntermediateMat = mIntermediateMat.submat(roi);//crop matrix for roi
        mIntermediateMat.adjustROI(-2, 0, -2, 0);

        Boolean bool = Highgui.imwrite(filename, mIntermediateMat);

        Toast.makeText(this, "Saved to " + filename , Toast.LENGTH_LONG).show();

        if (bool)
            Log.i(TAG, "SUCCESS writing image to external storage");
        else
            Log.i(TAG, "Fail writing image to external storage");
    }

    private void getPath (){
        File path = new File(Environment.getExternalStorageDirectory() + "/Images/");
        path.mkdirs();
        File file = new File(path, "image.png");
        filename = file.toString();
    }

    public native void FindFeatures(long matAddrGr, long matAddrRgba);
    public native void TplMatch(long matRGB, long matTpl, long matResult);
}
