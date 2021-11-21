package com.github.haocen2004.login_simulation.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class SocketHelper {
    QRScanner qrScanner;
    private final String TAG = "SocketHelper";
    private MulticastSocket ms;
    private final String multicastHost = "239.0.1.255";
    private InetAddress receiveAddress;
    private final Logger logger;
    private final Handler loopHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    public SocketHelper() {
        logger = Logger.getLogger(null);
    }

    public void setQrScanner(QRScanner qrScanner) {
        Logger.d(TAG, "扫码模块数据载入成功");
        this.qrScanner = qrScanner;
    }

    public void start() {
        loopHandle.post(() -> {
            Logger.d(TAG, "启动广播监听线程...");
            try {
                ms = new MulticastSocket(12585);
                receiveAddress = InetAddress.getByName(multicastHost);
                ms.joinGroup(receiveAddress);
                new Thread(socket_runnable).start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    Runnable socket_runnable = new Runnable() {
        @Override
        public void run() {
            Logger.d(TAG, "开始监听广播");
            byte[] buf = new byte[1024];
            DatagramPacket dp = new DatagramPacket(buf, 1024);
            while (true) {
                try {
                    ms.receive(dp);
                    String receiveMsg = new String(buf, 0, dp.getLength());
                    Logger.d(TAG, dp.getAddress() + ":" + dp.getPort() + ": 接收到消息: " + receiveMsg);
                    JSONObject receivedJson = new JSONObject(receiveMsg);
                    if (receivedJson.has("scanner_data")) {
                        //{"scanner_data":{"url":"xxx","t":time}}
                        String url = receivedJson.getJSONObject("scanner_data").getString("url");
                        Logger.d(TAG, "接收到扫码器助手广播：" + receivedJson.getJSONObject("scanner_data").toString());
                        if (qrScanner == null) {
                            Logger.e(TAG, "扫码模块未加载！ 暂不处理消息");
                            continue;
                        }
                        logger.makeToast("接收到扫码器助手广播");
                        if (qrScanner.parseUrl(url)) {
                            qrScanner.start();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }


        }
    };
}
