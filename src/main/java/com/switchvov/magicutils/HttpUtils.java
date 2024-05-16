package com.switchvov.magicutils;

import com.fasterxml.jackson.core.type.TypeReference;
import okhttp3.ConnectionPool;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
import java.util.function.Function;


/**
 * Http utils.
 *
 * @author switch
 * @since 2024/5/4
 */
public interface HttpUtils {

    Logger log = LoggerFactory.getLogger(HttpUtils.class);

    HttpInvoker Default = new OkHttpInvoker();

    static HttpInvoker getDefault() {
        if (((OkHttpInvoker) Default).isInitialized()) {
            return Default;
        }
        int timeout = Integer.parseInt(System.getProperty("magicutils.http.timeout", "1000"));
        int maxIdleConnections = Integer.parseInt(System.getProperty("magicutils.http.maxconn", "128"));
        int keepAliveDuration = Integer.parseInt(System.getProperty("magicutils.http.keepalive", "300"));
        ((OkHttpInvoker) Default).init(timeout, maxIdleConnections, keepAliveDuration);
        return Default;
    }

    static String get(String url) {
        return getDefault().get(url);
    }

    static String post(String requestString, String url) {
        return getDefault().post(requestString, url);
    }

    static <T> T httpGet(String url, Class<T> clazz) {
        return httpGet(HttpUtils::get, url, clazz);
    }

    static <T> T httpGet(Function<String, String> f, String url, Class<T> clazz) {
        log.debug(" =====> httpGet:{}", url);
        String respJson = f.apply(url);
        log.debug(" =====> response:{}", respJson);
        return JsonUtils.fromJson(respJson, clazz);
    }

    static <T> T httpGet(String url, TypeReference<T> typeReference) {
        return httpGet(HttpUtils::get, url, typeReference);
    }

    static <T> T httpGet(Function<String, String> f, String url, TypeReference<T> typeReference) {
        log.debug(" =====> httpGet:{}", url);
        String respJson = f.apply(url);
        log.debug(" =====> response:{}", respJson);
        return JsonUtils.fromJson(respJson, typeReference);
    }

    static <T> T httpPost(String requestString, String url, Class<T> clazz) {
        return httpPost(HttpUtils::post, requestString, url, clazz);
    }

    static <T> T httpPost(BiFunction<String, String, String> f, String requestString, String url, Class<T> clazz) {
        log.debug(" =====> httpPost:{}", url);
        String respJson = f.apply(requestString, url);
        log.debug(" =====> response:{}", respJson);
        return JsonUtils.fromJson(respJson, clazz);
    }

    static <T> T httpPost(String requestString, String url, TypeReference<T> typeReference) {
        return httpPost(HttpUtils::post, requestString, url, typeReference);
    }

    static <T> T httpPost(BiFunction<String, String, String> f, String requestString, String url, TypeReference<T> typeReference) {
        log.debug(" =====> httpPost:{}", url);
        String respJson = f.apply(requestString, url);
        log.debug(" =====> response:{}", respJson);
        return JsonUtils.fromJson(respJson, typeReference);
    }

    interface HttpInvoker {
        String post(String requestString, String url);

        String get(String url);
    }

    class OkHttpInvoker implements HttpInvoker {
        private static final Logger logger = LoggerFactory.getLogger(OkHttpInvoker.class);
        private static final MediaType JSON_TYPE = MediaType.get("application/json;charset=utf-8");

        private boolean initialized = false;
        private OkHttpClient client;

        public void init(int timeout, int maxIdleConnections, int keepAliveDuration) {
            client = new OkHttpClient.Builder()
                    .connectionPool(new ConnectionPool(maxIdleConnections, keepAliveDuration, TimeUnit.SECONDS))
                    .readTimeout(timeout, TimeUnit.MILLISECONDS)
                    .writeTimeout(timeout, TimeUnit.MILLISECONDS)
                    .connectTimeout(timeout, TimeUnit.MILLISECONDS)
                    .retryOnConnectionFailure(true)
                    .build();
            initialized = true;
        }

        public boolean isInitialized() {
            return initialized;
        }

        @Override
        public String post(String requestString, String url) {
            log.debug(" ===> post url = {}, requestString = {}", url, requestString);
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(requestString, JSON_TYPE))
                    .build();
            try {
                String respJson = client.newCall(request).execute().body().string();
                log.debug(" ===> respJson = {}", respJson);
                return respJson;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public String get(String url) {
            log.debug(" ===> get url = {}", url);
            Request request = new Request.Builder()
                    .url(url)
                    .get()
                    .build();
            try {
                String respJson = client.newCall(request).execute().body().string();
                log.debug(" ===> respJson = {}", respJson);
                return respJson;
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
