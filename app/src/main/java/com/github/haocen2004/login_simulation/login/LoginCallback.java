package com.github.haocen2004.login_simulation.login;

import android.content.Intent;

import com.github.haocen2004.login_simulation.data.LaunchActivityCallback;
import com.github.haocen2004.login_simulation.data.RoleData;

public interface LoginCallback {
    void onLoginSucceed(RoleData roleData);

    void onLoginFailed();

    void launchActivityForResult(Intent intent, LaunchActivityCallback callback);
}
