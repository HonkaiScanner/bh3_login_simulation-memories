package com.github.haocen2004.login_simulation.login;

import android.content.Intent;

import com.github.haocen2004.login_simulation.data.RoleData;

import java.io.Serializable;

public interface LoginImpl extends Serializable {
    void login();

    void logout();

    RoleData getRole();

    boolean isLogin();

    String getUsername();

    void onActivityResult(int requestCode, int resultCode, Intent data);

}
