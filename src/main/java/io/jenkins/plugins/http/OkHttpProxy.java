package io.jenkins.plugins.http;

import com.google.gson.Gson;
import io.jenkins.plugins.exception.PluginException;
import io.jenkins.plugins.util.Utils;
import okhttp3.*;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static io.jenkins.plugins.util.Utils.throwPluginException;

/**
 * OkHttp请求代理
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/6/30 20:17
 */
public final class OkHttpProxy implements IOkHttpProxy {
    static OkHttpProxy okHttpProxy;
    private OkHttpClient mHttpClient;
    public static final MediaType JSON_TYPE = MediaType.parse("application/json; charset=utf-8");
    public static final MediaType STREAM_TYPE = MediaType.parse("application/octet-stream");

    private OkHttpProxy() {
        this(new OkHttpClient.Builder()
                .connectTimeout(20_000, TimeUnit.MILLISECONDS)
                .writeTimeout(20_000, TimeUnit.MILLISECONDS)
                .readTimeout(20_000, TimeUnit.MILLISECONDS)
                .build());

    }

    public OkHttpProxy(OkHttpClient mHttpClient) {
        this.mHttpClient = mHttpClient;
    }

    private static OkHttpProxy init() {
        if (okHttpProxy == null) {
            synchronized (OkHttpProxy.class) {
                if (okHttpProxy == null) {
                    okHttpProxy = new OkHttpProxy();
                }
            }
        }
        return okHttpProxy;
    }

    private OkHttpClient getHttpClient() {
        return mHttpClient == null ? new OkHttpClient() : mHttpClient;
    }

    public static OkHttpProxy getInstance() {
        return okHttpProxy == null ? init() : okHttpProxy;
    }

    public void setHttpClient(OkHttpClient okHttpClient) {
        mHttpClient = okHttpClient;
    }


    private <T> void request(Request.Builder builder,
                             Map<String, String> headers, OkCallback<T> callback) throws PluginException {
        addHeaders(headers, builder);
        getHttpClient().newCall(builder.build()).enqueue(new OkHttpCallback<>(callback));
    }

    private <T> Response syncRequest(Request.Builder builder,
                                     Map<String, String> headers, OkCallback<T> callback) throws PluginException {
        addHeaders(headers, builder);
        try {
            Call call = getHttpClient().newCall(builder.build());
            Response response = call.execute();
            if (callback != null) {
                OkHttpCallback<T> httpCallback = new OkHttpCallback<>(callback);
                httpCallback.onResponse(call, response);
            }
            return response;
        } catch (Exception e) {
            throwPluginException(e);
        }
        return null;
    }

    private void addHeaders(Map<String, String> headers, Request.Builder builder) {
        if (headers != null && headers.size() > 0) {
            builder.headers(Headers.of(headers));
        }
    }

    protected final RequestBody buildRequestBody(Map<String, Object> map) {
        return buildRequestBody(MediaType.parse("application/json; charset=utf-8"), map);
    }

    protected final RequestBody buildRequestBody(MediaType mediaType, Map<String, Object> map) {
        //补全请求地址
        MultipartBody.Builder builder = new MultipartBody.Builder();
        //设置类型
        builder.setType(MultipartBody.FORM);
        Set<String> keys = map.keySet();
        //追加参数
        for (String key : keys) {
            final Object object = map.get(key);
            if (object instanceof File) {
                final File file = (File) object;
                builder.addFormDataPart(key, file.getName(), RequestBody.create(file, mediaType));
            } else if (object != null) {
                builder.addFormDataPart(key, object.toString());
            }
        }
        return builder.build();
    }

    @Override
    public <T> void get(String url, final OkCallback<T> callback) throws PluginException {
        get(url, null, null, callback);
    }

    @Override
    public <T> Response syncGet(String url) throws PluginException {
        return syncGet(url, null);
    }

    @Override
    public <T> Response syncGet(String url, OkCallback<T> callback) throws PluginException {
        return syncGet(url, null, null, null);
    }

