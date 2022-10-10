package com.github.haocen2004.login_simulation.login;

import androidx.fragment.app.Fragment;

public interface LoginCallback {
    void onLoginSucceed();

    void onLoginFailed();

    Fragment getCallbackFragment();
}
