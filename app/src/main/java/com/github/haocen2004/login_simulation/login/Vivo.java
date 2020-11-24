//package com.github.haocen2004.login_simulation.login;
//
//import android.app.Activity;
//
//import com.github.haocen2004.login_simulation.util.RoleData;
//import com.vivo.unionsdk.open.VivoAccountCallback;
//import com.vivo.unionsdk.open.VivoUnionSDK;
//
//public class Vivo implements LoginImpl {
//    private Activity activity;
//    private boolean isLogin;
//    private String uid;
//    private String token;
//    private RoleData roleData;
//    private String appId = "";
//
//    private VivoAccountCallback callback = new VivoAccountCallback() {
//        @Override
//        public void onVivoAccountLogin(String s, String s1, String s2) {
//            uid = s1;
//            token = s2;
////            roleData = new RoleData();
//        }
//
//        @Override
//        public void onVivoAccountLogout(int i) {
//            isLogin = false;
//        }
//
//        @Override
//        public void onVivoAccountLoginCancel() {
//            isLogin = false;
//        }
//    };
//
//    public Vivo(Activity activity){
//        this.activity = activity;
//
//        VivoUnionSDK.initSdk(activity,appId,true);
//        VivoUnionSDK.registerAccountCallback(activity,callback);
//    }
//    @Override
//    public void login() {
//        VivoUnionSDK.login(activity);
//    }
//
//    @Override
//    public void logout() {
//
//    }
//
//    @Override
//    public RoleData getRole() {
//        return roleData;
//    }
//
//    @Override
//    public boolean isLogin() {
//        return isLogin;
//    }
//}
