package io.jenkins.plugins.http;

import io.jenkins.plugins.exception.PluginException;

/**
 * 请求返回
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/3/13 17:54
 */
public interface OkCallback<T> {

    void onSuccess(T response) throws PluginException;

    void onFailure(Throwable e) throws PluginException;
}
