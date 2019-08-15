package com.zsf.bamboofilemanager;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * @author EWorld  e-mail:852333743@qq.com
 * 2019/4/17
 */
public class SearchBroadCast extends BroadcastReceiver {
    public static String mServiceKeyword;
    public static String mServiceSearchPath;
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (MainActivity.KEYWORD_BROADCAST.equals(action)){
            mServiceKeyword = intent.getStringExtra("keyword");
            mServiceSearchPath = intent.getStringExtra("search_path");
        }
    }
}
