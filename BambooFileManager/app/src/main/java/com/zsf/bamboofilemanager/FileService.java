package com.zsf.bamboofilemanager;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;

import java.io.File;
import java.util.ArrayList;

/**
 * @author EWorld  e-mail:852333743@qq.com
 * 2019/4/17
 */
public class FileService extends Service {
    private Looper mLooper;
    private FileHandler mFileHandler;
    private ArrayList<String> mFileName = null;
    private ArrayList<String> mFilePaths = null;
    public static final String FILE_SEARCH_COMPLETED = "com.zsf.file.FILE_SEARCH_NOTIFICATION";
    public static final String FILE_NOTIFICATION = "com.zsf.file.FILE_NOTIFICATION";

    private NotificationManager mNotificationManager;
    private int m = -1;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //新建线程处理搜索操作
        HandlerThread handlerThread = new HandlerThread("FileService", HandlerThread.NORM_PRIORITY);
        handlerThread.start();
        mLooper = handlerThread.getLooper();
        mFileHandler = new FileHandler(mLooper);

    }


    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        mFileName = new ArrayList<>();
        mFilePaths = new ArrayList<>();
        mFileHandler.sendEmptyMessage(0);
        //发出通知表明正在进行搜索
        fileSearchNotification();
    }

    private void fileSearchNotification() {
//        Notification notification = new Notification(R.drawable.ym,"后台搜索中...",System.currentTimeMillis());
//        Intent intent = new Intent(FILE_NOTIFICATION);
//        //打开notice时的提示内容
//        intent.putExtra("notification","当通知还在说明搜索未完成，可以在这里触发一个事件，当点击通知回到Activity之后，可以弹出一个框，提示是否取消搜索");
//        PendingIntent pi = PendingIntent.getBroadcast(this,0,intent,0);
//        // TODO: 2019/5/23 这里的方法找不到
//        //notification.setL(this,"","",pi);
//        if (mNotificationManager == null){
//            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
//        }
//        mNotificationManager.notify(R.string.app_name,notification);

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        //取消通知
        mNotificationManager.cancel(R.string.app_name);

    }


    class FileHandler extends Handler {
        public FileHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            //在指定范围搜索
            initFileArray(new File(SearchBroadCast.mServiceSearchPath));
            //用户单击取消搜索则不发送广播
            if (!MainActivity.isComeBackFromNotification == true) {
                Intent intent = new Intent(FILE_SEARCH_COMPLETED);
                intent.putStringArrayListExtra("mFileNameList", mFileName);
                intent.putStringArrayListExtra("mFilePathList", mFilePaths);
                //搜索完毕后携带数据并发送广播
                sendBroadcast(intent);
            }
        }
    }

    /**
     * 搜索的具体逻辑
     * @param file
     */
    private void initFileArray(File file) {
        //遍历可读的文件
        if (file.canRead()){
            File[] fileArray = file.listFiles();
            for (File currentArray : fileArray){
                if (currentArray.getName().indexOf(SearchBroadCast.mServiceKeyword) != -1){
                    if (m == -1){
                        m++;
                        //返回搜索之前的目录
                        mFileName.add("BacktoSearchBefore");
                        mFilePaths.add(MainActivity.mCurrentFilePath);
                    }
                    mFileName.add(currentArray.getName());
                    mFilePaths.add(currentArray.getPath());
                }
                //如果是文件夹则回调该方法
                if (currentArray.exists() && currentArray.isDirectory()){
                    //用户取消了搜索，停止继续搜索
                    if (MainActivity.isComeBackFromNotification == true){
                        return;
                    }
                    initFileArray(currentArray);
                }
            }
        }
    }








}
