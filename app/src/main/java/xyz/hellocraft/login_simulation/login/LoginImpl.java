package xyz.hellocraft.login_simulation.login;

import xyz.hellocraft.login_simulation.data.RoleData;

public interface LoginImpl {
    void login();

    boolean logout();

    RoleData getRole();

    void setRole(RoleData roleData);

    boolean isLogin();

    String getUsername();
}
