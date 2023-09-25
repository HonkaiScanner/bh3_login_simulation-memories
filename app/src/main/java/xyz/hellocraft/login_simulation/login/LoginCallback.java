package xyz.hellocraft.login_simulation.login;

import android.content.Intent;

import xyz.hellocraft.login_simulation.data.LaunchActivityCallback;
import xyz.hellocraft.login_simulation.data.RoleData;

public interface LoginCallback {
    void onLoginSucceed(RoleData roleData);

    void onLoginFailed();

    void launchActivityForResult(Intent intent, LaunchActivityCallback callback);
}
