package com.xbw.mvp.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.cpr.VideoEnabledWebChromeClient;
import com.cpr.VideoEnabledWebView;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.RecognizerResult;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.ui.RecognizerDialog;
import com.iflytek.cloud.ui.RecognizerDialogListener;
import com.iflytek.speech.util.JsonParser;
import com.opendanmaku.DanmakuView;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;
import com.xbw.mvp.R;
import com.xbw.mvp.core.LocalVpnService;
import com.xbw.mvp.mysql.AsyncTask_Insert_Danmu;
import com.xbw.mvp.mysql.AsyncTask_User_Info;
import com.xbw.mvp.mysql.AsyncTask_User_Token;

import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;

import com.iflytek.cloud.SpeechRecognizer;
import com.iflytek.cloud.RecognizerListener;
import com.xbw.mvp.mysql.Config_mysql;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity implements
        View.OnClickListener,
        OnCheckedChangeListener,
        LocalVpnService.onStatusChangedListener {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String CONFIG_URL_KEY = "CONFIG_URL_KEY";
    private static final String CONFIG_URL_KEYS = "CONFIG_URL_KEYS";
    private static final String CONFIG_URL_KEYYiYong = "CONFIG_URL_KEYYiYong";
    private static final String CONFIG_URL_KEYShengYu = "CONFIG_URL_KEYShengYu";
    private static final String CONFIG_URL_KEYKeYong = "CONFIG_URL_KEYKeYong";
    private static final String CONFIG_URL_KEYLastTime = "CONFIG_URL_KEYLastTime";
    private ToggleButton switchProxy;
    private Calendar mCalendar;
    private VideoEnabledWebView webView;
    private VideoEnabledWebChromeClient webChromeClient;
    private SharedPreference sharedpreference;
    public ProgressBar pb;
    private SwipeRefreshLayout swipeLayout;
    public static DanmakuView mDanmakuView;
    private ImageView sendDanmu;
    private ImageView a;
    private ImageView b;
    private ImageView c;
    private ImageView d;
    private String danmucontent = "";
    // 语音听写对象
    private SpeechRecognizer mIat;
    // 语音听写UI
    private RecognizerDialog mIatDialog;
    // 引擎类型
    private String mEngineType = SpeechConstant.TYPE_CLOUD;
    private Toast mToast;
    float mPosX = 0;
    float mPosY = 0;
    float mCurPosX = 0;
    float mCurPosY = 0;
    private String url="";
    private FloatingActionsMenu FAM;
    // 用HashMap存储听写结果
    private HashMap<String, String> mIatResults = new LinkedHashMap<String, String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow()
                .addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);// 沉浸式状态栏
        getWindow().addFlags(
                WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);// 沉浸式状态栏
        setContentView(R.layout.activity_main);
        switchProxy = (ToggleButton) findViewById(R.id.switch1);
        webView = (VideoEnabledWebView) findViewById(R.id.webView);
        FAM=(FloatingActionsMenu)findViewById(R.id.multiple_actions);
        a = (ImageView) findViewById(R.id.action_a);
        b = (ImageView) findViewById(R.id.action_b);
        c = (ImageView) findViewById(R.id.action_c);
        d = (ImageView) findViewById(R.id.action_d);
        switchProxy.setChecked(LocalVpnService.IsRunning);
        switchProxy.setOnCheckedChangeListener(this);
        findViewById(R.id.imageButton).setOnClickListener(this);
        mCalendar = Calendar.getInstance();
        LocalVpnService.addOnStatusChangedListener(this);
        sharedpreference = new SharedPreference(this);
        AsyncTask_Insert_Danmu dan = new AsyncTask_Insert_Danmu(MainActivity.this, false);
        dan.execute(sharedpreference.getID(), "上线啦！！！", "one");
        initWeb();
        initXGPush();
        initUI();
        //下拉刷新
        swipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        swipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
                                             @Override
                                             public void onRefresh() {
                                                 //重新刷新页面
                                                 webView.loadUrl(webView.getUrl());
                                                 pb.setVisibility(View.VISIBLE);
                                                 webView.setWebViewClient(new InsideWebViewClient());
                                                 webView.setWebChromeClient(webChromeClient);
                                             }
                                         }

        );
        swipeLayout.setColorScheme(R.color.holo_blue_bright,
                R.color.holo_green_light, R.color.holo_orange_light,
                R.color.holo_red_light);

        a.setOnClickListener(new android.view.View.OnClickListener()

                             {
                                 @Override
                                 public void onClick(View v) {
                                     FAM.collapse();
                                     mIatResults.clear();
                                     danmucontent = "";
                                     // 设置参数
                                     setParam();
                                     boolean isShowDialog = true;
                                     if (isShowDialog) {
                                         // 显示听写对话框
                                         mIatDialog.setListener(mRecognizerDialogListener);
                                         mIatDialog.show();
                                         showTip(getString(R.string.text_begin));
                                     } else {
                                         // 不显示听写对话框
                                         ret = mIat.startListening(mRecognizerListener);
                                         if (ret != ErrorCode.SUCCESS) {
                                             showTip("听写失败,错误码：" + ret);
                                         } else {
                                             showTip(getString(R.string.text_begin));
                                         }
                                     }
                                 }
                             }

        );
        b.setOnClickListener(new android.view.View.OnClickListener()

                             {
                                 @Override
                                 public void onClick(View v) {
                                     FAM.collapse();
                                     final EditText editText = new EditText(MainActivity.this);
                                     editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
                                     editText.setHint("tan~tan~tan");
                                     new AlertDialog.Builder(MainActivity.this)
                                             .setTitle("写弹幕")
                                             .setView(editText)
                                             .setPositiveButton(R.string.btn_ok, new android.content.DialogInterface.OnClickListener() {
                                                 @Override
                                                 public void onClick(DialogInterface dialog, int which) {
                                                     if (editText.getText() == null) {
                                                         return;
                                                     }
                                                     AsyncTask_Insert_Danmu dan = new AsyncTask_Insert_Danmu(MainActivity.this, false);
                                                     dan.execute(sharedpreference.getID(), editText.getText().toString().trim(), "two");
                                                 }
                                             })
                                             .setNegativeButton(R.string.btn_cancel, null)
                                             .show();
                                 }
                             }

        );
        c.setOnClickListener(new android.view.View.OnClickListener()

                             {
                                 @Override
                                 public void onClick(View v) {
                                     FAM.collapse();
                                     startActivity(new Intent(MainActivity.this, UserInfoActivity.class));
                                 }
                             }

        );
        d.setOnClickListener(new android.view.View.OnClickListener()

                             {
                                 @Override
                                 public void onClick(View v) {
                                     if (switchProxy.isChecked()) {
                                         return;
                                     }
                                     FAM.collapse();
                                     new AlertDialog.Builder(MainActivity.this)
                                             .setTitle(R.string.config_url)
                                             .setItems(new CharSequence[]{
                                                     getString(R.string.config_url_scan),
                                                     getString(R.string.config_url_manual)
                                             }, new android.content.DialogInterface.OnClickListener() {
                                                 @Override
                                                 public void onClick(DialogInterface dialogInterface, int i) {
                                                     switch (i) {
                                                         case 0:
                                                             scanForProxyUrl();
                                                             break;
                                                         case 1:
                                                             showProxyUrlInputDialog();
                                                             break;
                                                     }
                                                 }
                                             })
                                             .show();
                                 }
                             }

        );

    }

    private void initXGPush() {
        XGPushManager.registerPush(this, new XGIOperateCallback() {
            @Override
            public void onSuccess(Object data, int flag) {
                Log.d("TPush", "注册成功，设备token为：" + data);
                AsyncTask_User_Token an = new AsyncTask_User_Token(MainActivity.this);
                an.execute(sharedpreference.getID(), data + "");
            }

            @Override
            public void onFail(Object data, int errCode, String msg) {
                Log.d("TPush", "注册失败，错误码：" + errCode + ",错误信息：" + msg);
            }
        });
    }

    int ret = 0;

    private void initUI() {
        mDanmakuView = (DanmakuView) findViewById(R.id.danmakuView);//弹幕
        sendDanmu = (ImageView) findViewById(R.id.imageView5);//发送弹幕
        mIat = SpeechRecognizer.createRecognizer(MainActivity.this, mInitListener);
        // 初始化听写Dialog，如果只使用有UI听写功能，无需创建SpeechRecognizer
        // 使用UI听写功能，请根据sdk文件目录下的notice.txt,放置布局文件和图片资源
        mIatDialog = new RecognizerDialog(MainActivity.this, mInitListener);
        mToast = Toast.makeText(this, "", Toast.LENGTH_SHORT);
        sendDanmu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIatResults.clear();
                danmucontent = "";
                // 设置参数
                setParam();
                boolean isShowDialog = true;
                if (isShowDialog) {
                    // 显示听写对话框
                    mIatDialog.setListener(mRecognizerDialogListener);
                    mIatDialog.show();
                    showTip(getString(R.string.text_begin));
                } else {
                    // 不显示听写对话框
                    ret = mIat.startListening(mRecognizerListener);
                    if (ret != ErrorCode.SUCCESS) {
                        showTip("听写失败,错误码：" + ret);
                    } else {
                        showTip(getString(R.string.text_begin));
                    }
                }
            }
        });
    }

    String readProxyUrl() {
        SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE);
        return preferences.getString(CONFIG_URL_KEY, "");
    }

    void setProxyUrl(String ProxyUrl) {
        SharedPreferences preferences = getSharedPreferences("shadowsocksProxyUrl", MODE_PRIVATE);
        Editor editor = preferences.edit();
        editor.putString(CONFIG_URL_KEY, ProxyUrl);
        editor.commit();
    }

    String getVersionName() {
        PackageManager packageManager = getPackageManager();
        if (packageManager == null) {
            Log.e(TAG, "null package manager is impossible");
            return null;
        }

        try {
            return packageManager.getPackageInfo(getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "package not found is impossible", e);
            return null;
        }
    }

    boolean isValidUrl(String url) {
        try {
            if (url == null || url.isEmpty())
                return false;

            if (url.startsWith("ss://")) {//file path
                return true;
            } else { //url
                Uri uri = Uri.parse(url);
                if (!"http".equals(uri.getScheme()) && !"https".equals(uri.getScheme()))
                    return false;
                if (uri.getHost() == null)
                    return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onClick(View v) {
        if (switchProxy.isChecked()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.config_url)
                .setItems(new CharSequence[]{
                        getString(R.string.config_url_scan),
                        getString(R.string.config_url_manual)
                }, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        switch (i) {
                            case 0:
                                scanForProxyUrl();
                                break;
                            case 1:
                                showProxyUrlInputDialog();
                                break;
                        }
                    }
                })
                .show();
    }

    private void scanForProxyUrl() {
        //开启扫描二维码
        Intent intent = new Intent();
        intent.setClass(this, SaoYiSao.class);
        startActivityForResult(intent, 1001);
    }

    private void showProxyUrlInputDialog() {
        final EditText editText = new EditText(this);
        editText.setInputType(InputType.TYPE_TEXT_VARIATION_URI);
        editText.setHint(getString(R.string.config_url_hint));
        editText.setText(readProxyUrl());

        new AlertDialog.Builder(this)
                .setTitle(R.string.config_url)
                .setView(editText)
                .setPositiveButton(R.string.btn_ok, new android.content.DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (editText.getText() == null) {
                            return;
                        }

                        String ProxyUrl = editText.getText().toString().trim();
                        if (isValidUrl(ProxyUrl)) {
                            setProxyUrl(ProxyUrl);
                        } else {
                            Toast.makeText(MainActivity.this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(R.string.btn_cancel, null)
                .show();
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onLogReceived(String logString) {
        mCalendar.setTimeInMillis(System.currentTimeMillis());
        logString = String.format("[%1$02d:%2$02d:%3$02d] %4$s\n",
                mCalendar.get(Calendar.HOUR_OF_DAY),
                mCalendar.get(Calendar.MINUTE),
                mCalendar.get(Calendar.SECOND),
                logString);

        System.out.println(logString);
    }

    @Override
    public void onStatusChanged(String status, Boolean isRunning) {
        switchProxy.setEnabled(true);
        switchProxy.setChecked(isRunning);
        onLogReceived(status);
        if (isRunning) {
            url="";
            webView.loadUrl("http://m.youtube.com");
        } else {
            url="file:///android_asset/wechat/index.html";
            webView.loadUrl(url);
        }
        Toast.makeText(this, status, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (LocalVpnService.IsRunning != isChecked) {
            switchProxy.setEnabled(false);
            if (isChecked) {
                Intent intent = LocalVpnService.prepare(this);
                if (intent == null) {
                    startVPNService();
                } else {
                    startActivityForResult(intent, 1001);
                }
            } else {
                LocalVpnService.IsRunning = false;
            }
        }
    }

    private void startVPNService() {
        String ProxyUrl = readProxyUrl();
        if (!isValidUrl(ProxyUrl)) {
            Toast.makeText(this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
            switchProxy.post(new Runnable() {
                @Override
                public void run() {
                    switchProxy.setChecked(false);
                    switchProxy.setEnabled(true);
                }
            });
            return;
        }
        onLogReceived("starting...");
        LocalVpnService.ProxyUrl = ProxyUrl;
        startService(new Intent(this, LocalVpnService.class));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (resultCode == 1001) {
            String result_value = intent.getStringExtra("describe");
            if (isValidUrl(result_value)) {
                setProxyUrl(result_value);
                Toast.makeText(MainActivity.this, "已填写配置［" + result_value + "］请点击启动", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, R.string.err_invalid_url, Toast.LENGTH_SHORT).show();
            }
            return;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_actions, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_youtube:
                url="";
                webView.loadUrl("http://m.youtube.com");
                return true;
            case R.id.menu_item_facebook:
                url="";
                webView.loadUrl("http://www.facebook.com");
                return true;
            case R.id.menu_item_twitter:
                url="";
                webView.loadUrl("http://mobile.twitter.com");
                return true;
            case R.id.menu_item_google:
                url="";
                webView.loadUrl("http://www.google.com");
                return true;
            case R.id.menu_item_about:
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.app_name) + getVersionName())
                        .setMessage(R.string.about_info)
                        .setPositiveButton(R.string.btn_ok, null)
                        .setNegativeButton(R.string.btn_more, new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://v.ecfun.cc")));
                            }
                        })
                        .show();

                return true;
            case R.id.menu_item_logout:
                sharedpreference.DisconnectLogin();
                setProxyUrl("");
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
                return true;
            case R.id.menu_item_exit:
                if (!LocalVpnService.IsRunning) {
                    finish();
                    return true;
                }

                new AlertDialog.Builder(this)
                        .setTitle(R.string.menu_item_exit)
                        .setMessage(R.string.exit_confirm_info)
                        .setPositiveButton(R.string.btn_ok, new android.content.DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                LocalVpnService.IsRunning = false;
                                LocalVpnService.Instance.disconnectVPN();
                                stopService(new Intent(MainActivity.this, LocalVpnService.class));
                                System.runFinalization();
                                System.exit(0);
                            }
                        })
                        .setNegativeButton(R.string.btn_cancel, null)
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onDestroy() {
        LocalVpnService.removeOnStatusChangedListener(this);
        mDanmakuView.clear();
        AsyncTask_Insert_Danmu dan = new AsyncTask_Insert_Danmu(MainActivity.this, false);
        dan.execute(sharedpreference.getID(), "下线啦！！！", "one");
        mIat.cancel();
        mIat.destroy();
        super.onDestroy();
    }

    private void initWeb() {
        //进度条显示
        pb = (ProgressBar) findViewById(R.id.pb);
        pb.setMax(100);
        View nonVideoLayout = findViewById(R.id.nonVideoLayout);
        ViewGroup videoLayout = (ViewGroup) findViewById(R.id.videoLayout);
        View loadingView = getLayoutInflater().inflate(R.layout.view_loading_video, null);
        webChromeClient = new VideoEnabledWebChromeClient(nonVideoLayout, videoLayout, loadingView, webView) {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                pb.setProgress(newProgress);
                if (newProgress == 100) {
                    //加载完成刷新图标消失
                    swipeLayout.setRefreshing(false);
                    pb.setVisibility(View.GONE);
                }
                super.onProgressChanged(view, newProgress);
            }
        };
        webChromeClient.setOnToggledFullscreen(new VideoEnabledWebChromeClient.ToggledFullscreenCallback() {
            @Override
            public void toggledFullscreen(boolean fullscreen) {
                if (fullscreen) {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags |= WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
                    }
                } else {
                    WindowManager.LayoutParams attrs = getWindow().getAttributes();
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_FULLSCREEN;
                    attrs.flags &= ~WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
                    getWindow().setAttributes(attrs);
                    if (android.os.Build.VERSION.SDK_INT >= 14) {
                        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                    }
                }
            }
        });
        webView.setWebChromeClient(webChromeClient);
        webView.setWebViewClient(new InsideWebViewClient());
        url="file:///android_asset/wechat/index.html";
        webView.loadUrl(url);
    }

    private class InsideWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            pb.setVisibility(View.VISIBLE);
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            // TODO Auto-generated method stub
            super.onReceivedError(view, errorCode, description, failingUrl);
            Toast.makeText(MainActivity.this, "服务未开启，请重试", Toast.LENGTH_SHORT).show();
            url="file:///android_asset/wechat/index.html";
            webView.loadUrl(url);
        }

    }

    @Override
    public void onBackPressed() {
        if (!webChromeClient.onBackPressed()) {
            if (webView.canGoBack()) {
                webView.goBack();
            } else {
                super.onBackPressed();
            }
        }
    }

    /**
     * 初始化监听器。
     */
    private InitListener mInitListener = new InitListener() {

        @Override
        public void onInit(int code) {
            //Log.d(TAG, "SpeechRecognizer init() code = " + code);
            if (code != ErrorCode.SUCCESS) {
                showTip("初始化失败，错误码：" + code);
            }
        }
    };
    /**
     * 听写监听器。
     */
    private RecognizerListener mRecognizerListener = new RecognizerListener() {
        @Override
        public void onBeginOfSpeech() {
            // 此回调表示：sdk内部录音机已经准备好了，用户可以开始语音输入
            showTip("开始说话");
        }

        @Override
        public void onError(SpeechError error) {
            // Tips：
            // 错误码：10118(您没有说话)，可能是录音机权限被禁，需要提示用户打开应用的录音权限。
            // 如果使用本地功能（语记）需要提示用户开启语记的录音权限。
            showTip(error.getPlainDescription(true));
        }

        @Override
        public void onEndOfSpeech() {
            // 此回调表示：检测到了语音的尾端点，已经进入识别过程，不再接受语音输入
            showTip("结束说话");
        }

        @Override
        public void onResult(RecognizerResult results, boolean isLast) {
        }

        @Override
        public void onVolumeChanged(int volume, byte[] data) {
            showTip("当前正在说话，音量大小：" + volume);
        }

        @Override
        public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        }
    };

    private void printResult(RecognizerResult results) {
        String text = JsonParser.parseIatResult(results.getResultString());
        String sn = null;
        // 读取json结果中的sn字段
        try {
            JSONObject resultJson = new JSONObject(results.getResultString());
            sn = resultJson.optString("sn");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mIatResults.put(sn, text);
        StringBuffer resultBuffer = new StringBuffer();
        for (String key : mIatResults.keySet()) {
            resultBuffer.append(mIatResults.get(key));
        }
        danmucontent = resultBuffer.toString();
    }

    /**
     * 听写UI监听器
     */
    private RecognizerDialogListener mRecognizerDialogListener = new RecognizerDialogListener() {
        public void onResult(RecognizerResult results, boolean isLast) {
            printResult(results);
            if (isLast) {
                // TODO 最后的结果
                //	printResult(results);
                AsyncTask_Insert_Danmu dan = new AsyncTask_Insert_Danmu(MainActivity.this, true);
                dan.execute(sharedpreference.getID(), danmucontent, "two");
            }
        }

        /**
         * 识别回调错误.
         */
        public void onError(SpeechError error) {
            showTip(error.getPlainDescription(true));
        }
    };

    private void showTip(final String str) {
        mToast.setText(str);
        mToast.show();
    }

    /**
     * 参数设置
     *
     * @param param
     * @return
     */
    public void setParam() {
        // 清空参数
        mIat.setParameter(SpeechConstant.PARAMS, null);

        // 设置听写引擎
        mIat.setParameter(SpeechConstant.ENGINE_TYPE, mEngineType);
        // 设置返回结果格式
        mIat.setParameter(SpeechConstant.RESULT_TYPE, "json");

        String lag = "mandarin";
        if (lag.equals("en_us")) {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "en_us");
        } else {
            // 设置语言
            mIat.setParameter(SpeechConstant.LANGUAGE, "zh_cn");
            // 设置语言区域
            mIat.setParameter(SpeechConstant.ACCENT, lag);
        }

        // 设置语音前端点:静音超时时间，即用户多长时间不说话则当做超时处理
        mIat.setParameter(SpeechConstant.VAD_BOS, "4000");

        // 设置语音后端点:后端点静音检测时间，即用户停止说话多长时间内即认为不再输入， 自动停止录音
        mIat.setParameter(SpeechConstant.VAD_EOS, "1000");

        // 设置标点符号,设置为"0"返回结果无标点,设置为"1"返回结果有标点
        mIat.setParameter(SpeechConstant.ASR_PTT, "1");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        // 注：AUDIO_FORMAT参数语记需要更新版本才能生效
        mIat.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
        mIat.setParameter(SpeechConstant.ASR_AUDIO_PATH, Environment.getExternalStorageDirectory() + "/msc/iat.wav");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mDanmakuView.hide();
        mDanmakuView.setVisibility(View.GONE);
    }

    @Override
    public void onResume() {
        super.onResume();
        mDanmakuView.show();
        mDanmakuView.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (webView.canGoBack()&&!url.equals("file:///android_asset/wechat/index.html")) {
                webView.goBack();
                return true;
            } else if(LocalVpnService.IsRunning){
                moveTaskToBack(false);
                return true;
            }else{
                finish();
            }
        }
        return super.onKeyDown(keyCode, event);
    }
}
