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
public class UserRegActivity extends Activity implements View.OnClickListener{
    private EditText et_name,et_pwd,et_repwd;
    private Button btn_login,btn_reg;
    private boolean result,checkName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userreg);

        initView();
    }

    private void initView(){
        et_name = (EditText) findViewById(R.id.et_name);
        et_pwd = (EditText) findViewById(R.id.et_pwd);
        et_repwd = (EditText) findViewById(R.id.et_repwd);
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
                Intent login = new Intent(UserRegActivity.this,UserLoginActivity.class);
                startActivity(login);
                finish();
                break;
            case R.id.btn_reg:
                String name = et_name.getText().toString().trim();
                String pwd = et_pwd.getText().toString().trim();
                String repwd = et_repwd.getText().toString().trim();
                doReg(name,pwd,repwd);
                break;
            default:
                break;
        }
    }

    private boolean doReg(String name,String pwd,String repwd) {
        result = false;
        if(name.equals("")) {
            Utils.toast(MyApplication.getContextObject(),"用户名不能为空");
            return result;
        }
        if(pwd.equals("")) {
            Utils.toast(MyApplication.getContextObject(),"密码不能为空");
            return result;
        }
        if(repwd.equals("")) {
            Utils.toast(MyApplication.getContextObject(),"确认密码不能为空");
            return result;
        }
        if(!repwd.equals(pwd)) {
            Utils.toast(MyApplication.getContextObject(),"密码不一致");
            return result;
        }
        if(checkUsername(name) == true){
            Utils.toast(MyApplication.getContextObject(),"用户名已存在，请重新输入！");
            return result;
        }
        final User user = new User();
        user.setUsername(name);
        pwd = Utils.MD5(pwd);
        user.setUserpassword(pwd);
        //user.setIs_login(true);


        user.save(new SaveListener<String>() {
            @Override
            public void done(String objectId, BmobException e) {
                if (e == null) {
                    Utils.toast(MyApplication.getContextObject(),"注册成功！");
                    result = true;
                    MyApplication.setIs_login(true);
                    MyApplication.setUsername(et_name.getText().toString().trim());
                    Intent i = new Intent(UserRegActivity.this,MainActivity.class);
                    startActivity(i);
                } else {
                    Utils.toast(MyApplication.getContextObject(),"注册失败，请稍后再试");
                    result = false;
                }
            }
        });
        //Utils.toast(MyApplication.getContextObject(),"--in---"+result);
        return result;

    }


    private boolean checkUsername(String name){
        checkName = false;
        BmobQuery<User> bmobQuery = new BmobQuery<User>();
        bmobQuery.addWhereEqualTo("username",name);

        Utils.toast(MyApplication.getContextObject(),name);

        //返回50条数据，如果不加上这条语句，默认返回10条数据
        //bmobQuery.setLimit(50);
        //执行查询方法
        bmobQuery.findObjects(new FindListener<User>() {
            @Override
            public void done(List<User> object, BmobException e) {
                if(e==null){
                    if(object.size()>0){
                        //Log.i("===========","abc");
                        checkName = true;
                    }
                }
            }
        });
        return checkName;
    }


}
