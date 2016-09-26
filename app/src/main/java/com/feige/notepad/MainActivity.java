package com.feige.notepad;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Button;

import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import cn.bmob.v3.BmobBatch;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.datatype.BatchResult;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.QueryListListener;
import cn.bmob.v3.listener.QueryListener;
import cn.bmob.v3.listener.SaveListener;
import cn.bmob.v3.listener.UpdateListener;

import cn.bmob.v3.Bmob;
import cn.bmob.v3.listener.UploadBatchListener;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    //private Button btn_add, btn_delete, btn_update, btn_query;
    private FloatingActionButton fab;

    private String objectId="";

    private String Bmob_AppId = "ef025a42e71fe9fab84e16d45e427587";

    private ImageView iv_login,iv_logout;
    //数据库对象
    private NotesDB notesDB;
    private SQLiteDatabase dbRead;//读取的权限
    //获取数据
    private ListView lv;
    private MyAdapter myAdapter;
    private Cursor cursor;

    private String uploadImgUrls,uploadVideoUrls;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 初始化 Bmob SDK
        // 使用时请将第二个参数Application ID替换成你在Bmob服务器端创建的Application ID
        Bmob.initialize(this, Bmob_AppId);


        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        //设置title
        toolbar.setTitle("");
        setSupportActionBar(toolbar);



        //ef025a42e71fe9fab84e16d45e427587
        // 初始化View
        initView();
        initListener();

    }

    private void initView() {
//        btn_add = (Button) findViewById(R.id.btn_add);
//        btn_delete = (Button) findViewById(R.id.btn_delete);
//        btn_update = (Button) findViewById(R.id.btn_update);
//        btn_query = (Button) findViewById(R.id.btn_query);
        fab = (FloatingActionButton) findViewById(R.id.fab);


        //登录
        iv_login = (ImageView) findViewById(R.id.toolbar_login);
        iv_logout = (ImageView) findViewById(R.id.toolbar_logout);
        //数据库实例化操作：
        lv=(ListView) findViewById(R.id.lv);
        notesDB = new NotesDB(this);
        dbRead = notesDB.getReadableDatabase();
        //
        if(MyApplication.getIs_login()){
            //隐藏
            iv_login.setVisibility(View.GONE);
            iv_logout.setVisibility(View.VISIBLE);
        }
    }

    private void initListener() {
//        btn_add.setOnClickListener(this);
//        btn_delete.setOnClickListener(this);
//        btn_update.setOnClickListener(this);
//        btn_query.setOnClickListener(this);
        iv_login.setOnClickListener(this);
        iv_logout.setOnClickListener(this);
        fab.setOnClickListener(this);


        //ListView监听事件:
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                cursor.moveToPosition(position);
                Intent i = new Intent(MainActivity.this, DetailActivity.class);
                i.putExtra(NotesDB.ID,
                        cursor.getInt(cursor.getColumnIndex(NotesDB.ID)));
                i.putExtra(NotesDB.TITLE, cursor.getString(cursor
                        .getColumnIndex(NotesDB.TITLE)));
                i.putExtra(NotesDB.CONTENT, cursor.getString(cursor
                        .getColumnIndex(NotesDB.CONTENT)));
                i.putExtra(NotesDB.IMAGE_PATHS,
                        cursor.getString(cursor.getColumnIndex(NotesDB.IMAGE_PATHS)));
                i.putExtra(NotesDB.VIDEO_PATHS,
                        cursor.getString(cursor.getColumnIndex(NotesDB.VIDEO_PATHS)));
                i.putExtra("role", "modify");
                startActivity(i);

            }

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if(id == R.id.action_edit){
            if(MyApplication.getUsername().equals("1")){
                Toast.makeText(this,"请先登录",Toast.LENGTH_LONG).show();
                return false;
            }else {
                doSync();
                Toast.makeText(this, "doSync", Toast.LENGTH_LONG).show();
                return true;
            }

        }

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if(v == iv_login){
            Toast.makeText(this,"login",Toast.LENGTH_LONG).show();
            Intent intent = new Intent(MainActivity.this,UserLoginActivity.class);
            startActivity(intent);
        }else if(v==fab){
            Toast.makeText(this,"fab",Toast.LENGTH_LONG).show();
            Intent addNew = new Intent(MainActivity.this,AddActivity.class);
            startActivity(addNew);
        }else if(v == iv_logout){
            MyApplication.setIs_login(false);
            //隐藏
            iv_logout.setVisibility(View.GONE);
            iv_login.setVisibility(View.VISIBLE);
            Toast.makeText(this,"用户已退出",Toast.LENGTH_LONG).show();
        }
    }

    //读取数据库中的数据
    public void selectDB(){
        cursor = dbRead.query(NotesDB.TABLE_NAME, null, null, null, null, null, null);
        myAdapter = new MyAdapter(this,cursor);
        lv.setAdapter(myAdapter);

    }

    //在onResume方法中读取数据
    protected void onResume(){
        super.onResume();
        selectDB();
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 1){
            Toast.makeText(this,"login1",Toast.LENGTH_LONG).show();
        }else{
            Toast.makeText(this,"login2",Toast.LENGTH_LONG).show();
        }
    }

    /**
     * 同步数据
     */
    private void doSync(){
        cursor = dbRead.query(NotesDB.TABLE_NAME, null, null, null, null, null, null);
        int count = cursor.getCount();
        List<BmobObject> notes = new ArrayList<BmobObject>();
        if(count>0&&cursor!=null&&cursor.moveToFirst()){

            do{
                uploadImgUrls="";
                uploadVideoUrls="";
                String title = cursor.getString(cursor.getColumnIndex("title"));
                String time = cursor.getString(cursor.getColumnIndex("time"));
                String content = cursor.getString(cursor.getColumnIndex("content"));
                String img_urls = cursor.getString(cursor.getColumnIndex("imagepaths"));
                String video_urls = cursor.getString(cursor.getColumnIndex("videopaths"));
                //Utils.toast(MyApplication.getContextObject(),title);

                final Notes note = new Notes();
                note.setTitle(title);
                note.setContent(content);
                note.setImgUrls(img_urls);
                note.setVideoUrls(video_urls);
                note.save(new SaveListener<String>() {
                    @Override
                    public void done(String objectId, BmobException e) {
                        if(e==null){
                            //先上传图片
                            uploadImgs(note.getImgUrls(),objectId);
                            //上传视频
                            uploadVideos(note.getVideoUrls(),objectId);

                            Utils.toast(MyApplication.getContextObject(),"创建数据成功：" + objectId);
                        }else{
                            Log.i("bmob","失败："+e.getMessage()+","+e.getErrorCode());
                        }
                    }
                });

            }while(cursor.moveToNext());

        }


    }

    private void  uploadImgs(String img_urls,final String id){
        //图片start
        if(img_urls.equals("[]")){
        }else {
            String new_img_url = img_urls.replace("[", "");
            new_img_url = new_img_url.replace("]", "");
            Log.i("url===", new_img_url);
            boolean retval = new_img_url.contains(",");
            final String[] img_filePaths;
            if (retval) {
                String[] url = new_img_url.split(",");
                img_filePaths = new String[url.length];
                for (int i = 0; i < url.length; i++) {
                    img_filePaths[i] = url[i];
                }
            } else {
                img_filePaths = new String[1];
                img_filePaths[0] = new_img_url;
            }
            Log.i("img_filePaths---",""+img_filePaths);

            //批量上传
            BmobFile.uploadBatch(img_filePaths, new UploadBatchListener() {
                @Override
                public void onSuccess(List<BmobFile> files,List<String> urls) {
                    //1、files-上传完成后的BmobFile集合，是为了方便大家对其上传后的数据进行操作，例如你可以将该文件保存到表中
                    //2、urls-上传文件的完整url地址
                    if(urls.size()==img_filePaths.length){//如果数量相等，则代表文件全部上传完成
                        //do something
                        uploadImgUrls = urls.toString();
                        Notes note = new Notes();
                        note.setImgUrls(uploadImgUrls);

                        note.update(id, new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e==null){
                                    Log.i("bmob","更新成功");
                                }else{
                                    Log.i("bmob","更新失败："+e.getMessage()+","+e.getErrorCode());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(int statuscode, String errormsg) {
                    Utils.toast(MyApplication.getContextObject(),"错误码"+statuscode +",错误描述："+errormsg);
                }

                @Override
                public void onProgress(int curIndex, int curPercent, int total,int totalPercent) {
                    //1、curIndex--表示当前第几个文件正在上传
                    //2、curPercent--表示当前上传文件的进度值（百分比）
                    //3、total--表示总的上传文件数
                    //4、totalPercent--表示总的上传进度（百分比）
                }
            });

        }

    }

    private void uploadVideos(String video_urls,final String id){
        //上传视频
        if(video_urls.equals("[]")){
        }else {
            String new_video_url = video_urls.replace("[", "");
            new_video_url = new_video_url.replace("]", "");
            Log.i("url===", new_video_url);
            boolean retval = new_video_url.contains(",");
            final String[] video_filePaths;
            if (retval) {
                String[] url = new_video_url.split(",");
                video_filePaths = new String[url.length];
                for (int i = 0; i < url.length; i++) {
                    video_filePaths[i] = url[i];
                }
            } else {
                video_filePaths = new String[1];
                video_filePaths[0] = new_video_url;
            }

            //批量上传
            BmobFile.uploadBatch(video_filePaths, new UploadBatchListener() {

                @Override
                public void onSuccess(List<BmobFile> files,List<String> urls) {
                    //1、files-上传完成后的BmobFile集合，是为了方便大家对其上传后的数据进行操作，例如你可以将该文件保存到表中
                    //2、urls-上传文件的完整url地址
                    if(urls.size()==video_filePaths.length){//如果数量相等，则代表文件全部上传完成
                        //do something
                        uploadVideoUrls = urls.toArray().toString();
                        //update
                        Notes note = new Notes();
                        note.setImgUrls(uploadVideoUrls);

                        note.update(id, new UpdateListener() {
                            @Override
                            public void done(BmobException e) {
                                if(e==null){
                                    Log.i("bmob","更新成功");
                                }else{
                                    Log.i("bmob","更新失败："+e.getMessage()+","+e.getErrorCode());
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(int statuscode, String errormsg) {
                    Utils.toast(MyApplication.getContextObject(),"错误码"+statuscode +",错误描述："+errormsg);
                }

                @Override
                public void onProgress(int curIndex, int curPercent, int total,int totalPercent) {
                    //1、curIndex--表示当前第几个文件正在上传
                    //2、curPercent--表示当前上传文件的进度值（百分比）
                    //3、total--表示总的上传文件数
                    //4、totalPercent--表示总的上传进度（百分比）
                }
            });

        }
    }

    private String setLocalDBId(){
        getBmobUserId(MyApplication.getUsername());
        return mBmobUserId;
    }
    private void getBmobUserId(String name){
        BmobQuery<User> bmobQuery = new BmobQuery<User>();
        bmobQuery.addWhereEqualTo("username",name);
        Utils.toast(MyApplication.getContextObject(),name);
        bmobQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if(e==null){
                    if(object.size()>0){
                        for (User Lost : object) {
                            //获得数据的objectId信息
                            mBmobUserId = Lost.getObjectId();
                        }
                    }


                }
            }
        });

    }

    private String mBmobUserId;

}
