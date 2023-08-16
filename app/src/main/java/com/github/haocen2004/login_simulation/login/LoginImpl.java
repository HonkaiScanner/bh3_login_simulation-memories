package com.github.haocen2004.login_simulation.login;

import com.github.haocen2004.login_simulation.data.RoleData;

public interface LoginImpl {
    void login();

    boolean logout();

    RoleData getRole();

    void setRole(RoleData roleData);

    boolean isLogin();

    String getUsername();
}
