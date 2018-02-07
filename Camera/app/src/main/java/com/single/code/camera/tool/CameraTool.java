package com.single.code.camera.tool;

import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2018/1/17.
 */
public class CameraTool implements MediaRecorder.OnErrorListener,SurfaceHolder.Callback {
    //存放照片的文件夹
    private Camera mCamera;
    private Timer mTimer;// 计时器
    TimerTask timerTask;
    private int mTimeCount;// 时间计数
    private MediaRecorder mMediaRecorder;// 录制视频的类
    private Builder builder;
    private SurfaceHolder mSurfaceHolder;
    private Camera.Parameters parameters;
    private Camera.Size optimalSize;
    private  File mVecordFile;//录制文件
    //摄像头默认是后置， 0：前置， 1：后置
    private int cameraPosition = 1;
    private RecordListener recordListener;
    private final static int TIMER_COUNT_CODE=0;
    private boolean isRecording = false;
    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case TIMER_COUNT_CODE:
                    int mTimeCount = (int) msg.obj;
                    long fileSize = 0;
                    if(mVecordFile!=null){
                        fileSize = mVecordFile.length();
                        Log.d("HooweCamera","fileSize :"+fileSize);
                    }
                    int progress =0;
                    if(builder.mRecordMaxTime!=0){
                        progress = mTimeCount*100/builder.mRecordMaxTime;
                    }
                    if(recordListener!=null){
                        recordListener.RecordProgress(mTimeCount,progress,fileSize);
                    }
                    if(builder.mRecordMaxTime!=0&&(mTimeCount>=builder.mRecordMaxTime)){//到达指定时间，停止录制
                        stop(false);
                    }
                    if(builder.maxFileSize>0&&(fileSize>=builder.maxFileSize)){//达到最大录制大小，停止录制
                        stop(false);
                    }
                    break;
            }
        }
    };
    private CameraTool(Builder builder,RecordListener recordListener){
        this.builder = builder;
        this.recordListener = recordListener;
        mSurfaceHolder = builder.surfaceView.getHolder();// 取得holder
        mSurfaceHolder.addCallback(this); // holder加入回调接口
        mSurfaceHolder.setKeepScreenOn(true);
    }

    /**
     *
     * @param surfaceChangedHeight  surfaceChanged(SurfaceHolder holder, int format, int surfaceChangedWidth, int surfaceChangedHeight)
     * @param surfaceChangedWidth   surfaceChanged(SurfaceHolder holder, int format, int surfaceChangedWidth, int surfaceChangedHeight)
     */
    public void initCamera(int surfaceChangedHeight,int surfaceChangedWidth){
        if (mCamera != null) {
            freeCameraResource();
        }
        try {
            mCamera = Camera.open();
            if (mCamera == null)
                return;
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(mSurfaceHolder);
            parameters = mCamera.getParameters();// 获得相机参数

            List<Camera.Size> mSupportedPreviewSizes = parameters.getSupportedPreviewSizes();
            List<Camera.Size> mSupportedVideoSizes = parameters.getSupportedVideoSizes();
            optimalSize = CameraHelper.getOptimalVideoSize(mSupportedVideoSizes,
                    mSupportedPreviewSizes, surfaceChangedHeight, surfaceChangedWidth);

            parameters.setPreviewSize(optimalSize.width, optimalSize.height); // 设置预览图像大小

            parameters.set("orientation", "portrait");
            List<String> focusModes = parameters.getSupportedFocusModes();
            if (focusModes.contains("continuous-video")) {
                parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            }
//            mFpsRange =  parameters.getSupportedPreviewFpsRange();

            mCamera.setParameters(parameters);// 设置相机参数
            mCamera.startPreview();// 开始预览
        }catch (Exception io){
            io.printStackTrace();
        }
    }

    /**
     * 开始录制
     */
    public void startRecord(String saveVideoPath){
        if(!isRecording){
            isRecording = true;
            builder.setVideoFile(saveVideoPath);
            initTimer();
            initRecord();
            if(recordListener!=null){
                recordListener.onRecordStart();
            }
        }
    }

    /**
     * 初始化计时器
     */
    private void initTimer(){
        mTimer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                mTimeCount++;
                if(mHandler!=null){
                    Message message = new Message();
                    message.what = TIMER_COUNT_CODE;
                    message.obj = mTimeCount;
                    mHandler.sendMessage(message);
                }
            }
        };
        mTimer.schedule(timerTask, 0, 1000);//1s执行一次
    }

    /**
     * 释放timer
     */
    private void releaseTimer(){
        if(timerTask!=null){
            timerTask.cancel();
        }
        if(mTimer!=null){
            mTimer.cancel();
        }
        if(mHandler!=null){
            mHandler.removeCallbacksAndMessages(null);
        }
    }
    /**
     * 停止录制
     */
    public void stopRecord(){
        if(isRecording){
            if (mMediaRecorder != null) {
                try {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            releaseTimer();
            if(recordListener!=null){
                recordListener.onRecordStop(mTimeCount,mVecordFile.getAbsolutePath());
            }
        }
        mTimeCount = 0;
        isRecording = false;
    }

    /**
     * 停止录制
     * @param isCancel true:放弃录制 false：结束录制
     */
    public void stop(boolean isCancel){
        if(isCancel){
            cancelRecord();
        }else {
            stopRecord();
        }
        releaseRecord();
        freeCameraResource();
    }

    /**
     * 取消录制
     */
    public void cancelRecord(){
        if(isRecording){
            releaseTimer();
            if (mMediaRecorder != null) {
                try {
                    mMediaRecorder.stop();
                    mMediaRecorder.reset();
                } catch (IllegalStateException e) {
                    e.printStackTrace();
                } catch (RuntimeException e) {
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if(mVecordFile!=null&& mVecordFile.exists()){
                mVecordFile.delete();
            }
            if(recordListener!=null){
                recordListener.onRecordCancel();
            }
        }
        isRecording = false;
    }

    /**
     * 拍照闪光控制
     * @param isFlashLightOn
     */
    public void flashLightToggle(boolean isFlashLightOn){
        try {
            if(isFlashLightOn){
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
                mCamera.setParameters(parameters);
            }else {
                parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(parameters);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 切换摄像头
     */
    public void switchCamera(){
        if(!isRecording){
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            int cameraCount = Camera.getNumberOfCameras();//得到摄像头的个数

            for(int i = 0; i < cameraCount; i++ ) {
                Camera.getCameraInfo(i, cameraInfo);//得到每一个摄像头的信息
                if(cameraPosition == 1) {
                    //现在是后置，变更为前置
                    if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_FRONT) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                        mCamera.stopPreview();//停掉原来摄像头的预览
                        mCamera.release();//释放资源
                        mCamera = null;//取消原来摄像头
                        mCamera = Camera.open(i);//打开当前选中的摄像头
                        try {
                            mCamera.setDisplayOrientation(90);
                            mCamera.setPreviewDisplay(mSurfaceHolder);//通过surfaceview显示取景画面
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
//                    mCamera.setParameters(parameters);// 设置相机参数
                        mCamera.startPreview();//开始预览
                        cameraPosition = 0;
                        break;
                    }
                } else {
                    //现在是前置， 变更为后置
                    if(cameraInfo.facing  == Camera.CameraInfo.CAMERA_FACING_BACK) {//代表摄像头的方位，CAMERA_FACING_FRONT前置      CAMERA_FACING_BACK后置
                        mCamera.stopPreview();//停掉原来摄像头的预览
                        mCamera.release();//释放资源
                        mCamera = null;//取消原来摄像头
                        mCamera = Camera.open(i);//打开当前选中的摄像头
                        try {
                            mCamera.setDisplayOrientation(90);
                            mCamera.setPreviewDisplay(mSurfaceHolder);//通过surfaceview显示取景画面
                        } catch (IOException e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                        mCamera.setParameters(parameters);// 设置相机参数
                        mCamera.startPreview();//开始预览
                        cameraPosition = 1;
                        break;
                    }
                }
            }
        }
        if(recordListener!=null){
            recordListener.onSwitchCamera(isRecording,cameraPosition);
        }
    }

    /**
     * 录制前，初始化
     */
    private void initRecord() {
        try {
            if(mMediaRecorder == null){
                mMediaRecorder = new MediaRecorder();
            }
            if(mCamera != null){
                mCamera.unlock();
                mMediaRecorder.setCamera(mCamera);
            }
            mMediaRecorder.setOnErrorListener(this);
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);// 视频源
//            设置最大录制时间和最大录制文件大小
//            if(builder.mRecordMaxTime!=0){
//                mMediaRecorder.setMaxDuration(builder.mRecordMaxTime*1000);
//            }
//            if(builder.maxFileSize!=0){
//                mMediaRecorder.setMaxFileSize(builder.maxFileSize);
//            }
//            mMediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
//                @Override
//                public void onInfo(MediaRecorder mr, int what, int extra) {
//                    Log.d("HooweCamera","what :"+what);
//                    switch (what){
//                        case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
//                        case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
//                            stop(false);
//                            break;
//                    }
//                }
//            });
            // Use the same size for recording profile.
            CamcorderProfile mProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
            mProfile.videoFrameWidth = optimalSize.width;
            mProfile.videoFrameHeight = optimalSize.height;

            mMediaRecorder.setProfile(mProfile);
            //该设置是为了抽取视频的某些帧，真正录视频的时候，不要设置该参数
//            mMediaRecorder.setCaptureRate(mFpsRange.get(0)[0]);//获取最小的每一秒录制的帧数
            mVecordFile = new File(builder.mVideoFilePath);
            mMediaRecorder.setOutputFile(mVecordFile.getAbsolutePath());

            mMediaRecorder.prepare();
            mMediaRecorder.start();
        } catch (Exception e) {
            e.printStackTrace();
            releaseRecord();
        }
    }

    /**
     * 释放资源
     */
    public void releaseRecord() {
        if (mMediaRecorder != null) {
            mMediaRecorder.setPreviewDisplay(null);
            mMediaRecorder.setOnErrorListener(null);
            try {
                mMediaRecorder.release();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        mMediaRecorder = null;
    }

    /**
     * 移除录制监听
     */
    public void removeListener(){
        if(recordListener!=null){
            recordListener= null;
        }
    }



    /**
     * 释放摄像头资源
     */
    private void freeCameraResource() {
        if (mCamera != null) {
            mCamera.setPreviewCallback(null);
            mCamera.stopPreview();
            mCamera.lock();
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {
        try {
            if (mr != null){
//                mr.stop();
                mr.reset();
            }
            if(recordListener!=null){
                recordListener.onRecordError();
            }
            releaseRecord();
            freeCameraResource();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        initCamera(height,width);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stop(false);
    }

    public static class Builder{
        private String mVideoFilePath = System.currentTimeMillis()+".mp4";// 文件
        private int mRecordMaxTime = 10;// 一次拍摄最长时间 默认10秒 ,0:表示不限制
        private long maxFileSize = 0;//文件最大录制大小 0：表示不限制
        private SurfaceView surfaceView;
        public Builder setSurfaceView(SurfaceView view){
            this.surfaceView = view;
            return this;
        }
        private Builder setVideoFile(String videoFilePath){
            this.mVideoFilePath = videoFilePath;
            return this;
        }
        public Builder setMaxFileSize(long fileSize){
            this.maxFileSize = fileSize;
            return this;
        }

        /**
         *
         * @param maxTime 最大录制时间 s
         * @return
         */
        public Builder setMaxRecordTime(int maxTime){
            this.mRecordMaxTime = maxTime;
            return this;
        }
        public CameraTool build(RecordListener recordListener){
            return new CameraTool(this,recordListener);
        }
    }

    public interface RecordListener{
        void onRecordStart();//录制开始

        /**
         *
         * @param videoFilePath 录制的文件路径
         */
        void onRecordStop(int timeProgress, String videoFilePath);//录制结束
        void onRecordCancel();//放弃录制

        /**
         *
         * @param cameraPosition   摄像头默认是后置， 0：前置， 1：后置
         */
        void onSwitchCamera(boolean isRecording, int cameraPosition);

        /**
         * 录制出错
         */
        void onRecordError();

        /**
         *@param timeCount 录制的时间 s
         * @param progress 录制执行时间进度
         * @param recordFileSize 录制视频文件大小
         */
        void RecordProgress(int timeCount,int progress, long recordFileSize);
    }
}