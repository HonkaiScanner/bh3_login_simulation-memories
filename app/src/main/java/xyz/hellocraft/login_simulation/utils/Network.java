package xyz.hellocraft.login_simulation.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class Network {

    private static Response realSendPost(String url, String param, Map<String, String> map) {

        Response response = new Response();

        PrintWriter out = null;
        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
            if (map != null) {
                ArrayList<String> arrayList = new ArrayList<>(map.keySet());
                for (String key : arrayList) {
                    conn.setRequestProperty(key, map.get(key));
                }
            }
            conn.setDoOutput(true);
            conn.setDoInput(true);
            out = new PrintWriter(conn.getOutputStream());
            out.print(param);
            out.flush();
            response.setCode(conn.getResponseCode());
            response.setHeaders(conn.getHeaderFields());
            String encoding = conn.getContentEncoding();
            InputStream ism = conn.getInputStream();
            if (encoding != null && encoding.contains("gzip")) {
                ism = new GZIPInputStream(ism);
            }
            in = new BufferedReader(
                    new InputStreamReader(ism));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            Logger.i("Network", "sendPost: Failed. Target: " + url + " \n" + e.getMessage());
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        response.setBody(result.toString());
        return response;
    }

    private static String realSendGet(String url, Map<String, String> map) {

        BufferedReader in = null;
        StringBuilder result = new StringBuilder();
        try {
            URL realUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) realUrl.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setConnectTimeout(3000);
            conn.setReadTimeout(3000);
//            conn.setRequestProperty("user-agent", "okhttp/3.10.0");
            if (map != null) {
                ArrayList<String> arrayList = new ArrayList<>(map.keySet());
                for (String key : arrayList) {
                    conn.setRequestProperty(key, map.get(key));
                }
            }
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
        } catch (Exception e) {
            Logger.i("Network", "sendGet: Failed. Target: " + url + " \n" + e.getMessage());
            e.printStackTrace();
            return "null";
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result.toString();
    }

    public static String sendGet(String url, Map<String, String> map, Boolean autoRetry) {
        String ret;
        while (true) {
            ret = realSendGet(url, map);
            if (ret != null && !ret.equals("null")) {
                break;
            }
            if (!autoRetry) {
                return "";
            }
            Logger.getLogger(null).makeToast("网络请求错误\n2s后自动重试");
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static String sendGet(String url) {
        return sendGet(url, null, true);
    }

    public static String sendGet(String url, Boolean autoRetry) {
        return sendGet(url, null, autoRetry);
    }

    public static Map<String, String> getBaseHeader() {
        Map<String, String> defaultMap = new HashMap<>();
        defaultMap.put("Content-Type", "application/json");
        return defaultMap;
    }

    public static String sendPost(String url, String param) {
        return sendPost(url, param, getBaseHeader(), 5, true).body;
    }

    public static Response sendPostRaw(String url, String param) {
        return sendPost(url, param, getBaseHeader(), 5, true);
    }

    public static String sendPost(String url, String param, int maxRetry) {
        return sendPost(url, param, null, maxRetry, true).body;
    }

    public static Response sendPostRaw(String url, String param, int maxRetry) {
        return sendPost(url, param, null, maxRetry, true);
    }

    public static String sendPost(String url, String param, Map<String, String> map) {
        return sendPost(url, param, map, 5, true).body;
    }

    public static Response sendPostRaw(String url, String param, Map<String, String> map) {
        return sendPost(url, param, map, 5, true);
    }

    public static Response sendPost(String url, String param, Map<String, String> map, int maxRetry, Boolean sendToast) {
        Response ret = new Response();
        while (maxRetry-- >= -1) {
            ret = realSendPost(url, param, map);
            if (ret != null && !ret.body.equals("null")) {
                break;
            }
            if (sendToast) {
                Logger.getLogger(null).makeToast("网络请求错误\n2s后自动重试");
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return ret;
    }

    public static class Response {
        public int code;
        public String body;
        public Map<String, List<String>> headers;

        public Response() {
            this.code = 404;
            this.body = "";
            this.headers = new HashMap<>();
        }

        public int getCode() {
            return code;
        }

        public void setCode(int code) {
            this.code = code;
        }

        public String getBody() {
            return body;
        }

        public void setBody(String body) {
            this.body = body;
        }

        public Map<String, List<String>> getHeaders() {
            return headers;
        }

        public void setHeaders(Map<String, List<String>> headers) {
            this.headers = headers;
        }
    }
}
