package io.jenkins.plugins.http;

import io.jenkins.plugins.module.HttpResponse;
import io.jenkins.plugins.module.ServiceInfo;
import org.apache.commons.lang.StringUtils;

import java.text.MessageFormat;
import java.util.HashMap;

/**
 * 日志服务器Api
 * 接口文档: http://10.32.5.200:8090/doc.html#/home
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/6/30 15:51
 */
public interface LogServiceApi {
    /**
     * 上传服务器地址
     */
    String URL_UPLOAD_IP = "10.32.5.200:8090";
    /**
     * 上传服务器地址
     */
    String URL_HTTP_FORMAT = "http://{0}/{1}";
    /**
     * 上传APP信息接口路径
     */
    String URL_UPLOAD_APP = "/app/uploadInfo";
    /**
     * 注册Jenkins服务器IP及端口号
     */
    String URL_REGISTER_SERVICE = "/app/registerService";
    /**
     * 上传APP文件接口路径
     */
    String URL_UPLOAD_FILE = "/app/uploadFile";
    /**
     * 上传多语言文案接口路径
     */
    String URL_UPLOAD_STRINGS = "/strings/saveList/";
    /**
     * 上传多语言文案接口路径
     */
    String URL_UPLOAD_STRINGS2 = "/strings/save/";

    static String formatUploadHost(String url, String host) {
        host = StringUtils.isEmpty(host) ? URL_UPLOAD_IP : host;
        return MessageFormat.format(URL_HTTP_FORMAT, host, url);
    }

    default String formatUrl(String url, String hostIp) {
        return formatUploadHost(url, hostIp);
    }

    static LogServiceApi createApi(String serviceIp) {
        return createApi(serviceIp, OkHttpProxy.getInstance());
    }

    static LogServiceApi createApi(String serviceIp, OkHttpProxy proxy) {
        return new LogServiceApiImpl(serviceIp, proxy);
    }

    /**
     * 上传APP信息数据
     *
     * @param params 请求参数
     * @author dingpeihua
     * @date 2020/7/1 9:52
     * @version 1.0
     */
    void uploadAppInfo(String params, OkCallback<HttpResponse<String>> callback) throws Exception;

    /**
     * 上传APP文件
     *
     * @param params   请求参数
     * @param callback 请求成功回调
     * @author dingpeihua
     * @date 2020/7/1 9:52
     * @version 1.0
     */
    void uploadAppFile(HashMap<String, Object> params, OkCallback<HttpResponse<String>> callback) throws Exception;

    /**
     * 注册Jenkins服务器IP地址
     *
     * @param params   请求参数
     * @param callback 请求成功回调
     * @author dingpeihua
     * @date 2020/7/1 9:52
     * @version 1.0
     */
    void registerServiceIp(HashMap<String, Object> params, OkCallback<HttpResponse<ServiceInfo>> callback) throws Exception;

    /**
     * 上传多语言
     *
     * @param language 语言简码
     * @param params   请求参数
     * @param callback 请求成功回调
     * @author dingpeihua
     * @date 2020/7/1 9:54
     * @version 1.0
     */
    void uploadMultiLanguage(String language, String params, OkCallback<HttpResponse<String>> callback) throws Exception;
    /**
     * 上传多语言
     *
     * @param bundleId app包名
     * @param params   请求参数
     * @param callback 请求成功回调
     * @author dingpeihua
     * @date 2020/7/1 9:54
     * @version 1.0
     */
    void uploadMultiLanguage2(String bundleId, String params, OkCallback<HttpResponse<String>> callback) throws Exception;

}
