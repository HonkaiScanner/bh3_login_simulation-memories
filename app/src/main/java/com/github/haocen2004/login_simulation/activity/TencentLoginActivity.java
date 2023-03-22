package com.github.haocen2004.login_simulation.activity;

import static com.github.haocen2004.login_simulation.data.Constant.INTENT_EXTRA_KEY_TENCENT_LOGIN;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.LinearLayout;

import com.github.haocen2004.login_simulation.data.dialog.ButtonData;
import com.github.haocen2004.login_simulation.data.dialog.DialogData;
import com.github.haocen2004.login_simulation.data.dialog.DialogLiveData;
import com.github.haocen2004.login_simulation.databinding.ActivityTencentLoginBinding;
import com.github.haocen2004.login_simulation.utils.Logger;
import com.github.haocen2004.login_simulation.utils.Tools;

import java.net.MalformedURLException;
import java.net.URL;

public class TencentLoginActivity extends BaseActivity {

    private ActivityTencentLoginBinding binding;

    private WebView webView;

    private String uin;

    private final String TAG = "Tencent Web login";

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityTencentLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        webView = new WebView(getApplicationContext());

        webView.setLayoutParams(params);

        binding.getRoot().addView(webView);

        WebSettings webSettings = webView.getSettings();

        webSettings.setJavaScriptEnabled(true);

        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);

        webView.setWebViewClient(new WebViewClient() {


            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
                Logger.d(TAG, "loading 1 " + url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                super.onLoadResource(view, url);

                Logger.d(TAG, "loading 2 " + url);
                if (url.contains("uin=")) {
                    try {
                        URL url1 = new URL(url);
                        for (String s : url1.getQuery().split("&")) {
                            if (s.contains("uin")) {
                                uin = s.split("=")[1];
                                Logger.d(TAG, "find qq: " + uin);
                            }
                        }
                    } catch (MalformedURLException e) {
                        throw new RuntimeException(e);
                    }
                }
                if (url.contains("auth://")) {
                    if (!url.contains("access_token")) {
                        if (url.contains("progress/0")) {
                            DialogData dialogData = new DialogData("登录失败", "code: progress/0\n可能是账号被腾讯风控阻止登陆");
                            dialogData.setPositiveButtonData(new ButtonData("我已知晓"));
                            DialogLiveData.getINSTANCE(getApplicationContext()).addNewDialog(dialogData);
                            setResult(RESULT_CANCELED);
                            finish();
                        }
                        return; // auth://progress/0
//                        Logger.d(TAG,"");
                    }
                    url = url.replace("auth://", "https://");
                    Logger.d(TAG, "login success");
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_EXTRA_KEY_TENCENT_LOGIN, url);
                    intent.putExtra("tencent.login.uin", uin);
                    setResult(RESULT_OK, intent);
                    finish();
                }
//                Logger.d(TAG,url);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Logger.d(TAG, "loaded " + url);
                if (url.contains("imgcache.qq.com")) {
                    Logger.d(TAG, "login success");
                    Intent intent = new Intent();
                    intent.putExtra(INTENT_EXTRA_KEY_TENCENT_LOGIN, url);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

        webView.loadUrl("https://xui.ptlogin2.qq.com/cgi-bin/xlogin?appid=716027609&pt_3rd_aid=1105553399&daid=381&pt_skey_valid=0&style=35&s_url=http%3A%2F%2Fconnect.qq.com&refer_cgi=m_authorize&ucheck=1&fall_to_wv=1&status_os=13.3.1&redirect_uri=auth%3A%2F%2Fwww.qq.com&client_id=1105553399&response_type=token&scope=get_user_info%2Cget_simple_userinfo%2Cadd_t&sdkp=i&sdkv=3.3.8_lite&state=test&status_machine=iPhone9%2C1&switch=1&traceid=" + Tools.genRandomString(48) + "_" + System.currentTimeMillis());

    }
}