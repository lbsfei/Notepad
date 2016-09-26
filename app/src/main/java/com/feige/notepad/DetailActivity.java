package com.feige.notepad;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.File;
import java.io.InputStream;

import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.UploadFileListener;

/**
 * Created by feifei on 2016/9/24.
 */
public class DetailActivity extends Activity implements View.OnClickListener {
    private final int REQUEST_CODE_CAPTURE_CAMEIA = 100;
    private final int REQUEST_CODE_PICK_IMAGE = 200;
    private final String TAG = "RichTextActivity";
    private Context context;
    private LinearLayout line_rootView, line_addImg;
    private InterceptLinearLayout line_intercept;
    private RichTextEditor richText;
    private EditText et_name;
    private TextView tv_back, tv_title, tv_ok;
    private boolean isKeyBoardUp, isEditTouch;// 判断软键盘的显示与隐藏
    private File mCameraImageFile;// 照相机拍照得到的图片
    private FileUtils mFileUtils;
    private String ROLE = "add";// 当前页面是新增还是查看详情 add/modify

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                        | WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        setContentView(R.layout.activity_detail);
        context = this;
        //初始化：
        initView();
        //赋值
        setVisiableAndValue();
        uploadImage();
    }

    private void uploadImage(){
        String picPath = "/storage/emulated/0/AndroidImage/IMG_2016_09_25_12_45_10.jpg";
        final BmobFile bmobFile = new BmobFile(new File(picPath));
        bmobFile.uploadblock(new UploadFileListener() {

            @Override
            public void done(BmobException e) {
                if(e==null){
                  //  bmobFile.getFileUrl()--返回的上传文件的完整地址
                    Utils.toast(MyApplication.getContextObject(),"上传文件成功:" + bmobFile.getFileUrl());
                }else{
                    Utils.toast(MyApplication.getContextObject(),"上传文件失败：" + e.getMessage());
                }

            }

            @Override
            public void onProgress(Integer value) {
                // 返回的上传进度（百分比）
            }
        });
    }


    public void initView(){
        if (getIntent() != null) {
            ROLE = getIntent().getStringExtra("role");
        }

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
        et_name.setText(getIntent().getStringExtra(NotesDB.TITLE));

        richText.insertText(getIntent().getStringExtra(NotesDB.CONTENT));

        //图片
        if(getIntent().getStringExtra(NotesDB.IMAGE_PATHS).equals("[]")){

        }else{
            String new_url = getIntent().getStringExtra(NotesDB.IMAGE_PATHS).replace("[","");

            new_url = new_url.replace("]","");
            Log.i("url===",new_url);
            boolean retval = new_url.contains(",");
            if(retval){
                String[] url = new_url.split(",");
                for(int i=0;i<url.length;i++){
                    richText.insertImage(url[i]);
                }
            }else{
                //Uri uri = new Uri(new_url);
                //richText.insertImage(mFileUtils.getFilePathFromUri(uri));
            }

        }

        //视频
        if(getIntent().getStringExtra(NotesDB.VIDEO_PATHS).equals("[]")){

        }else{
            String new_url_v = getIntent().getStringExtra(NotesDB.VIDEO_PATHS).replace("[","");
            new_url_v = new_url_v.replace("]","");

            boolean retval = new_url_v.contains(",");
            if(retval) {
                String[] url_v = new_url_v.split(",");
                for(int i=0;i<url_v.length;i++){
                    richText.insertVideo(url_v[i]);
                }
            }else{
                richText.insertVideo(new_url_v);
            }

        }

        //richText.insertImageByURL("http://baike.soso.com/p/20090711/20090711100323-24213954.jpg");

    }

    public void setVisiableAndValue(){

    }

    @Override
    public void onClick(View v) {

    }

}
