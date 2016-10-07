package com.feige.notepad;


import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;

import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.SaveListener;


/**
 * Created by Administrator on 2016/9/23 0023.
 */

@SuppressLint("SimpleDateFormat")
public class AddActivity extends Activity implements OnClickListener {
    private final int REQUEST_CODE_CAPTURE_CAMEIA = 100;
    private final int REQUEST_CODE_PICK_IMAGE = 200;
    private final int REQUEST_CODE_PICK_VIDEO = 300;
    private final String TAG = "RichTextActivity";
    private Context context;
    private LinearLayout line_rootView, line_addImg;
    private InterceptLinearLayout line_intercept;
    private RichTextEditor richText;
    private EditText et_name;
    private TextView tv_back, tv_title, tv_ok;
    private boolean isKeyBoardUp, isEditTouch;
    private File mCameraImageFile;//照相机拍照得到的图片
    private File mVideoFile;
    private FileUtils mFileUtils;
    private String ROLE = "add";
    //数据库对象
    private NotesDB notesDB;
    private SQLiteDatabase dbWriter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.activity_add);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_add);
        context = this;
        initView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_ok:
                if ("修改".equals(tv_ok.getText())) {
                    tv_ok.setText("提交");
                    line_intercept.setIntercept(false);
                    richText.setIntercept(false);
                } else {
//                    Log.i(TAG, "---richtext-data:" + richText.getRichEditData());
//                    Toast.makeText(context, "start", Toast.LENGTH_LONG)
//                            .show();
                    //richtext-data:{text=如, videoUrls=[/storage/emulated/0/AndroidImage/VIDEO_2016_09_24_19_38_41.mp4], imgUrls=[/storage/emulated/0/AndroidImage/IMG_2016_09_24_19_40_05.jpg]}
                    String title = et_name.getText().toString().trim();
                    if(!title.equals("")) {
                        doInsertLocalDB(richText.getRichEditData(), title);
                        //上传到BMOB
                        //doInsertBmobDB(richText.getRichEditData(), title);
                    }else{
                        Utils.toast(MyApplication.getContextObject(),"标题不能为空");
                        return;
                    }
                }
                break;
            case R.id.tv_back:
                finish();
                break;

            case R.id.img_addPicture:
                // 打开系统相册
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");// 相片类型
                startActivityForResult(intent, REQUEST_CODE_PICK_IMAGE);
                break;
            case R.id.img_takePicture:
                // 打开相机
                openCamera();
                break;
            case R.id.img_video:
                //开摄像机
                openVideo();
                break;

        }
    }
    private void initView(){
        if (getIntent() != null)
            ROLE = getIntent().getStringExtra("role");

        mFileUtils = new FileUtils(context);

        line_addImg = (LinearLayout) findViewById(R.id.line_addImg);
        line_rootView = (LinearLayout) findViewById(R.id.line_rootView);
        line_intercept = (InterceptLinearLayout) findViewById(R.id.line_intercept);

        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_back.setOnClickListener(this);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        tv_ok.setOnClickListener(this);
        tv_title = (TextView) findViewById(R.id.tv_title);
        et_name = (EditText) findViewById(R.id.et_name);
        richText = (RichTextEditor) findViewById(R.id.richText);

        //数据库：
        notesDB = new NotesDB(this);
        dbWriter = notesDB.getWritableDatabase();

        initRichEdit();
        if ("modify".equals(ROLE)) {
            tv_ok.setText("修改");
            tv_title.setText("查看详情");
            line_intercept.setIntercept(true);
            richText.setIntercept(true);
            getData();
        } else {
            tv_ok.setText("提交");
            tv_title.setText("新增");
        }
    }
    private void initRichEdit() {
        ImageView img_addPicture, img_takePicture,img_video;
        //像片库
        img_addPicture = (ImageView) line_addImg
                .findViewById(R.id.img_addPicture);
        img_addPicture.setOnClickListener(this);
        //照相
        img_takePicture = (ImageView) line_addImg
                .findViewById(R.id.img_takePicture);
        img_takePicture.setOnClickListener(this);
        //摄像
        img_video = (ImageView) line_addImg
                .findViewById(R.id.img_video);
        img_video.setOnClickListener(this);

        et_name.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    isEditTouch = false;
                    line_addImg.setVisibility(View.GONE);
                }
            }

        });
        richText.setLayoutClickListener(new RichTextEditor.LayoutClickListener() {
            @Override
            public void layoutClick() {
                isEditTouch = true;
                line_addImg.setVisibility(View.VISIBLE);
            }
        });

        line_rootView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int heightDiff = line_rootView.getRootView()
                                .getHeight() - line_rootView.getHeight();
                        if (isEditTouch) {
                            // 大小超过500时，一般为显示虚拟键盘事件,此判断条件不唯一
                            if (heightDiff > 500) {
                                isKeyBoardUp = true;
                                line_addImg.setVisibility(View.VISIBLE);
                            } else {
                                if (isKeyBoardUp) {
                                    isKeyBoardUp = false;
                                    isEditTouch = false;
                                    line_addImg.setVisibility(View.GONE);
                                }
                            }
                        }
                    }
                });
    }

    private void getData() {
        et_name.setText("模拟几条数据");

        richText.insertText("第一行");
        richText.insertText("接下来是张图片-王宝强");
        richText.insertImageByURL("http://baike.soso.com/p/20090711/20090711100323-24213954.jpg");
        richText.insertText("下面是一副眼镜");
        richText.insertImageByURL("http://img4.3lian.com/sucai/img6/230/29.jpg");
        richText.insertImageByURL("http://pic9.nipic.com/20100812/3289547_144304019987_2.jpg");
        richText.insertText("上面是一个树妖");
        richText.insertText("最后一行");
    }
    private void openCamera() {
        try {
            File PHOTO_DIR = new File(mFileUtils.getStorageDirectory());
            if (!PHOTO_DIR.exists())
                PHOTO_DIR.mkdirs();// 创建照片的存储目录
            // 给新照的照片文件命名
            mCameraImageFile = new File(PHOTO_DIR, getPhotoFileName());
            final Intent intent = getTakePickIntent(mCameraImageFile,"img");
            startActivityForResult(intent, REQUEST_CODE_CAPTURE_CAMEIA);
        } catch (ActivityNotFoundException e) {
        }
    }

    private void openVideo(){
        try {
            File VIDEO_DIR = new File(mFileUtils.getStorageDirectory());
            if (!VIDEO_DIR.exists())
                VIDEO_DIR.mkdirs();// 创建照片的存储目录
            // 给新照的照片文件命名
            mVideoFile = new File(VIDEO_DIR, getVideoFileName());
            final Intent intent = getTakePickIntent(mVideoFile,"video");
            startActivityForResult(intent, REQUEST_CODE_PICK_VIDEO);

        } catch (ActivityNotFoundException e) {
        }
    }

    private Intent getTakePickIntent(File f,String type) {
        Intent intent;
        if(type.equals("img")){
            intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        }else{
            intent = new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
        }

        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        return intent;
    }
    private String getPhotoFileName() {
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'IMG'_yyyy_MM_dd_HH_mm_ss");
        return dateFormat.format(date) + ".jpg";
    }

    private String getVideoFileName(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                "'VIDEO'_yyyy_MM_dd_HH_mm_ss");
        return dateFormat.format(date) + ".mp4";
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }
        if (requestCode == REQUEST_CODE_PICK_IMAGE) {
            Uri uri = data.getData();
            richText.insertImage(mFileUtils.getFilePathFromUri(uri));
        } else if (requestCode == REQUEST_CODE_CAPTURE_CAMEIA
                && resultCode == Activity.RESULT_OK) {

            Toast.makeText(context, "file===="+mCameraImageFile, Toast.LENGTH_LONG)
                    .show();
            Log.i("file===",""+mCameraImageFile);
            Toast.makeText(context, "path----"+mCameraImageFile.getAbsolutePath(), Toast.LENGTH_LONG)
                    .show();
            Log.i("path===",""+mCameraImageFile.getAbsolutePath());
           // 32.340 11792-11792/com.feige.notepad I/file===: /storage/emulated/0/AndroidImage/IMG_2016_09_24_18_18_18.jpg
            //09-24 18:18:32.380 11792-11792/com.feige.notepad I/path===: /storage/emulated/0/AndroidImage/IMG_2016_09_24_18_18_18.jpg

            richText.insertImage(mCameraImageFile.getAbsolutePath());
        }else if (requestCode == REQUEST_CODE_PICK_VIDEO
                && resultCode == Activity.RESULT_OK) {

            Log.i("file===v",""+mVideoFile);
            Log.i("path===v",""+mVideoFile.getAbsolutePath());
           // /com.feige.notepad I/file===v: /storage/emulated/0/AndroidImage/VIDEO_2016_09_24_18_22_02.mp4
           // 09-24 18:22:29.960 18814-18814/com.feige.notepad I/path===v: /storage/emulated/0/AndroidImage/VIDEO_2016_09_24_18_22_02.mp4

           richText.insertVideo(mVideoFile.getAbsolutePath());
        }

    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void doInsertBmobDB(HashMap<String, Object> hm,String title){
//{
// text=如真好玩,
// videoUrls=[/storage/emulated/0/AndroidImage/VIDEO_2016_09_24_19_38_41.mp4],
// imgUrls=[/storage/emulated/0/AndroidImage/IMG_2016_09_24_19_40_05.jpg,
// /system/media/Pre-loaded/Pictures/Picture_03_Pattens.jpg, /storage/emulated/0/AndroidImage/IMG_2016_09_24_19_42_20.jpg]
// }
        HashMap<String, Object> data = hm;
        StringBuilder content = (StringBuilder) data.get("text");
        List<String> imgUrls = (List<String>) data.get("imgUrls");
        List<String> videoUrls = (List<String>) data.get("videoUrls");
//        Log.i("text===",str+"-----"+title);
//        Log.i("imgUrls===",imgUrls.toString());
//        Log.i("videoUrls===",videoUrls.toString());

        final Notes note = new Notes();
        note.setTitle(title);
        note.setContent(content.toString());
        note.setImgUrls(imgUrls.toString());
        note.setVideoUrls(videoUrls.toString());
        note.save(new SaveListener<String>() {
            @Override
            public void done(String objectId,BmobException e) {
                if(e==null){
                    Utils.toast(MyApplication.getContextObject(),"添加数据成功，返回objectId为："+objectId);
                    Intent i = new Intent(AddActivity.this,MainActivity.class);
                    startActivity(i);
                    finish();
                }else{
                    Utils.toast(MyApplication.getContextObject(),"创建数据失败：" + e.getMessage());
                }
            }
        });

    }

    //本地DB
    private void doInsertLocalDB(HashMap<String, Object> hm,String title) {
        HashMap<String, Object> data = hm;
        StringBuilder content = (StringBuilder) data.get("text");
        List<String> imgUrls = (List<String>) data.get("imgUrls");
        List<String> videoUrls = (List<String>) data.get("videoUrls");

        ContentValues cv = new ContentValues();
        cv.put(NotesDB.TITLE, title);
        cv.put(NotesDB.CONTENT, content.toString());
        cv.put(NotesDB.TIME,getTime());
        //添加图片
        cv.put(NotesDB.IMAGE_PATHS, imgUrls.toString());
        //cv.put(NotesDB.IMAGE_PATHS, imgUrls.toArray().toString());
        //添加视频
        cv.put(NotesDB.VIDEO_PATHS, videoUrls.toString());
        dbWriter.insert(NotesDB.TABLE_NAME, null, cv);
        Utils.toast(MyApplication.getContextObject(),"添加数据成功");
        Intent i = new Intent(AddActivity.this,MainActivity.class);
        startActivity(i);
        finish();
    }

    //添加数据的时间
    public String getTime(){
        SimpleDateFormat formatTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date curDate = new Date();
        String str = formatTime.format(curDate);
        return str;
    }


}
