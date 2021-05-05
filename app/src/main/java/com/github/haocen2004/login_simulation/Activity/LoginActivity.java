package com.github.haocen2004.login_simulation.Activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.ActivityLoginBinding;
import com.github.haocen2004.login_simulation.util.Logger;
import com.github.haocen2004.login_simulation.util.Network;
import com.github.haocen2004.login_simulation.util.Tools;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.AVUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.util.Constant.HAS_ACCOUNT;

public class LoginActivity extends AppCompatActivity {
    private final String TAG = "SponsorManager";
    private final String emailPattern = "^[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
    private final String scKeyPattern = "^scanner_key_+[a-zA-Z0-9_-]{16}";
    private ActivityLoginBinding binding;
    private Logger Log;
    private final TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        @Override
        public void afterTextChanged(Editable editable) {
            boolean t1, t2, t3, t4;
            t1 = binding.editTextName.getText().toString().isEmpty();
            t2 = binding.editTextPassword.getText().toString().isEmpty();
            t3 = binding.editTextRegEmail.getText().toString().isEmpty();
            t4 = binding.editTextKey.getText().toString().isEmpty();
            binding.buttonRegisterStep2.setEnabled(!t1 && !t2 && !t3 && !t4);
            binding.buttonLogin.setEnabled(!t2 && !t3);
        }
    };
    @SuppressLint("HandlerLeak")
    Handler accessProgressBar = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.getData().getBoolean("type")) {
                showProgressBar();
            } else {
                hideProgressBar();
            }
        }
    };
    private int sp_level;
    private AVUser user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log = Logger.getLogger(this);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        binding.editTextName.addTextChangedListener(textWatcher);
        binding.editTextKey.addTextChangedListener(textWatcher);
        binding.editTextRegEmail.addTextChangedListener(textWatcher);
        binding.editTextPassword.addTextChangedListener(textWatcher);
        binding.buttonRegStep1.setOnClickListener(view1 -> {
            binding.buttonLogin.setVisibility(View.GONE);
            binding.buttonRegStep1.setVisibility(View.GONE);
            binding.buttonRegisterStep2.setVisibility(View.VISIBLE);
            binding.editTextName.setVisibility(View.VISIBLE);
            binding.editTextKey.setVisibility(View.VISIBLE);
            binding.buttonGetCode.setVisibility(View.VISIBLE);
        });
        binding.buttonGetCode.setOnClickListener(view -> {
            try {
                Uri uri = Uri.parse("http://afdian.net/@Haocen20004");
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(uri);
                startActivity(intent);
            } catch (Exception ignore) {
            }
        });

        binding.buttonRegisterStep2.setOnClickListener(view1 -> {
            showProgressBar();
            String sc_key = binding.editTextKey.getText().toString();
            if (Pattern.matches(scKeyPattern, sc_key)) {  // 正则匹配格式
                String content = binding.editTextRegEmail.getText().toString();
                if (Pattern.matches(emailPattern, content)) {
                    String postParam = "{\"app_ver\":" + VERSION_CODE + ",\"sponsor_key\":\"" + sc_key + "\"}";
                    AVQuery<AVObject> query = new AVQuery<>("Sponsors"); // 请求云端查重 1  LeanCloud
                    query.whereEqualTo("scannerKey", sc_key);
                    query.findInBackground().subscribe(new Observer<List<AVObject>>() {
                        public void onSubscribe(Disposable disposable) {
                        }

                        public void onError(Throwable throwable) {
                        }

                        public void onComplete() {
                        }

                        public void onNext(List<AVObject> sp) {
                            if (sp.size() == 0) {
                                Executors.newSingleThreadExecutor().execute(() -> {
                                    startCodeCheck(content, postParam, sc_key);
                                });
//                                new Thread(() -> {
                            } else {
                                Looper.prepare();
                                makeToast(getString(R.string.error_key_used));
                                hideProgressBar();
                            }
                        }


                    });

                } else {
                    makeToast(getString(R.string.error_email));
                    hideProgressBar();
                }
            } else {
                makeToast(getString(R.string.error_key));
                hideProgressBar();
            }


        });
        binding.buttonLogin.setOnClickListener(view1 -> {
            showProgressBar();
            String content = binding.editTextRegEmail.getText().toString();
            if (Pattern.matches(emailPattern, content)) {
                AVUser.loginByEmail(content, binding.editTextPassword.getText().toString()).subscribe(new Observer<AVUser>() {
                    public void onSubscribe(Disposable disposable) {
                    }

                    public void onComplete() {
                    }

                    public void onNext(AVUser user) {
                        setUser(user);
                        makeToast(getString(R.string.login_succeed));
                        hideProgressBar();
                        finish();
                    }

                    public void onError(Throwable throwable) {
                        makeToast(throwable.getMessage());
                        hideProgressBar();
                    }


                });
            } else {
                makeToast(getString(R.string.error_email));
                hideProgressBar();
            }
        });

    }

    private void startCodeCheck(String content, String postParam, String sc_key) {
        // 请求云端查询身份码是否有绑定用户  Tencent Cloud
        boolean needLoop = true;
        String feedback = null;
        while (needLoop) {
            feedback = Network.sendPost("https://service-beurmroh-1256541670.sh.apigw.tencentcs.com/release/sponsor", postParam);
            if (feedback != null) {
                needLoop = false;
            }
        }
        Logger.d(TAG, feedback);
        try {
            JSONObject feedback_json = new JSONObject(feedback);
            int retCode = feedback_json.getInt("ret");
            if (retCode > 0) {
                sp_level = retCode;

                AVUser user = new AVUser(); //账号创建

                user.setUsername(binding.editTextName.getText().toString());
                user.setPassword(binding.editTextPassword.getText().toString());

                user.setEmail(content);

                user.put("sp_level", sp_level);
                user.put("desc", "该用户还没有设置签名哦");
                user.put("custom_username", "崩坏3扫码器用户");
                user.put("scanner_key", sc_key);
                user.put("deviceId", Tools.getDeviceID(getApplicationContext()));


                user.signUpInBackground().subscribe(new Observer<AVUser>() {
                    public void onSubscribe(Disposable disposable) {
                    }

                    public void onNext(AVUser user) {
                        onRegistered(sc_key);
                    }

                    public void onError(@NotNull Throwable throwable) {
                        makeToast(throwable.getMessage());
                        hideProgressBar();
                    }

                    public void onComplete() {
                    }
                });


            } else if (retCode == -2) {
                Looper.prepare();
                makeToast(getString(R.string.error_ver_outdate));
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putBoolean("type", false);
                msg.setData(data);
                accessProgressBar.sendMessage(msg);
                hideProgressBar();
            } else {
                Looper.prepare();
                makeToast(getString(R.string.error_key));
                Message msg = new Message();
                Bundle data = new Bundle();
                data.putBoolean("type", false);
                msg.setData(data);
                accessProgressBar.sendMessage(msg);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    private void onRegistered(String sc_key) {
        // 注册成功
        makeToast(getString(R.string.reg_succ));
        setUser(user);
        // 将身份码与用户绑定
        AVObject sponsor = new AVObject("Sponsors");

        sponsor.put("scannerKey", sc_key);
        sponsor.put("user", user);
        sponsor.put("name", binding.editTextName.getText().toString());
        sponsor.put("deviceId", Tools.getDeviceID(getApplicationContext()));
//        sponsor.put("desc", "该用户还没有设置签名哦");
        sponsor.put("personalPageUrl", " ");
        sponsor.put("sp_level", sp_level);

        sponsor.saveInBackground().subscribe(new Observer<AVObject>() {
            public void onSubscribe(Disposable disposable) {
            }

            public void onNext(AVObject todo) {

                Logger.i(TAG, "注册成功");
                hideProgressBar();
            }

            public void onError(Throwable throwable) {
                CrashReport.postCatchedException(throwable);
            }

            public void onComplete() {
            }
        });
        finish();
    }

    public void setUser(AVUser user) {
        try {
            this.user = user;
            AVUser.changeCurrentUser(user, true);
            getDefaultSharedPreferences(this).edit()
                    .putBoolean("has_account", true)
                    .putString("account_token", user.getSessionToken())
                    .putString("custom_username", user.getString("custom_username"))
                    .apply();
            HAS_ACCOUNT = true;
        } catch (Exception ignore) {
        }
    }

    private void makeToast(String msg) {
        Log.makeToast(this, msg);
    }

    private void showProgressBar() {
        binding.progessBarUser.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        binding.progessBarUser.setVisibility(View.GONE);
    }

}