    @Override
    public <T> void get(String url, HashMap<String, String> headers, final OkCallback<T> callback) throws PluginException {
        get(url, null, headers, callback);
    }


    @Override
    public <T> void get(String url, Map<String, Object> params, HashMap<String, String> headers,
                        final OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl != null) {
            HttpUrl.Builder httpBuilder = httpUrl.newBuilder();
            buildGetParams(httpBuilder, params);
            builder.url(httpBuilder.build());
        }
        request(builder.get(), headers, callback);
    }

    @Override
    public <T> Response syncGet(String url, Map<String, Object> params, HashMap<String, String> headers,
                                OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        HttpUrl httpUrl = HttpUrl.parse(url);
        if (httpUrl != null) {
            HttpUrl.Builder httpBuilder = httpUrl.newBuilder();
            buildGetParams(httpBuilder, params);
            builder.url(httpBuilder.build());
        }
        return syncRequest(builder.get(), headers, callback);
    }

    public <T> void post(String url, String json, String contentType, OkCallback<T> callback) throws PluginException {
        post(url, json, null, contentType, callback);
    }

    @Override
    public <T> void post(String url, Map<String, Object> params, OkCallback<T> callback) throws PluginException {
        post(url, params, null, callback);
    }

    @Override
    public <T> void post(String url, Map<String, Object> params, HashMap<String, String> headers,
                         OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(buildRequestBody(params));
        request(builder, headers, callback);
    }

    @Override
    public <T> void post(String url, String json, HashMap<String, String> headers,
                         String contentType, OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(RequestBody.create(json, MediaType.parse(contentType)));
        request(builder, headers, callback);
    }

    @Override
    public <T> Response syncPostFile(String url, Map<String, Object> params, OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(createFileRequestBody(params));
        return syncRequest(builder, null, callback);
    }

    @Override
    public <T> Response syncPost(String url, String json, OkCallback<T> callback) throws PluginException {
        return syncPost(url, json, null, callback);
    }

    @Override
    public <T> Response syncPost(String url, String json, HashMap<String, String> headers, OkCallback<T> callback) throws PluginException {
        return syncPost(url, json, headers, JSON_TYPE, callback);
    }

    @Override
    public <T> Response syncPost(String url, String json, HashMap<String, String> headers, String contentType,
                                 OkCallback<T> callback) throws PluginException {
        return syncPost(url, json, headers, MediaType.parse(contentType), callback);
    }

    @Override
    public <T> Response syncPost(String url, String json, HashMap<String, String> headers, MediaType contentType,
                                 OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(RequestBody.create(json, contentType));
        return syncRequest(builder, headers, callback);
    }

    @Override
    public <T> Response syncPost(String url, Map<String, Object> params, HashMap<String, String> headers,
                                 MediaType contentType, OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(createRequestBody(params, contentType));
        return syncRequest(builder, headers, callback);
    }

    @Override
    public <T> Response syncPost(String url, Map<String, Object> params, HashMap<String, String> headers,
                                 OkCallback<T> callback) throws PluginException {
        return syncPost(url, params, headers, JSON_TYPE, callback);
    }

    @Override
    public <T> Response syncPost(String url, HashMap<String, Object> params, OkCallback<T> callback) throws PluginException {
        return syncPost(url, params, null, callback);
    }

    @Override
    public <T> void delete(String url, String json, HashMap<String, String> headers,
                           String contentType, OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.delete(RequestBody.create(json, MediaType.parse(contentType)));
        request(builder, headers, callback);
    }

    @Override
    public <T> void delete(String url, Map<String, Object> params, HashMap<String, String> headers,
                           OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.delete(createRequestBody(params, JSON_TYPE));
        request(builder, headers, callback);
    }

    @Override
    public <T> Response syncDelete(String url, Map<String, Object> params,
                                   HashMap<String, String> headers, OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.delete(createRequestBody(params, JSON_TYPE));
        return syncRequest(builder, headers, callback);
    }

    @Override
    public <T> void put(String url, String json, HashMap<String, String> headers,
                        String contentType, OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.put(RequestBody.create(json, MediaType.parse(contentType)));
        request(builder, headers, callback);
    }

    @Override
    public <T> void put(String url, Map<String, Object> params, HashMap<String, String> headers,
                        OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.put(buildRequestBody(params));
        request(builder, headers, callback);
    }

    @Override
    public <T> Response syncPut(String url, Map<String, Object> params, HashMap<String, String> headers,
                                OkCallback<T> callback) throws PluginException {
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.put(buildRequestBody(params));
        return syncRequest(builder, headers, callback);
    }

    public static void buildGetParams(HttpUrl.Builder httpBuilder, Map<String, Object> params) {
        if (params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                httpBuilder.addEncodedQueryParameter(entry.getKey(), Utils.toString(entry.getValue()));
            }
        }
    }

    /**
     * 请求参数
     *
     * @param params
     * @return
     */
    public static RequestBody createRequestBody(Map<String, Object> params,
                                                MediaType mediaType) {
        return createRequestBody(params, mediaType, true);
    }

    /**
     * 请求参数
     *
     * @param params
     * @return
     */
    public static RequestBody createRequestBody(Map<String, Object> params,
                                                MediaType mediaType,
                                                boolean isJsonParams) {
        if (isJsonParams) {
            //json 参数
            return RequestBody.create(new Gson().toJson(params), mediaType);
        } else {
            // Form表单
            FormBody.Builder builder = new FormBody.Builder();
            // add 参数
            Set<String> keys = params.keySet();
            for (String key : keys) {
                Object value = params.get(key);
                if (value != null) {
                    builder.add(key, Utils.toString(value));
                }
            }
            return builder.build();
        }
    }

    /**
     * 构建多部分请求体
     *
     * @param params
     * @return
     */
    public static MultipartBody createFileRequestBody(Map<String, Object> params) {
        MultipartBody.Builder builder = new MultipartBody.Builder();
        //设置类型
        builder.setType(MultipartBody.FORM);
        Set<String> keys = params.keySet();
        for (String key : keys) {
            final Object object = params.get(key);
            if (object instanceof File) {
                final File file = (File) object;
                builder.addFormDataPart(key, file.getName(), RequestBody.create(file, STREAM_TYPE));
            } else if (object instanceof FileWrapper) {
                FileWrapper wrapper = (FileWrapper) object;
                builder.addFormDataPart(key, wrapper.customFileName,
                        RequestBody.create(wrapper.file, MediaType.parse(wrapper.contentType)));
            } else if (object != null) {
                builder.addFormDataPart(key, object.toString());
            }
        }
        return builder.build();
    }

    public static class FileWrapper implements Serializable {
        public final File file;
        public String contentType;
        public String customFileName;

        public FileWrapper(File file, String contentType, String customFileName) {
            this.file = file;
            this.contentType = contentType;
            this.customFileName = customFileName;
            if (StringUtils.isEmpty(contentType)) {
                this.contentType = "image/*";
            }
            if (StringUtils.isEmpty(customFileName)) {
                this.customFileName = file.getName();
            }
        }

        public FileWrapper(File file, String contentType) {
            this(file, contentType, file.getName());
        }

        public String getAbsolutePath() {
            return file != null ? file.getAbsolutePath() : "unknown";
        }
    }

    /**
     * 取消请求
     *
     * @param tag
     * @author dingpeihua
     * @date 2020/6/30 20:18
     * @version 1.0
     */
    public void cancel(Object tag) {
        Dispatcher dispatcher = getHttpClient().dispatcher();
        for (Call call : dispatcher.queuedCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
        for (Call call : dispatcher.runningCalls()) {
            if (tag.equals(call.request().tag())) {
                call.cancel();
            }
        }
    }
}
