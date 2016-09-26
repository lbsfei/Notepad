package com.feige.notepad;

import android.app.Application;
import android.content.Context;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by Administrator on 2016/9/23 0023.
 * 全局类
 */
public class MyApplication extends Application {
    private static MyApplication mInstance = null;
    private static Context context;
    private static boolean is_login;

    public static String getUsername() {
        return username;
    }

    public static void setUsername(String username) {
        MyApplication.username = username;
    }

    private static String username="1";

    @Override
    public void onCreate() {
        super.onCreate();
        //获取Context
        context = getApplicationContext();
        mInstance = MyApplication.this;
        ImageLoaderConfiguration configuration = ImageLoaderConfiguration
                .createDefault(this);
        ImageLoader.getInstance().init(configuration);
    }

    //返回
    public static Context getContextObject(){
        return context;
    }

    public static void setIs_login(boolean login){
        is_login = login;
    }
    public static boolean getIs_login(){
        return is_login;
    }

}
