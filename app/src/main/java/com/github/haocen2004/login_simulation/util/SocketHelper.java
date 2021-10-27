package com.github.haocen2004.login_simulation.util;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import androidx.annotation.NonNull;

import org.json.JSONObject;

import java.net.DatagramPacket;
import java.net.DatagramSocket;

public class SocketHelper {
    QRScanner qrScanner;
    private final String TAG = "SocketHelper";
    private final Handler loopHandle = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
        }
    };

    public SocketHelper() {
    }

    public void setQrScanner(QRScanner qrScanner) {
        Logger.d(TAG, "扫码模块数据载入成功");
        this.qrScanner = qrScanner;
    }

    public void start() {
        loopHandle.post(() -> {
            Logger.d(TAG, "启动广播监听线程...");
            Listen();
        });
    }

    private void Listen() {
        int port = 12585; // 扫码器监听端口
        DatagramSocket ds;
        DatagramPacket dp;
        byte[] buf = new byte[1024 * 4];//存储发来的消息
        StringBuilder stringBuilder = new StringBuilder();
        try {
            //绑定端口的
            ds = new DatagramSocket(port);
            dp = new DatagramPacket(buf, buf.length);
            Logger.d(TAG, "开始监听广播");
            ds.receive(dp);
            ds.close();
            int i;
            for (i = 0; i < 1024; i++) {
                if (buf[i] == 0) {
                    break;
                }
                stringBuilder.append((char) buf[i]);
            }
            Logger.d(TAG, "接收到消息：" + stringBuilder.toString());
            JSONObject receivedJson = new JSONObject(stringBuilder.toString());
            if (receivedJson.has("scanner_data")) {
                //{"scanner_data":{"url":"xxx","t":time}}
                String url = receivedJson.getJSONObject("scanner_data").getString("url");
                Logger.d(TAG, "接收到扫码器助手广播：" + receivedJson.getJSONObject("scanner_data").toString());
                if (qrScanner.parseUrl(url)) {
                    qrScanner.start();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
//        本段代码参考：
//        作者：愚公要移山
//        链接：https://juejin.cn/post/6844903918535720973
//        来源：稀土掘金
//        著作权归作者所有。商业转载请联系作者获得授权，非商业转载请注明出处。

    }
}
