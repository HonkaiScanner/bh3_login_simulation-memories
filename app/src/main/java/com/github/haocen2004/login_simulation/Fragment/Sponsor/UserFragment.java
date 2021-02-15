package com.github.haocen2004.login_simulation.Fragment.Sponsor;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.github.haocen2004.login_simulation.R;
import com.github.haocen2004.login_simulation.databinding.FragmentSpUserBinding;
import com.github.haocen2004.login_simulation.util.Network;
import com.tencent.bugly.crashreport.BuglyLog;
import com.tencent.bugly.crashreport.CrashReport;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.regex.Pattern;

import cn.leancloud.AVObject;
import cn.leancloud.AVQuery;
import cn.leancloud.AVUser;
import io.reactivex.Observer;
import io.reactivex.disposables.Disposable;

import static com.github.haocen2004.login_simulation.BuildConfig.VERSION_CODE;

public class UserFragment extends Fragment {
    private FragmentSpUserBinding binding;
    private int sp_level;
    private AVUser user;
    private final String TAG = "SponsorManager";
    private final String emailPattern = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
    private final String scKeyPattern = "^scanner_key_+[a-zA-Z0-9_-]{16}*";
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

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSpUserBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
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
        });

        binding.checkBox.setOnCheckedChangeListener((compoundButton, b) -> {

        });
        binding.buttonRegisterStep2.setOnClickListener(view1 -> {
            String sc_key = binding.editTextKey.getText().toString();
            if (Pattern.matches(scKeyPattern, sc_key)) {
                String content = binding.editTextRegEmail.getText().toString();
                if (Pattern.matches(emailPattern, content)) {
                    String postParam = "{\"app_ver\":" + VERSION_CODE + ",\"sponsor_key\":\"" + sc_key + "\"}";
                    AVQuery<AVObject> query = new AVQuery<>("Sponsors");
                    query.whereEqualTo("scannerKey", sc_key);
                    query.findInBackground().subscribe(new Observer<List<AVObject>>() {
                        public void onSubscribe(Disposable disposable) {
                        }

                        public void onNext(List<AVObject> sp) {
                            makeToast(getString(R.string.error_key_used));
                        }

                        public void onError(Throwable throwable) {
                            new Thread(() -> {
                                String feedback = Network.sendPost("https://service-beurmroh-1256541670.sh.apigw.tencentcs.com/release/sponsor", postParam);
                                if (feedback == null) {
                                    makeToast(getString(R.string.error_network));
                                    return;
                                }
                                try {
                                    JSONObject feedback_json = new JSONObject(feedback);
                                    int retCode = feedback_json.getInt("ret");
                                    if (retCode > 0) {
                                        sp_level = retCode;

                                        AVUser user = new AVUser();

                                        user.setUsername(binding.editTextName.getText().toString());
                                        user.setPassword(binding.editTextPassword.getText().toString());

                                        user.setEmail(content);

                                        user.put("sp_level", sp_level);

                                        user.signUpInBackground().subscribe(new Observer<AVUser>() {
                                            public void onSubscribe(Disposable disposable) {
                                            }

                                            public void onNext(AVUser user) {
                                                // 注册成功
                                                makeToast(getString(R.string.reg_succ));
                                                setUser(user);
                                                AVObject sponsor = new AVObject("Sponsors");

                                                sponsor.put("scannerKey", sc_key);
                                                sponsor.put("user", user);

                                                sponsor.saveInBackground().subscribe(new Observer<AVObject>() {
                                                    public void onSubscribe(Disposable disposable) {
                                                    }

                                                    public void onNext(AVObject todo) {
                                                        // 成功保存之后，执行其他逻辑
                                                        BuglyLog.i(TAG, "注册成功");
                                                    }

                                                    public void onError(Throwable throwable) {
                                                        CrashReport.postCatchedException(throwable);
                                                    }

                                                    public void onComplete() {
                                                    }
                                                });
                                            }

                                            public void onError(@NotNull Throwable throwable) {
                                                makeToast(throwable.getMessage());
                                            }

                                            public void onComplete() {
                                            }
                                        });


                                    } else if (retCode == -2) {
                                        makeToast(getString(R.string.error_ver_outdate));
                                    } else {
                                        makeToast(getString(R.string.error_key));
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            }).start();
                        }

                        public void onComplete() {
                        }
                    });

                } else {
                    makeToast(getString(R.string.error_email));
                }
            } else {
                makeToast(getString(R.string.error_key));
            }


        });
        binding.buttonLogin.setOnClickListener(view1 -> {
            String content = binding.editTextRegEmail.getText().toString();
            if (Pattern.matches(emailPattern, content)) {
                AVUser.loginByEmail(content, binding.editTextPassword.getText().toString()).subscribe(new Observer<AVUser>() {
                    public void onSubscribe(Disposable disposable) {
                    }

                    public void onNext(AVUser user) {
                        setUser(user);
                        makeToast(getString(R.string.login_succeed));
                    }

                    public void onError(Throwable throwable) {
                        makeToast(throwable.getMessage());
                    }

                    public void onComplete() {
                    }
                });
            } else {
                makeToast(getString(R.string.error_email));
            }
        });

    }

    public AVUser getUser() {
        return user;
    }

    public void setUser(AVUser user) {
        this.user = user;
    }

    private void makeToast(String msg) {
        Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
    }
}