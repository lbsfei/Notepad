package com.feige.notepad;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.util.List;

import cn.bmob.v3.BmobQuery;
import cn.bmob.v3.exception.BmobException;
import cn.bmob.v3.listener.FindListener;
import cn.bmob.v3.listener.SaveListener;

/**
 * Created by Administrator on 2016/9/22 0022.
 */
public class UserLoginActivity extends Activity implements View.OnClickListener{
    private EditText et_name,et_pwd;
    private Button btn_login,btn_reg;
    private boolean result,checkName;
    private String name,pwd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userlogin);

        initView();
    }

    private void initView(){
        et_name = (EditText) findViewById(R.id.et_name);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        btn_login = (Button) findViewById(R.id.btn_login);
        //btn_login.setBackgroundColor(Color.rgb(0,255,0));
        btn_reg = (Button) findViewById(R.id.btn_reg);

        btn_login.setOnClickListener(this);
        btn_reg.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_login:
                String name = et_name.getText().toString().trim();
                String pwd = et_pwd.getText().toString().trim();
                doLogin(name,pwd);

                break;
            case R.id.btn_reg:
                Intent reg = new Intent(UserLoginActivity.this,UserRegActivity.class);
                startActivity(reg);
                finish();
                break;
            default:
                break;
        }
    }

    public boolean doLogin(String name,String pwd){
        result = false;
        if(name.equals("")) {
            Utils.toast(MyApplication.getContextObject(),"用户名不能为空");
            return result;
        }
        if(pwd.equals("")) {
            Utils.toast(MyApplication.getContextObject(),"密码不能为空");
            return result;
        }
        BmobQuery<User> bmobQuery = new BmobQuery<User>();
        bmobQuery.addWhereEqualTo("username",name);
        pwd = Utils.MD5(pwd);
        bmobQuery.addWhereEqualTo("userpassword",pwd);

       // Utils.toast(MyApplication.getContextObject(),name);

        //返回50条数据，如果不加上这条语句，默认返回10条数据
        //bmobQuery.setLimit(50);
        //执行查询方法
        bmobQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if(e==null){
                    if(object.size()>0){
                        //Log.i("===========","abc");
                        result = true;
                        MyApplication.setIs_login(true);
                        MyApplication.setUsername(et_name.getText().toString().trim());
                        Intent i = new Intent(UserLoginActivity.this,MainActivity.class);
                        startActivity(i);
                    }
                }
            }
        });
        return result;
    }





}

