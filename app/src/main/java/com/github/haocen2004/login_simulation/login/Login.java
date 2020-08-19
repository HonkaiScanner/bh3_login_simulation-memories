package com.github.haocen2004.login_simulation.login;

public interface Login {
    void login();
    void logout();
    String getCombo_token();
    String getCombo_id();
    String getUid();
    boolean isLogin();
}
