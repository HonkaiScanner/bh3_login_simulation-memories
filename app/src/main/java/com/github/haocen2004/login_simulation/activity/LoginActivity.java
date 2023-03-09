package com.github.haocen2004.login_simulation.activity;

import static android.preference.PreferenceManager.getDefaultSharedPreferences;
import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;
import static com.github.haocen2004.login_simulation.data.Constant.AFD_URL;
import static com.github.haocen2004.login_simulation.data.Constant.HAS_ACCOUNT;

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

import androidx.annotation.Keep;
import androidx.annotation.NonNull;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.ActivityLoginBinding;
import com.github.haocen2004.login_simulation.utils.Logger;
import com.github.haocen2004.login_simulation.utils.Network;
import com.github.haocen2004.login_simulation.utils.Tools;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import cn.leancloud.LCObject;
import cn.leancloud.LCQuery;
import cn.leancloud.LCUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

public class LoginActivity extends BaseActivity {
    private final String TAG = "SponsorManager";
    private final String emailPattern = "^[\\w!#$%&'*+/=?^_`{|}~-]+(?:\\.[\\w!#$%&'*+/=?^_`{|}~-]+)*@(?:[\\w](?:[\\w-]*[\\w])?\\.)+[\\w](?:[\\w-]*[\\w])?";
    private final String scKeyPattern = "^scanner_key_+[a-zA-Z0-9_-]{16}";
    private ActivityLoginBinding binding;
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
    Handler accessProgressBar = new Handler(Objects.requireNonNull(Looper.myLooper())) {
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
    private Logger Log;
    private int sp_level;
    private LCUser user;

    @Keep
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
                Uri uri = Uri.parse(AFD_URL);
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
                    String postParam = "app_ver=" + VERSION_CODE + "&sponsor_key=" + sc_key;
//                    String postParam = "{\"app_ver\":" + VERSION_CODE + ",\"sponsor_key\":\"" + sc_key + "\"}";
                    LCQuery<LCObject> query = new LCQuery<>("Sponsors"); // 请求云端查重 1  LeanCloud
                    query.whereEqualTo("scannerKey", sc_key);
                    query.findInBackground().subscribe(new Observer<List<LCObject>>() {
                        public void onSubscribe(@NonNull Disposable disposable) {
                        }

                        public void onError(@NonNull Throwable throwable) {
                            CrashReport.postCatchedException(throwable);
                            makeToast(throwable.getMessage());
                            hideProgressBar();
                        }

                        public void onComplete() {
                        }

                        public void onNext(@NonNull List<LCObject> sp) {
                            if (sp.size() == 0) {
                                Executors.newSingleThreadExecutor().execute(() -> startCodeCheck(content, postParam, sc_key));
//                                new Thread(() -> {
                            } else {
                                try {
                                    Looper.prepare();
                                } catch (Exception ignore) {
                                }
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
                LCUser.loginByEmail(content, binding.editTextPassword.getText().toString()).subscribe(new Observer<LCUser>() {
                    public void onSubscribe(@NonNull Disposable disposable) {
                    }

                    public void onComplete() {
                    }

                    public void onNext(@NonNull LCUser user) {
                        setUser(user);
                        makeToast(getString(R.string.login_succeed));
                        hideProgressBar();
                        finish();
                    }

                    public void onError(@NonNull Throwable throwable) {
                        for (StackTraceElement stackTraceElement : throwable.getStackTrace()) {
                            Logger.d("crash", stackTraceElement.toString());
                        }
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
        String feedback = Network.sendGet("https://api.scanner.hellocraft.xyz/sp_check?" + postParam, true);
        Logger.d(TAG, feedback);
        try {
            JSONObject feedback_json = new JSONObject(feedback);
            int retCode = feedback_json.getInt("ret");
            if (retCode > 0) {
                sp_level = retCode;

                LCUser user = new LCUser(); //账号创建

                user.setUsername(binding.editTextName.getText().toString());
                user.setPassword(binding.editTextPassword.getText().toString());

                user.setEmail(content);

                user.put("sp_level", sp_level);
                user.put("scanner_key", sc_key);
                user.put("deviceId", Tools.getDeviceID(getApplicationContext()));


                user.signUpInBackground().subscribe(new Observer<LCUser>() {
                    public void onSubscribe(@NotNull Disposable disposable) {
                    }

                    public void onNext(@NotNull LCUser user) {
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
                makeToast(getString(R.string.error_ver_outdated));
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
        LCObject sponsor = new LCObject("Sponsors");

        sponsor.put("scannerKey", sc_key);
        sponsor.put("user", user);
        sponsor.put("name", binding.editTextName.getText().toString());
        sponsor.put("deviceId", Tools.getDeviceID(getApplicationContext()));
//        sponsor.put("desc", "该用户还没有设置签名哦");
        sponsor.put("personalPageUrl", " ");
        sponsor.put("sp_level", sp_level);

        sponsor.saveInBackground().subscribe(new Observer<LCObject>() {
            public void onSubscribe(@NonNull Disposable disposable) {
            }

            public void onNext(@NonNull LCObject todo) {

                Logger.i(TAG, "注册成功");
                hideProgressBar();
            }

            public void onError(@NonNull Throwable throwable) {
                CrashReport.postCatchedException(throwable);
            }

            public void onComplete() {
            }
        });
        finish();
    }

    public void setUser(LCUser user) {
        try {
            this.user = user;
            LCUser.changeCurrentUser(user, true);
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
