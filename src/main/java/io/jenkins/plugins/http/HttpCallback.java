package io.jenkins.plugins.http;

import io.jenkins.plugins.exception.PluginException;

/**
 * http 请求回调
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/7/17 16:35
 */
public abstract class HttpCallback<T> implements OkCallback<T> {

    @Override
    public void onSuccess(T response) throws PluginException {

    }

    @Override
    public void onFailure(Throwable e) throws PluginException {

    }
}
