package com.xbw.mvp.mysql;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xubowen on 16/8/19.
 */
public class AsyncTask_User_Info extends AsyncTask<String, String, String> {
    // mysql
    JSONParser jsonParser = new JSONParser();
    private static String url_up = Config_mysql.mysql_url_user_info;
    Context context;
    ProgressDialog dialog;
    public AsyncTask_User_Info(Context context) {
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

    @Override
    protected String doInBackground(String... p) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String message = null;
        params.add(new BasicNameValuePair("uid", p[0]));
        try {
            JSONObject json = jsonParser
                    .makeHttpRequest(url_up, "POST", params);
            Config_mysql.keyong=json.getString("keyong");
            Config_mysql.yiyong=json.getString("yiyong");
            Config_mysql.shengyu=json.getString("shengyu");
            Config_mysql.lasttime=json.getString("lasttime");
            message = json.getString("message");
        } catch (Exception e) {
            e.printStackTrace();
            message = "NONET";
        }
        return message;
    }

    protected void onPostExecute(String message) {
        dialog.dismiss();
        if (mysqlListener != null) {
            if (message.equals("1")) {
                mysqlListener.Success();
            } else {
                mysqlListener.Fail();
            }
        }
        if (message.equals("NONET")) {
            Toast.makeText(context, "网络连接失败", 8000).show();
        } else if (message.equals("1")) {
            Toast.makeText(context, "获取info成功", 8000).show();
        } else {
            Toast.makeText(context, "获取info失败", 8000).show();
        }
    }
}
