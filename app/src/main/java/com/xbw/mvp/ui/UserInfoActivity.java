package com.xbw.mvp.ui;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.xbw.mvp.R;
import com.xbw.mvp.mysql.AsyncTask_User_Info;
import com.xbw.mvp.mysql.Config_mysql;

/**
 * Created by xubowen on 16/8/19.
 */
public class UserInfoActivity extends Activity{

    private TextView tx1;
    private TextView tx2;
    private TextView tx3;
    private TextView tx4;
    private static final String CONFIG_URL_KEYS = "CONFIG_URL_KEYS";
    private static final String CONFIG_URL_KEYYiYong = "CONFIG_URL_KEYYiYong";
    private static final String CONFIG_URL_KEYShengYu = "CONFIG_URL_KEYShengYu";
    private static final String CONFIG_URL_KEYKeYong = "CONFIG_URL_KEYKeYong";
    private static final String CONFIG_URL_KEYLastTime = "CONFIG_URL_KEYLastTime";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_userinfo);
        tx1=(TextView)findViewById(R.id.textView5);
        tx2=(TextView)findViewById(R.id.textView6);
        tx3=(TextView)findViewById(R.id.textView7);
        tx4=(TextView)findViewById(R.id.textView8);
        tx1.setText(readyiyong());
        tx2.setText(readkeyong());
        tx3.setText(readshengyu());
        tx4.setText(readlasttime());
        AsyncTask_User_Info ui=new AsyncTask_User_Info(this);
        ui.setMysqlListenerz(new AsyncTask_User_Info.MysqlListenerz() {
            @Override
            public void Success() {
                setInfo(Config_mysql.yiyong,Config_mysql.keyong,Config_mysql.shengyu,Config_mysql.lasttime);
                tx1.setText(Config_mysql.yiyong);
                tx2.setText(Config_mysql.keyong);
                tx3.setText(Config_mysql.shengyu);
                tx4.setText(Config_mysql.lasttime);
            }
            @Override
            public void Fail() {

            }
        });
        ui.execute(readUserId());

    }
    String readyiyong(){
        SharedPreferences preferences = getSharedPreferences("shadowsocksinfo", MODE_PRIVATE);
        return preferences.getString(CONFIG_URL_KEYYiYong, "");
    }
    String readkeyong(){
        SharedPreferences preferences = getSharedPreferences("shadowsocksinfo", MODE_PRIVATE);
        return preferences.getString(CONFIG_URL_KEYKeYong, "");
    }
    String readshengyu(){
        SharedPreferences preferences = getSharedPreferences("shadowsocksinfo", MODE_PRIVATE);
        return preferences.getString(CONFIG_URL_KEYShengYu, "");
    }
    String readlasttime(){
        SharedPreferences preferences = getSharedPreferences("shadowsocksinfo", MODE_PRIVATE);
        return preferences.getString(CONFIG_URL_KEYLastTime, "");
    }
    String readUserId() {
        SharedPreferences preferences = getSharedPreferences("userid", MODE_PRIVATE);
        return preferences.getString(CONFIG_URL_KEYS, "");
    }
    void setInfo(String yiyong,String keyong,String shengyu,String lasttime) {
        SharedPreferences preferences = getSharedPreferences("shadowsocksinfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(CONFIG_URL_KEYYiYong, yiyong);
        editor.putString(CONFIG_URL_KEYKeYong, keyong);
        editor.putString(CONFIG_URL_KEYShengYu, shengyu);
        editor.putString(CONFIG_URL_KEYLastTime, lasttime);
        editor.commit();
    }
}
