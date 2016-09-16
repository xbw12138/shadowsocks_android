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
 * Created by xubowen on 16/8/17.
 */
public class AsyncTask_User_Token extends AsyncTask<String, String, String> {
    // mysql
    JSONParser jsonParser = new JSONParser();
    private static String url_up = Config_mysql.mysql_url_user_token;
    Context context;

    public AsyncTask_User_Token(Context context) {
        this.context = context;
    }

    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected String doInBackground(String... p) {
        List<NameValuePair> params = new ArrayList<NameValuePair>();
        String message = null;
        params.add(new BasicNameValuePair("user_phone", p[0]));
        params.add(new BasicNameValuePair("user_token", p[1]));
        try {
            JSONObject json = jsonParser
                    .makeHttpRequest(url_up, "POST", params);
            message = json.getString("message");
        } catch (Exception e) {
            e.printStackTrace();
            message = "NONET";
        }
        return message;
    }

    protected void onPostExecute(String message) {
        if (message.equals("NONET")) {
            Toast.makeText(context, "网络连接失败", 8000).show();
        } else if (message.equals("YES")) {
            Toast.makeText(context, "获取token成功", 8000).show();
        } else {
            Toast.makeText(context, "获取token失败", 8000).show();
        }
    }
}
