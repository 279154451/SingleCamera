singleCamera

1、初始化CameraTool

    CameraTool.Builder builder = new CameraTool.Builder();
        builder.setMaxRecordTime(60);//录制时间控制60s
        builder.setMaxFileSize(10000000);//录制文件大小控制
        builder.setSurfaceView(mSurfaceView);//设置SurfaceView
        CameraTool cameraTool = builder.build(new CameraTool.RecordListener() {
            @Override
            public void onRecordStart() {
                //开始
            }

            @Override
            public void onRecordStop(int timeProgress, String videoFilePath) {
              //结束
            }

            @Override
            public void onRecordCancel() {
              //取消
            }

            @Override
            public void onSwitchCamera(boolean isRecording, int cameraPosition) {
              //切换镜头
            }

            @Override
            public void onRecordError() {
              //录制出错
            }

            @Override
            public void RecordProgress(int timeCount, int progress, long recordFileSize) {
              //录制进度
            }
        });

2、开始录制：

    cameraTool.startRecord(outPutVideoPath);
    
3、结束录制：

    cameraTool.stopRecord();或者cameraTool.stop(false);
    
4、放弃（取消）录制：

    cameraTool.cancelRecord(); 或者 cameraTool.stop(true);
    
5、切换镜头：

      cameraTool.switchCamera();
      
6、开关闪光灯：

    cameraTool.flashLightToggle(isLightFrash);
