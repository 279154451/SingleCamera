singleCamera

1、初始化CameraTool：


      CameraTool cameraTool = new CameraTool.Builder()
      .setMaxRecordTime(60)
      .setMaxFileSize(10000000)
      .setSurfaceView(mSurfaceView)
      .build(new CameraTool.RecordListener() {
            @Override
            public void onRecordStart() {

            }

            @Override
            public void onRecordStop(int timeProgress, String videoFilePath) {

            }

            @Override
            public void onRecordCancel() {

            }

            @Override
            public void onSwitchCamera(boolean isRecording, int cameraPosition) {

            }

            @Override
            public void onRecordError() {

            }

            @Override
            public void RecordProgress(int timeCount, int progress, long recordFileSize) {

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
