package io.jenkins.plugins.http;

import io.jenkins.plugins.module.HttpResponse;
import io.jenkins.plugins.module.ServiceInfo;
import org.apache.commons.lang.StringUtils;

import java.util.HashMap;

class LogServiceApiImpl implements LogServiceApi {
    private OkHttpProxy okHttpProxy;
    private final String serviceIp;

    public LogServiceApiImpl(String serviceIp, OkHttpProxy okHttpProxy) {
        this.okHttpProxy = okHttpProxy;
        this.serviceIp = StringUtils.isEmpty(serviceIp) ? URL_UPLOAD_IP : serviceIp;
    }

    @Override
    public void uploadAppInfo(String params, OkCallback<HttpResponse<String>> callback) throws Exception {
        okHttpProxy.syncPost(formatUrl(URL_UPLOAD_APP, serviceIp), params, callback);
    }

    @Override
    public void uploadAppFile(HashMap<String, Object> params, OkCallback<HttpResponse<String>> callback) throws Exception {
        okHttpProxy.syncPostFile(formatUrl(URL_UPLOAD_FILE, serviceIp), params, callback);
    }

    @Override
    public void registerServiceIp(HashMap<String, Object> params, OkCallback<HttpResponse<ServiceInfo>> callback) throws Exception {
        okHttpProxy.syncPost(formatUrl(URL_REGISTER_SERVICE, serviceIp), params, callback);
    }

    @Override
    public void uploadMultiLanguage(String language, String params, OkCallback<HttpResponse<String>> callback) throws Exception {
        okHttpProxy.syncPost(formatUrl(URL_UPLOAD_STRINGS + language, serviceIp), params, callback);
    }

    @Override
    public void uploadMultiLanguage2(String bundleId, String params, OkCallback<HttpResponse<String>> callback) throws Exception {
        okHttpProxy.syncPost(formatUrl(URL_UPLOAD_STRINGS2 + bundleId, serviceIp), params, callback);
    }
}
