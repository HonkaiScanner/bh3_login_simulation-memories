package com.github.haocen2004.login_simulation.Proxy;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.github.megatronking.netbare.BuildConfig;
import com.github.megatronking.netbare.NetBare;
import com.github.megatronking.netbare.NetBareConfig;
import com.github.megatronking.netbare.http.HttpInjectInterceptor;
import com.github.megatronking.netbare.http.HttpInterceptorFactory;
import com.github.megatronking.netbare.ssl.JKS;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Proxy {
    private NetBare netBare;
    private JKS jks;
    private Activity activity;

    private final String JKS_ALIAS = "login_simulation_cert";
    private final String JKS_PW = "login_simulation_cert";
    private final String TAG = "Login Simulation Proxy";


    public Proxy(Activity activity) {
        this.activity = activity;
        netBare = NetBare.get();
        netBare.attachApplication(activity.getApplication(), BuildConfig.DEBUG);
        jks = new JKS(activity, JKS_ALIAS, JKS_PW.toCharArray(), JKS_ALIAS, "github",
                "US", "github", "US");
        prepareNetBare();
    }

    public void prepareNetBare() {
        // 安装自签证书
        if (!JKS.isInstalled(activity, JKS_ALIAS)) {
            try {
                JKS.install(activity, JKS_ALIAS, JKS_ALIAS);
            } catch (IOException e) {
                // 安装失败
                Log.e(TAG, "Init: JKS Install Failed.");
            }
            return;
        }
        // 配置VPN
        Intent intent = NetBare.get().prepare();
        if (intent != null) {
            int REQUEST_CODE_PREPARE = 12580;
            Log.i(TAG, "Init: Prepare VPN.");
            activity.startActivityForResult(intent, REQUEST_CODE_PREPARE);
            return;
        }
        // 启动NetBare服务
//        NetBareConfig netBareConfig  = new NetBareConfig.Builder()
//                .dumpUid(false)
//                .setMtu(4096)
//                .setAddress(new IpAddress("10.1.10.1", 32))
//                .setSession("NetBare")
//                .addRoute(new IpAddress("0.0.0.0", 0))
//                .setVirtualGatewayFactory(new HttpVirtualGatewayFactory(jks,interceptorFactories()))
//                .build();
        netBare.start(NetBareConfig.defaultHttpConfig(jks, interceptorFactories()));
        Log.i(TAG, "Init: Succeed.");
    }

    private List<HttpInterceptorFactory> interceptorFactories() {
        List<HttpInterceptorFactory> list = new ArrayList<>();
        list.add(HttpInjectInterceptor.createFactory(new HttpInject()));
        return list;
    }


}
