package com.xbw.mvp.mysql;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xubowen on 16/8/17.
 */
public class AsyncTask_Login extends AsyncTask<String, String, String> {
    JSONParser jsonParser = new JSONParser();
    private static String url_up = Config_mysql.URLPATH;
    Context context;
    ProgressDialog dialog;

    public AsyncTask_Login(Context context) {
        this.context = context;
    }

    public interface MysqlListenerz {
        public void Success();

        public void Fail();
    }

    private MysqlListenerz mysqlListener = null;

    public void setMysqlListenerz(MysqlListenerz mysqlListener) {
        this.mysqlListener = mysqlListener;
    }

    protected void onPreExecute() {
        super.onPreExecute();
        dialog = Progress_Dialog.CreateProgressDialog(context);
        dialog.show();
    }

    @SuppressWarnings("deprecation")
    @Override
    protected String doInBackground(String... p) {
        // TODO 自动生成的方法存根
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String code = null;
        params.add(new BasicNameValuePair("email", p[0]));
        params.add(new BasicNameValuePair("passwd", p[1]));
        try {
            JSONObject json = jsonParser.makeHttpRequest(url_up,
                    "POST", params);
            Config_mysql.ss=json.getString("msg");
            Config_mysql.uid=json.getString("uid");
            code = json.getString("code");
        } catch (Exception e) {
            e.printStackTrace();
            code = "2";
        }
        return code;
    }

    @SuppressLint("ShowToast")
    protected void onPostExecute(String message) {
        dialog.dismiss();
        if (mysqlListener != null) {
            if (message.equals("1")) {
                mysqlListener.Success();
            } else {
                mysqlListener.Fail();
            }
        }

        if (message.equals("2")) {
            Toast.makeText(context, "网络连接失败", 8000).show();
        } else if (message.equals("1")) {
            Toast.makeText(context, "登录成功", 8000).show();
        } else {
            Toast.makeText(context, "邮箱或者密码错误", 8000).show();
        }
    }


}

