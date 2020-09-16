package io.jenkins.plugins.http;

import io.jenkins.plugins.exception.PluginException;
import okhttp3.MediaType;
import okhttp3.Response;

import java.util.HashMap;
import java.util.Map;

/**
 * Http 请求代理
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/7/1 15:20
 */
public interface IOkHttpProxy {
    <T> void get(String url, final OkCallback<T> callback) throws PluginException;

    <T> Response syncGet(String url) throws PluginException;

    <T> Response syncGet(String url, final OkCallback<T> callback) throws PluginException;

    <T> void get(String url, HashMap<String, String> headers, final OkCallback<T> callback) throws PluginException;

    <T> void get(String url, Map<String, Object> params, HashMap<String, String> headers,
                 final OkCallback<T> callback) throws PluginException;

    <T> Response syncGet(String url, Map<String, Object> params, HashMap<String, String> headers,
                         final OkCallback<T> callback) throws PluginException;

    <T> void post(String url, Map<String, Object> params, final OkCallback<T> callback) throws PluginException;

    <T> void post(String url, Map<String, Object> params, HashMap<String, String> headers,
                  final OkCallback<T> callback) throws PluginException;

    <T> void post(String url, String json, HashMap<String, String> headers, String contentType,
                  OkCallback<T> callback) throws PluginException;

    <T> Response syncPostFile(String url, Map<String, Object> params, OkCallback<T> callback) throws PluginException;

    <T> Response syncPost(String url, String json, OkCallback<T> callback) throws PluginException;

    <T> Response syncPost(String url, String json, HashMap<String, String> headers,
                          OkCallback<T> callback) throws PluginException;

    <T> Response syncPost(String url, String json, HashMap<String, String> headers, String contentType,
                          OkCallback<T> callback) throws PluginException;

    <T> Response syncPost(String url, String json, HashMap<String, String> headers, MediaType contentType, OkCallback<T> callback) throws PluginException;

    <T> Response syncPost(String url, Map<String, Object> params, HashMap<String, String> headers, MediaType contentType, final OkCallback<T> callback) throws PluginException;

    <T> Response syncPost(String url, Map<String, Object> params, HashMap<String, String> headers, final OkCallback<T> callback) throws PluginException;

    <T> Response syncPost(String url, HashMap<String, Object> params, final OkCallback<T> callback) throws PluginException;

    <T> void delete(String url, String json, HashMap<String, String> headers, String contentType,
                    OkCallback<T> callback) throws PluginException;

    <T> void delete(String url, Map<String, Object> params, HashMap<String, String> headers,
                    final OkCallback<T> callback) throws PluginException;

    <T> Response syncDelete(String url, Map<String, Object> params, HashMap<String, String> headers,
                            final OkCallback<T> callback) throws PluginException;


    <T> void put(String url, String json, HashMap<String, String> headers, String contentType,
                 OkCallback<T> callback) throws PluginException;

    <T> void put(String url, Map<String, Object> params, HashMap<String, String> headers,
                 final OkCallback<T> callback) throws PluginException;

    <T> Response syncPut(String url, Map<String, Object> params, HashMap<String, String> headers,
                         final OkCallback<T> callback) throws PluginException;
}
