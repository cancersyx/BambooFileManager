package com.zsf.bamboofilemanager;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.RelativeLayout;
import android.widget.ZoomControls;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import static java.net.Proxy.Type.HTTP;

/**
 * @author EWorld  e-mail:852333743@qq.com
 * 2019/5/28
 */
public class WebActivity extends AppCompatActivity {
    private WebView mWebView;
    private RelativeLayout mLoadingLayout;
    private RelativeLayout mWebLayout;
    private ZoomControls mZoomControls;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_watch_web);

        mWebView = findViewById(R.id.web_view);
        mLoadingLayout = findViewById(R.id.rl_loading_layout);
        mWebLayout = findViewById(R.id.rl_web_container);
        mZoomControls = findViewById(R.id.zoom_controls);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        new MyAsyncTask().execute("");
    }
    private void reading(){
        String filePath = getIntent().getStringExtra("filePath");
        if (filePath != null){
            mWebView.loadData(readWebDataToStringFromPath(filePath, new FileReadOverBack() {
                @Override
                public void fileReadOver() {

                }
            }),"text/html","utf-8");
        }else {
            new AlertDialog.Builder(this).setTitle("出错")
                    .setMessage("获取文件路径出错")
                    .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            WebActivity.this.finish();
                        }
                    });
        }
    }

    private String readWebDataToStringFromPath(String path,FileReadOverBack fileReadOverBack){
        File file = new File(path);
        StringBuffer stringBuffer = new StringBuffer();
        try {
            FileInputStream fs = new FileInputStream(file);
            byte[] bytes = new byte[1024];
            int readCount = 0;
            while ((readCount = fs.read(bytes)) > 0){
                stringBuffer.append(new String(bytes,0,readCount));

            }
            fileReadOverBack.fileReadOver();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stringBuffer.toString();
    }


    interface FileReadOverBack{
        void fileReadOver();
    }
    class MyAsyncTask extends AsyncTask<String,String,String>{

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mLoadingLayout.setVisibility(View.VISIBLE);
            mWebLayout.setVisibility(View.GONE);
        }

        @Override
        protected String doInBackground(String... strings) {
            reading();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            mLoadingLayout.setVisibility(View.GONE);
            mWebLayout.setVisibility(View.VISIBLE);
            mZoomControls.setOnZoomInClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWebView.zoomIn();//放大
                }
            });

            mZoomControls.setOnZoomOutClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mWebView.zoomOut();
                }
            });
        }
    }
}
