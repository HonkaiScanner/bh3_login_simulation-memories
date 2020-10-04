package com.github.haocen2004.login_simulation.Proxy;

import android.util.Log;

import androidx.annotation.NonNull;

import com.github.megatronking.netbare.http.HttpBody;
import com.github.megatronking.netbare.http.HttpRequest;
import com.github.megatronking.netbare.http.HttpRequestHeaderPart;
import com.github.megatronking.netbare.http.HttpResponse;
import com.github.megatronking.netbare.http.HttpResponseHeaderPart;
import com.github.megatronking.netbare.injector.HttpInjector;
import com.github.megatronking.netbare.injector.InjectorCallback;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class HttpInject implements HttpInjector {
    private String TAG = "Injector";

    @Override
    public boolean sniffRequest(@NonNull HttpRequest request) {
        Log.i(TAG, "sniffRequest: " + request.host());
        return true;
    }

    @Override
    public boolean sniffResponse(@NonNull HttpResponse response) {
        return false;
    }

    @Override
    public void onRequestInject(@NonNull HttpRequestHeaderPart header, @NonNull InjectorCallback callback) throws IOException {
//        Log.i(TAG, "onRequestInject: "+ ;);
        for (Map.Entry<String, List<String>> entry : header.headers().entrySet()) {

            Log.i(TAG, "Key = " + entry.getKey() + ", Value = " + entry.getValue());

        }
        callback.onFinished(header);
    }

    @Override
    public void onResponseInject(@NonNull HttpResponseHeaderPart header, @NonNull InjectorCallback callback) throws IOException {
        callback.onFinished(header);
    }

    @Override
    public void onRequestInject(@NonNull HttpRequest request, @NonNull HttpBody body, @NonNull InjectorCallback callback) throws IOException {
//        Log.i(TAG, "onRequestInject: "+ body.toBuffer().);
        StringBuilder stringBuffer = new StringBuilder();
        for (Byte b : body.toBuffer().array()) {
            stringBuffer.append(b);
        }
        Log.i(TAG, "onRequestInject: " + stringBuffer.toString());
        callback.onFinished(body);

    }

    @Override
    public void onResponseInject(@NonNull HttpResponse response, @NonNull HttpBody body, @NonNull InjectorCallback callback) throws IOException {
        callback.onFinished(body);
    }

    @Override
    public void onRequestFinished(@NonNull HttpRequest request) {

    }

    @Override
    public void onResponseFinished(@NonNull HttpResponse response) {

    }
}
