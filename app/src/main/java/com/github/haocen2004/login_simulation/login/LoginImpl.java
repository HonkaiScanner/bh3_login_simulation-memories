package com.github.haocen2004.login_simulation.login;

import com.github.haocen2004.login_simulation.data.RoleData;

import java.io.Serializable;

public interface LoginImpl extends Serializable {
    void login();

    boolean logout();

    RoleData getRole();

    boolean isLogin();

    String getUsername();

    void setRole(RoleData roleData);
}
