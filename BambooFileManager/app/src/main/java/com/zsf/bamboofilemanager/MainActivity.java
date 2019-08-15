package com.zsf.bamboofilemanager;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Environment;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends ListActivity implements AdapterView.OnItemLongClickListener {
    //显示的文件列表的名称
    private List<String> mFileName = null;
    //显示的文件列表的相对应的路径
    private List<String> mFilePaths = null;
    //起始目录"/"
    private String mRootPath = File.separator;
    //SD 根目录
    private String mSdCard = Environment.getExternalStorageDirectory().toString();

    private String mOldFilePath = "";
    private String mNewFilePath = "";
    private String mKeyWords;
    //用于显示当前路径
    private TextView mPath;
    private GridView mGridViewToolBar;

    private int[] gridview_menu_image = {R.drawable.menu_phone, R.drawable.menu_sdcard,
            R.drawable.menu_search, R.drawable.menu_create, R.drawable.menu_palse,
            R.drawable.menu_exit};
    private String[] gridview_menu_title = {"手机", "SD卡", "搜索", "创建", "粘贴", "退出"};

    private static int menuPosition = 1;//1代表手机，2代表SD卡

    private boolean isAddBackUp = false;
    //存储当前目录路径信息
    public static String mCurrentFilePath = "";

    private String mCopyFileName;
    private boolean isCopy = false;

    private String mNewFolderName;
    private File mCreateFile;
    private RadioGroup mCreateRadioGroup;
    private static int mChecked;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initGridViewMenu();
        initMenuListener();
        getListView().setOnItemLongClickListener(this);
        mPath = findViewById(R.id.tv_path);

        initFileListInfo(mRootPath);
    }

    private void initGridViewMenu() {
        mGridViewToolBar = findViewById(R.id.grid_file_toolbar);
        mGridViewToolBar.setSelector(R.drawable.menu_item_selected);
        mGridViewToolBar.setBackgroundResource(R.drawable.menu_background);
        mGridViewToolBar.setNumColumns(6);
        mGridViewToolBar.setGravity(Gravity.CENTER);
        mGridViewToolBar.setVerticalSpacing(10);
        mGridViewToolBar.setHorizontalSpacing(10);
        mGridViewToolBar.setAdapter(getMenuAdapter(gridview_menu_title, gridview_menu_image));
    }

    private SimpleAdapter getMenuAdapter(String[] menuNameArray, int[] imageResArray) {
        ArrayList<HashMap<String, Object>> mData = new ArrayList<>();
        for (int i = 0; i < menuNameArray.length; i++) {
            HashMap<String, Object> mMap = new HashMap<>();
            mMap.put("image", imageResArray[i]);
            mMap.put("title", menuNameArray[i]);
            mData.add(mMap);
        }

        SimpleAdapter adapter = new SimpleAdapter(this, mData, R.layout.item_menu,
                new String[]{"image", "title"}, new int[]{R.id.iv_img, R.id.tv_text});
        return adapter;
    }

    private void initMenuListener() {
        mGridViewToolBar.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        menuPosition = 1;
                        initFileListInfo(mRootPath);
                        break;
                    case 1:
                        menuPosition = 2;
                        initFileListInfo(mSdCard);
                        break;
                    case 2:
                        searchDialog();
                        break;
                    case 3:
                        createFolder();
                        break;
                    case 4:
                        pasteFile();
                        break;
                    case 5:
                        MainActivity.this.finish();
                        break;
                }
            }
        });
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        if (isAddBackUp) {
            if (position != 0 && position != 1) {
                initItemLongClickListener(new File(mFilePaths.get(position)));
            }
        }

        if (mCurrentFilePath.equals(mRootPath) || mCurrentFilePath.equals(mSdCard)) {
            initItemLongClickListener(new File(mFilePaths.get(position)));
        }
        return false;
    }

    private void initItemLongClickListener(final File file) {
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (file.canRead()) {
                    if (which == 0) {
                        //复制
                        if (file.isFile() && "txt".equals(file.getName().substring(file.getName().lastIndexOf(".") + 1,
                                file.getName().length()).toLowerCase())) {
                            Toast.makeText(MainActivity.this, "已复制", Toast.LENGTH_SHORT).show();
                            //复制标志位,表明已复制文件
                            isCopy = true;
                            //取得复制文件的名字
                            mCopyFileName = file.getName();
                            //记录复制文件的路径
                            mOldFilePath = mCurrentFilePath + File.separator + mCopyFileName;

                        } else {
                            Toast.makeText(MainActivity.this, "仅支持复制文本文件", Toast.LENGTH_SHORT).show();
                        }
                    } else if (which == 1) {
                        //重命名
                        initRenameDialog(file);
                    } else if (which == 2) {
                        //删除
                        initDeleteDialog(file);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "抱歉，您的访问权限不足", Toast.LENGTH_SHORT).show();
                }
            }
        };
        //列表项名称
        String[] menus = {"复制", "重命名", "删除"};
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("请选择操作！")
                .setItems(menus, listener)
                .setPositiveButton("取消", null)
                .show();
    }

    private void initFileListInfo(String filePath) {
        isAddBackUp = false;
        mCurrentFilePath = filePath;
        //显示当前的路径
        mPath.setText(filePath);

        mFileName = new ArrayList<>();
        mFilePaths = new ArrayList<>();

        File file = new File(filePath);
        File[] files = file.listFiles();

        if (menuPosition == 1 && !mCurrentFilePath.equals(mRootPath)) {
            initAddBackUp(filePath, mRootPath);
        } else if (menuPosition == 2 && !mCurrentFilePath.equals(mSdCard)) {
            initAddBackUp(filePath, mSdCard);
        }

        if (files == null){
            return;
        }
        if(files.length > 0){
            for (File currentFile : files) {
                mFileName.add(currentFile.getName());
                mFilePaths.add(currentFile.getPath());
            }

            setListAdapter(new FileAdapter(MainActivity.this, mFileName, mFilePaths));
        }

    }


    private void initAddBackUp(String filePath, String sdCard) {
        if (!filePath.equals(sdCard)) {
            //列表第一项设置为返回目录
            mFileName.add("BacktoRoot");
            mFilePaths.add(sdCard);
            //列表第二项设置为返回上一级
            mFileName.add("BackToUp");
            mFilePaths.add(new File(filePath).getParent());
            //将添加返回键标志位设置为true
            isAddBackUp = true;
        }
    }


    private void createFolder() {
        //标识当前选中的是文件或者文件夹
        mChecked = 2;
        LayoutInflater inflater = (LayoutInflater) this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        //初始化对话框布局
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.dialog_create, null);
        mCreateRadioGroup = layout.findViewById(R.id.rg_create);
        final RadioButton createFolderButton = layout.findViewById(R.id.rb_create_folder);
        final RadioButton createFileButton = layout.findViewById(R.id.rb_create_file);
        createFolderButton.setChecked(true);

        mCreateRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == createFileButton.getId()) {
                    mChecked = 1;
                } else if (checkedId == createFolderButton.getId()) {
                    mChecked = 2;
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("新建")
                .setView(layout)
                .setPositiveButton("创建", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mNewFolderName = ((EditText) layout.findViewById(R.id.et_new_file_name)).getText().toString();
                        if (mChecked == 1) {
                            try {
                                mCreateFile = new File(mCurrentFilePath + File.separator + mNewFolderName + ".txt");
                                mCreateFile.createNewFile();
                                //刷新当前列表
                                initFileListInfo(mCurrentFilePath);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if (mChecked == 2) {
                            mCreateFile = new File(mCurrentFilePath + File.separator + mNewFolderName);
                            if (!mCreateFile.exists() && !mCreateFile.isDirectory() && mNewFolderName.length() != 0) {
                                if (mCreateFile.mkdirs()) {
                                    //刷新当前目录文件列表
                                    initFileListInfo(mCurrentFilePath);
                                } else {
                                    Toast.makeText(MainActivity.this, "创建失败，请检查是否权限不够！", Toast.LENGTH_SHORT).show();
                                }
                            } else {
                                Toast.makeText(MainActivity.this, "文件命名为空", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }
                }).setNegativeButton("取消", null);
        builder.show();
    }

    private EditText mEditFileName;

    private void initRenameDialog(final File file) {
        LayoutInflater layoutInflater = LayoutInflater.from(MainActivity.this);
        LinearLayout layout = (LinearLayout) layoutInflater.inflate(R.layout.dialog_rename, null);
        mEditFileName = layout.findViewById(R.id.tv_file_name);
        //显示当前文件名
        mEditFileName.setText(file.getName());
        DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String modifyName = mEditFileName.getText().toString();
                String modifyFilePath = file.getParentFile().getPath() + File.separator;
                final String newFilePath = modifyFilePath + modifyName;
                if (new File(newFilePath).exists()) {
                    if (!modifyName.equals(file.getName())) {
                        //把重命名操作时候没做任何修改的情况过滤掉
                        //弹出已存在的提示
                        new AlertDialog.Builder(MainActivity.this)
                                .setTitle("提示")
                                .setMessage("该文件名已存在，是否要覆盖？")
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        file.renameTo(new File(newFilePath));//使用file.renameTo()函数进行重命名
                                        Toast.makeText(MainActivity.this, "the file path is " + new File(newFilePath), Toast.LENGTH_SHORT).show();
                                        //更新当前目录信息
                                        initFileListInfo(file.getParentFile().getPath());
                                    }
                                })
                                .setPositiveButton("取消", null);
                    }
                } else {
                    //文件名不重复时直接修改文件名后再刷新
                    file.renameTo(new File(newFilePath));
                    initFileListInfo(file.getParentFile().getPath());

                }
            }
        };

        //显示对话框
        AlertDialog renameDialog = new AlertDialog.Builder(MainActivity.this).create();
        renameDialog.setView(layout);
        renameDialog.setButton(DialogInterface.BUTTON_POSITIVE, "确定", listener);
        renameDialog.setButton(DialogInterface.BUTTON_POSITIVE, "取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        renameDialog.show();
    }

    private void initDeleteDialog(final File file) {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("提示")
                .setMessage("您确定哟啊删除该" + (file.isDirectory() ? "文件夹" : "文件") + "吗？")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (file.isFile()) {
                            //是文件直接删除
                            file.delete();
                        } else {
                            //是文件夹
                            deleteFolder(file);
                        }
                        //重新遍历该文件目录
                        initFileListInfo(file.getParent());
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void deleteFolder(File folder) {
        File[] fileArray = folder.listFiles();
        if (fileArray.length == 0) {
            //空文件夹直接删除
            folder.delete();
        } else {
            //遍历该目录
            for (File currentFile : fileArray) {
                if (currentFile.exists() && currentFile.isFile()) {
                    //文件直接删除
                    currentFile.delete();
                } else {
                    //递归删除
                    deleteFolder(currentFile);
                }
            }
            folder.delete();
        }
    }

    private void pasteFile() {
        mNewFilePath = mCurrentFilePath + File.separator + mCopyFileName;
        Log.d("syx", "copy mOldFilePath is " + mOldFilePath + " | mNewFilePath is " + mNewFilePath + " | isCopy is " + isCopy);
        if (!mOldFilePath.equals(mNewFilePath) && isCopy == true) {
            //不同路径下复制才有效
            if (!new File(mNewFilePath).exists()) {
                copyFile(mOldFilePath, mNewFilePath);
                Toast.makeText(this, "执行了粘贴", Toast.LENGTH_SHORT).show();
                initFileListInfo(mCurrentFilePath);
            } else {
                new AlertDialog.Builder(MainActivity.this)
                        .setTitle("提示")
                        .setMessage("该文件名已存在，是否要覆盖?")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                copyFile(mOldFilePath, mNewFilePath);
                                initFileListInfo(mCurrentFilePath);
                            }
                        })
                        .setNegativeButton("取消", null)
                        .show();

            }
        } else {
            Toast.makeText(this, "未复制文件", Toast.LENGTH_SHORT).show();
        }
    }

    private int i;
    private FileInputStream fis;
    private FileOutputStream fos;

    private void copyFile(String oldFile, String newFile) {
        try {
            fis = new FileInputStream(oldFile);
            fos = new FileOutputStream(newFile);
            do {
                //逐个byte读取文件，并写入另一个文件中
                if ((i = fis.read()) != -1) {
                    fos.write(i);
                }
            } while (i != -1);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // TODO: 2019/4/17 这里没在书上看到
    private RadioGroup mRadioGroup;
    private int mRadioChecked;
    public static String KEYWORD_BROADCAST = "com.zsf.file.KEYWORD_BROADCAST";
    Intent serviceIntent;

    private void searchDialog() {
        mRadioChecked = 1;//用于确定是在当前目录搜索还是在整个目录搜索的标志
        LayoutInflater inflater = LayoutInflater.from(MainActivity.this);
        final View layout = inflater.inflate(R.layout.dialog_search, null);
        mRadioGroup = layout.findViewById(R.id.rg_search);
        final RadioButton currentPathBtn = layout.findViewById(R.id.rb_current_path);
        final RadioButton wholePathBtn = layout.findViewById(R.id.rb_whole_path);

        //设置默认选择在当前路径搜索
        currentPathBtn.setChecked(true);

        mRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == currentPathBtn.getId()) {
                    mRadioChecked = 1;
                } else if (checkedId == wholePathBtn.getId()) {
                    mRadioChecked = 2;
                }
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this)
                .setTitle("搜索")
                .setView(null)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mKeyWords = ((EditText) layout.findViewById(R.id.et_search)).getText().toString();
                        if (mKeyWords.length() == 0) {
                            Toast.makeText(MainActivity.this, "关键字不能为空！", Toast.LENGTH_SHORT).show();
                            searchDialog();
                        } else {
                            if (menuPosition == 1) {
                                mPath.setText(mRootPath);
                            } else {
                                mPath.setText(mSdCard);
                            }

                            //获取用户输入的关键字并发送广播-开始
                            Intent keyWordIntent = new Intent();
                            keyWordIntent.setAction(KEYWORD_BROADCAST);
                            if (mRadioChecked == 1) {
                                //当前路径下搜索
                                keyWordIntent.putExtra("search_path", mCurrentFilePath);
                            } else {
                                //SD卡下搜索
                                keyWordIntent.putExtra("search_path", mSdCard);
                            }
                            //传递关键字
                            keyWordIntent.putExtra("keyword", mKeyWords);
                            getApplicationContext().sendBroadcast(keyWordIntent);
                            //
                            serviceIntent = new Intent("com.android.service.FILE_SEARCH_START");
                            MainActivity.this.startService(serviceIntent);
                            //开启服务，启动搜搜
                            isComeBackFromNotification = false;
                        }
                    }
                })
                .setNegativeButton("取消", null);
        builder.create().show();
    }


    private IntentFilter mFilter;
    private FileBroadcast mFileBroadcast;
    private IntentFilter mIntentFilter;
    private SearchBroadCast mServiceBroadcast;

    @Override
    protected void onStart() {
        super.onStart();
        mFilter = new IntentFilter();
        mFilter.addAction(FileService.FILE_SEARCH_COMPLETED);
        mFilter.addAction(FileService.FILE_NOTIFICATION);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(KEYWORD_BROADCAST);
        if (mFileBroadcast == null) {
            mFileBroadcast = new FileBroadcast();
        }

        if (mServiceBroadcast == null) {
            mServiceBroadcast = new SearchBroadCast();
        }

        this.registerReceiver(mFileBroadcast, mFilter);
        this.registerReceiver(mServiceBroadcast, mIntentFilter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mFileName.clear();
        mFilePaths.clear();
        this.unregisterReceiver(mFileBroadcast);
        this.unregisterReceiver(mServiceBroadcast);
    }

    private String mAction;
    public static boolean isComeBackFromNotification = false;

    class FileBroadcast extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            mAction = intent.getAction();
            //搜索完毕的广播
            if (FileService.FILE_SEARCH_COMPLETED.equals(mAction)) {
                mFileName = intent.getStringArrayListExtra("mFileNameList");
                mFilePaths = intent.getStringArrayListExtra("mFilePathList");
                Toast.makeText(context, "搜索完毕", Toast.LENGTH_SHORT).show();

                //搜索完毕弹出提示框提示用户要不要显示数据
                searchCompleteDialog("搜索完毕，是否马上显示结果？");
                getApplicationContext().stopService(serviceIntent);

            } else if (FileService.FILE_NOTIFICATION.equals(mAction)) {
                //单击通知回到当前的Activity，读取其中的信息
                String notification = intent.getStringExtra("notification");
                Toast.makeText(context, notification, Toast.LENGTH_SHORT).show();
                searchCompleteDialog("你确定要取消搜索吗？");
            }
        }


    }


    private void searchCompleteDialog(String message) {
        AlertDialog.Builder searchDialog = new AlertDialog.Builder(this)
                .setTitle("提示")
                .setMessage(message)
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (FileService.FILE_SEARCH_COMPLETED.equals(mAction)) {
                            if (mFileName.size() == 0) {
                                Toast.makeText(MainActivity.this, "无相关文件/文件夹", Toast.LENGTH_SHORT).show();
                                setListAdapter(new FileAdapter(MainActivity.this, mFileName, mFilePaths));
                            } else {
                                //显示文件列表
                                setListAdapter(new FileAdapter(MainActivity.this, mFileName, mFilePaths));
                            }
                        } else {
                            //设置搜索标志为true
                            isComeBackFromNotification = true;
                            //关闭服务，取消搜索
                            getApplicationContext().stopService(serviceIntent);
                        }

                    }
                })
                .setNegativeButton("取消", null);
        searchDialog.create();
        searchDialog.show();

    }


    @Override
    protected void onListItemClick(ListView listView, View v, int position, long id) {
        super.onListItemClick(listView, v, position, id);

        final File file = new File(mFilePaths.get(position));
        if (file.canRead()) {
            if (file.isDirectory()) {
                //文件夹
                initFileListInfo(mFilePaths.get(position));
            } else {
                String fileName = file.getName();
                String fileEnds = fileName.substring(fileName.lastIndexOf(".") + 1, fileName.length()).toLowerCase();
                if (fileEnds.equals("txt")) {
                    initProgressDialog(ProgressDialog.STYLE_HORIZONTAL);
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            openTxtFile(file.getPath());
                        }
                    }).start();

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            while (true) {
                                if (isTxtDataOk) {
                                    mProgressDialog.dismiss();
                                    executeIntent(txtData.toString(),file.getPath());
                                    break;
                                }
                                if (isCancelProgressDialog){
                                    mProgressDialog.dismiss();
                                    break;
                                }
                            }
                        }
                    }).start();
                }else if (fileEnds.equals("html")||fileEnds.equals("mht")||fileEnds.equals("htm")){
                    Intent intent = new Intent(MainActivity.this,WebActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.putExtra("filePath",file.getPath());
                    startActivity(intent);
                }else {
                    openFile(file);
                }
            }
        }else {
            Toast.makeText(this, "对不起，您的访问权限不足！", Toast.LENGTH_SHORT).show();
        }

    }


    ProgressDialog mProgressDialog;
    boolean isCancelProgressDialog = false;

    private void initProgressDialog(int style) {
        isCancelProgressDialog = false;
        mProgressDialog = new ProgressDialog(this);
        mProgressDialog.setTitle("提示");
        mProgressDialog.setMessage("正在为你解析文本数据，请稍后。。。");
        mProgressDialog.setCancelable(true);
        mProgressDialog.setButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                isComeBackFromNotification = true;
                mProgressDialog.dismiss();
            }
        });
        mProgressDialog.show();

    }

    private void openFile(File file){
        if (file.isDirectory()){
            initFileListInfo(file.getPath());
        }else {
            Intent intent = new Intent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setAction(Intent.ACTION_VIEW);
            //设置当前文件类型
            intent.setDataAndType(Uri.fromFile(file),getMIMEType(file));
            startActivity(intent);
        }
    }

    private String getMIMEType(File file){
        String type = "";
        String fileName = file.getName();
        String fileEnds = fileName.substring(fileName.lastIndexOf(".") +1,fileName.length()).toLowerCase();
        if (fileEnds.equals("m4a")||fileEnds.equals("mp3")||fileEnds.equals("mid")||fileEnds.equals("xmf")
                ||fileEnds.equals("ogg")||fileEnds.equals("wav")){
            type = "audio/*";
        }else if (fileEnds.equals("3gp")||fileEnds.equals("mp4")){
            type = "video/*";
        }else if (fileEnds.equals("jpg")||fileEnds.equals("gif")||fileEnds.equals("png")||fileEnds.equals("jpeg")
                ||fileEnds.equals("bmp")){
            type = "image/*";
        }else {
            type = "*/*";
        }
        return type;
    }

    private String txtData = "";
    private boolean isTxtDataOk = false;
    private void openTxtFile(String file){
        isTxtDataOk = false;
        try {
            FileInputStream fis = new FileInputStream(new File(file));
            StringBuilder sb = new StringBuilder();
            int m;
            while ((m = fis.read()) != -1){
                sb.append((char) m);
            }
            fis.close();
            //保存读取到数据
            txtData = sb.toString();
            isTxtDataOk = true;//读取完毕
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void executeIntent(String data,String file){
        Intent intent = new Intent(MainActivity.this,EditTxtActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra("path",file);
        intent.putExtra("title",new File(file).getName());
        intent.putExtra("data",data);
        startActivity(intent);
    }
}
