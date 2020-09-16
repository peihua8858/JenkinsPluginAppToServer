package io.jenkins.plugins.http;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import io.jenkins.plugins.exception.PluginException;
import okhttp3.Call;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.lang.reflect.Type;

/**
 * OkHttp 回调
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/6/30 19:36
 */
public final class OkHttpCallback<T> implements okhttp3.Callback {
    private final Gson mGson;
    private final OkCallback<T> callback;
    private TypeAdapter<T> adapter;

    public OkHttpCallback(OkCallback<T> callback) {
        this(callback, new GsonBuilder()
                .setLenient()
                .create());
    }

    public OkHttpCallback(OkCallback<T> callback, Gson gson) {
        this.mGson = gson;
        this.callback = callback;
        try {
            Type type = ReflectUtil.getSuperclassTypeParameter(callback);
            setAdapter(mGson.getAdapter(TypeToken.get(type)));
        } catch (Exception e) {
            throw new PluginException(e);
        }
    }

    void setAdapter(TypeAdapter<?> adapter) {
        this.adapter = (TypeAdapter<T>) adapter;
    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) throws RuntimeException {
        onFailure(e);
    }

    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws RuntimeException {
        if (response.isSuccessful()) {
            ResponseBody body = response.body();
            if (body != null) {
                try {
                    ByteArrayInputStream bis = new ByteArrayInputStream(body.bytes());
                    Reader reader = new BufferedReader(new InputStreamReader(bis));
                    JsonReader jsonReader = mGson.newJsonReader(reader);
                    T result = adapter.read(jsonReader);
                    reader.close();
                    bis.close();
                    onSuccess(result);
                } catch (Exception e) {
                    onFailure(e);
                } finally {
                    body.close();
                }
            } else {
                onSuccess(null);
            }
        } else {
            onFailure(new Exception(response.message()));
        }
    }

    private void onFailure(Exception e) throws PluginException {
        callback.onFailure(e);
    }

    private void onSuccess(T result) throws PluginException {
        callback.onSuccess(result);
    }
}
