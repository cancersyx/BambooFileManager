package com.zsf.bamboofilemanager;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * @author EWorld  e-mail:852333743@qq.com
 * 2019/5/24
 */
public class FileAdapter extends BaseAdapter {
    private Bitmap mBackRoot;
    private Bitmap mBackUp;
    private Bitmap mImage;
    private Bitmap mAudio;
    private Bitmap mRar;
    private Bitmap mVideo;
    private Bitmap mFolder;
    private Bitmap mApk;
    private Bitmap mOthers;
    private Bitmap mTxt;
    private Bitmap mWeb;

    private Context mContext;
    private List<String> mFileNameList;
    private List<String> mFilePathList;

    public FileAdapter(Context context, List<String> fileNameList, List<String> filePathList) {
        mContext = context;
        mFileNameList = fileNameList;
        mFilePathList = filePathList;

        //初始化图片资源
        //返回到根目录
        mBackRoot = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.back_to_root);
        //返回到上一级目录
        mBackUp = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.back_to_up);
        //图片文件对应的icon
        mImage = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.image);
        //音频文件
        mAudio = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.audio);
        //视频文件
        mVideo = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.video);
        //可执行文件
        mApk = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.apk);
        //文本文件
        mTxt = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.txt);
        //其他类型文件
        mOthers = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.others);
        //文件夹对应的
        mFolder = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.folder);
        //zip文件
        mRar = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.zip_icon);
        //网页文件
        mWeb = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.web_browser);
    }

    @Override
    public int getCount() {
        return mFilePathList.size();
    }

    @Override
    public Object getItem(int position) {
        return mFileNameList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null) {
            holder = new ViewHolder();
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_child, null);
            holder.mIv = convertView.findViewById(R.id.image_list_childs);
            holder.mTv = convertView.findViewById(R.id.text_list_childs);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        File file = new File(mFilePathList.get(position).toString());
        if (mFileNameList.get(position).toString().equals("BacktoRoot")) {
            holder.mIv.setImageBitmap(mBackRoot);
            holder.mTv.setText("返回根目录");
        } else if (mFileNameList.get(position).equals("BacktoUp")) {
            holder.mIv.setImageBitmap(mBackUp);
            holder.mTv.setText("返回上一级");
        } else if (mFileNameList.get(position).equals("BacktoSearchBefore")) {
            holder.mIv.setImageBitmap(mBackRoot);
            holder.mTv.setText("返回搜索之前目录");
        } else {
            String fileName = file.getName();
            holder.mTv.setText(fileName);
            if (file.isDirectory()) {
                holder.mIv.setImageBitmap(mFolder);
            } else {
                String fileEnds = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
                if (fileEnds.equals("m4a") || fileEnds.equals("mp3") || fileEnds.equals("mid") || fileEnds.equals("xmf")
                        || fileEnds.equals("ogg") || fileEnds.equals("wav")) {
                    holder.mIv.setImageBitmap(mVideo);
                } else if (fileEnds.equals("3gp") || fileEnds.equals("mp4") || fileEnds.equals("")) {
                    holder.mIv.setImageBitmap(mAudio);
                } else if (fileEnds.equals("jpg") || fileEnds.equals("gif") || fileEnds.equals("png") ||
                        fileEnds.equals("jpeg") || fileEnds.equals("bmp")) {
                    holder.mIv.setImageBitmap(mImage);
                } else if (fileEnds.equals("apk")) {
                    holder.mIv.setImageBitmap(mApk);
                } else if (fileEnds.equals("txt ")) {
                    holder.mIv.setImageBitmap(mTxt);
                } else if (fileEnds.equals("zip") || fileEnds.equals("rar") || fileEnds.equals("7z")) {
                    holder.mIv.setImageBitmap(mRar);
                } else if (fileEnds.equals("html") || fileEnds.equals("htm") || fileEnds.equals("mht")) {
                    holder.mIv.setImageBitmap(mWeb);
                } else {
                    holder.mIv.setImageBitmap(mOthers);
                }
            }
        }
        return convertView;
    }


    class ViewHolder {
        ImageView mIv;
        TextView mTv;
    }
}
