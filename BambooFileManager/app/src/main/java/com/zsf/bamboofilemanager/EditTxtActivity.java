package com.zsf.bamboofilemanager;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * @author EWorld  e-mail:852333743@qq.com
 * 2019/5/28
 */
public class EditTxtActivity extends AppCompatActivity {
    private EditText mContentView;
    private TextView mTitleView;
    private Button mSaveBtn;
    private Button mCancleBtn;

    private String mTitle;
    private String mData;
    private String mPath;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_txt);
        initView();

        initData();
    }

    private void initData() {
        mPath = getIntent().getStringExtra("path");
        mTitle = getIntent().getStringExtra("title");
        mData = getIntent().getStringExtra("data");

        try {
            mData = new String(mData.getBytes("ISO-8859-1"), "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        mTitleView.setText(mTitle);
        mContentView.setText(mData);

    }

    private void initView() {
        mTitleView = findViewById(R.id.tv_file_name);
        mContentView = findViewById(R.id.et_content_detail);
        mSaveBtn = findViewById(R.id.btn_save);
        mCancleBtn = findViewById(R.id.btn_cancel);

        mSaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveTxt();
            }
        });
        mCancleBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditTxtActivity.this.finish();
            }
        });
    }

    private void saveTxt() {
        try {
            String content = mContentView.getText().toString();
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(mPath)));
            //写入文件
            bw.write(content, 0, content.length());
            bw.newLine();
            bw.close();
            Toast.makeText(this, "成功保存！", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            Toast.makeText(this, "存储异常", Toast.LENGTH_SHORT).show();
            e.printStackTrace();

        }
        this.finish();
    }
}
