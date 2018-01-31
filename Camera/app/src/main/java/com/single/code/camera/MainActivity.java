package com.single.code.camera;

import android.content.res.Configuration;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dinuscxj.progressbar.CircleProgressBar;
import com.single.code.camera.tool.CameraTool;

public class MainActivity extends AppCompatActivity implements View.OnClickListener,CameraTool.RecordListener {
//    private SurfaceHolder mSurfaceHolder;
    private CircleProgressBar mProgressBar;
    private SurfaceView mSurfaceView;
    private ImageView startBtn;
    private ImageView lightBtn;
    private ImageView tag_start;
    private AnimationDrawable anim;
    private LinearLayout lay_tool;
    private CameraTool cameraTool;
    boolean isRecording  =false;
    boolean isLightFrash = false;
    //存放照片的文件夹
    public final static String  BASE_PATH = Environment.getExternalStorageDirectory().getAbsolutePath() + "/video/";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);// 去掉标题栏
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);// 设置全屏
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initView();
        initData();
    }
    private void initView() {
        mProgressBar = (CircleProgressBar)findViewById(R.id.progress);
        lightBtn = (ImageView) findViewById(R.id.lightBtn);
        tag_start = (ImageView) findViewById(R.id.tag_start);
        anim = (AnimationDrawable)tag_start.getDrawable();
        anim.setOneShot(false); // 设置是否重复播放
        lay_tool = (LinearLayout) findViewById(R.id.lay_tool);
        lightBtn.setOnClickListener(this);
        findViewById(R.id.exitBtn).setOnClickListener(this);
        findViewById(R.id.switchCamera).setOnClickListener(this);

        mSurfaceView = (SurfaceView)findViewById(R.id.surfaceView);
//        mSurfaceHolder = mSurfaceView.getHolder();// 取得holder
//        mSurfaceHolder.addCallback(this); // holder加入回调接口
//        mSurfaceHolder.setKeepScreenOn(true);
        startBtn = (ImageView) findViewById(R.id.startBtn);
        startBtn.setOnClickListener(this);
    }
    private void initData(){
        if(cameraTool==null){
            CameraTool.Builder builder = new CameraTool.Builder();
            builder.setMaxRecordTime(60);
            builder.setMaxFileSize(10000000);
            builder.setSurfaceView(mSurfaceView);
            cameraTool = builder.build(this);
        }
    }


    @Override
    protected void onStop() {
        super.onStop();
        if(cameraTool!=null){
            cameraTool.stop(false);
        }
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }
    private String getVideoPath(){
        return BASE_PATH+System.currentTimeMillis()+".mp4";
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.startBtn) {
            if(cameraTool!=null){
                if(isRecording){
                    isRecording = false;
                    cameraTool.stopRecord();

                }else {
                    isRecording =true;
                    cameraTool.startRecord(getVideoPath());
                }
            }
        } else if (i == R.id.exitBtn) {
            if(cameraTool!=null){
                cameraTool.stop(true);
            }
            finish();
        } else if (i == R.id.switchCamera) {
            if(cameraTool!=null){
                cameraTool.switchCamera();
            }
        } else if (i == R.id.info) {

        } else if (i == R.id.lightBtn) {
            if(cameraTool!=null){
                if(isLightFrash){
                    isLightFrash = false;
                }else {
                    isLightFrash = true;
                }
                cameraTool.flashLightToggle(isLightFrash);
            }
        }
    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {

    }

    @Override
    public void onRecordStart() {
        anim.start();
    }

    @Override
    public void onRecordStop(int timeCount ,String videoFilePath) {
        anim.stop();
        if(mProgressBar!=null){
            mProgressBar.setProgress(0);
        }
        Log.d("HooweCamera","onRecordStop videoPath :"+videoFilePath+" timeCount :"+timeCount);
    }

    @Override
    public void onRecordCancel() {
        anim.stop();
    }

    @Override
    public void onSwitchCamera(boolean isRecording,int cameraPosition) {
        anim.stop();
    }

    @Override
    public void onRecordError() {
        anim.stop();
    }

    @Override
    public void RecordProgress(int timeCount,int progress,long fileSize) {
        if(mProgressBar!=null){
            mProgressBar.setProgress(progress);
        }
    }
}
