package com.xbw.mvp.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.xbw.mvp.R;
import com.xbw.mvp.mysql.AsyncTask_Login;
import com.xbw.mvp.mysql.Config_mysql;
public class LoginActivity extends Activity {
	private EditText mUsernameET;
	private EditText mPasswordET;
	private Button mSigninBtn;
	private Button mForget;
	private Button mSignupTV;
	private CheckBox mPasswordCB;
	private SharedPreference sharedpreference;
	private static final String CONFIG_URL_KEY = "CONFIG_URL_KEY";
	private static final String CONFIG_URL_KEYS = "CONFIG_URL_KEYS";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedpreference = new SharedPreference(this);
		boolean islogin = sharedpreference.isLogin(this.getClass()
				.getName());
		if (islogin) {
			startActivity(new Intent(this, MainActivity.class));
			finish();
		} else {
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//沉浸式状态栏
			getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//沉浸式状态栏
			setContentView(R.layout.activity_login);
			initView();
		}
	}
	private void initView()
	{
		mUsernameET = (EditText) findViewById(R.id.chat_login_username);
		mPasswordET = (EditText) findViewById(R.id.chat_login_password);
		mSigninBtn = (Button) findViewById(R.id.chat_login_signin_btn);
		mSignupTV = (Button) findViewById(R.id.chat_login_signup);
		mPasswordCB = (CheckBox) findViewById(R.id.chat_login_password_checkbox);
		mForget=(Button) findViewById(R.id.chat_forgot_password);
		mPasswordCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean arg1) {
				// TODO Auto-generated method stub
				if (arg1) {
					mPasswordCB.setChecked(true);
					//动态设置密码是否可见
					mPasswordET
							.setTransformationMethod(HideReturnsTransformationMethod
									.getInstance());
				} else {
					mPasswordCB.setChecked(false);
					mPasswordET
							.setTransformationMethod(PasswordTransformationMethod
									.getInstance());
				}
			}
		});
		mSigninBtn.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				// TODO Auto-generated method stub
				final String userName = mUsernameET.getText().toString().trim();
				final String password = mPasswordET.getText().toString().trim();
				if (TextUtils.isEmpty(userName)) {
					Toast.makeText(getApplicationContext(), "请输入用户名",
							Toast.LENGTH_SHORT).show();
				} else if (TextUtils.isEmpty(password)) {
					Toast.makeText(getApplicationContext(), "请输入密码",
							Toast.LENGTH_SHORT).show();
				} else {
					AsyncTask_Login al=new AsyncTask_Login(LoginActivity.this);
					al.setMysqlListenerz(new AsyncTask_Login.MysqlListenerz() {
						@Override
						public void Success() {
							setProxyUrl(Config_mysql.ss);
							setUserId(Config_mysql.uid);
							sharedpreference.KeepLogin(userName);//写入保持登陆状态
							startActivity(new Intent(LoginActivity.this, MainActivity.class));
							finish();
						}
						@Override
						public void Fail() {

						}
					});
					al.execute(userName,password);
				}
			}
		});
		mForget.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent mIntent = new Intent();
				mIntent.setClass(LoginActivity.this, WebActivity.class);
				mIntent.putExtra("url","http://ecfun.cc/mvp/user/resetpwd.php");
				startActivity(mIntent);
			}
		});
		mSignupTV.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent mIntent = new Intent();
				mIntent.setClass(LoginActivity.this, WebActivity.class);
				mIntent.putExtra("url","http://ecfun.cc/mvp/user/register.php");
				startActivity(mIntent);
			}
		});

	}
	void setProxyUrl(String ProxyUrl) {
		SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(CONFIG_URL_KEY, ProxyUrl);
		editor.commit();
	}
	void setUserId(String UserId) {
		SharedPreferences preferences = getSharedPreferences("userid", MODE_PRIVATE);
		SharedPreferences.Editor editor = preferences.edit();
		editor.putString(CONFIG_URL_KEYS, UserId);
		editor.commit();
	}
}
