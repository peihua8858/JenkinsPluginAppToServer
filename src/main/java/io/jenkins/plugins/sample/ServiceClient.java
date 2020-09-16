package io.jenkins.plugins.sample;

import com.google.gson.Gson;
import hudson.console.HyperlinkNote;
import io.jenkins.plugins.ILogPrintListener;
import io.jenkins.plugins.exception.AppInfoException;
import io.jenkins.plugins.exception.PluginException;
import io.jenkins.plugins.exception.UploadFileException;
import io.jenkins.plugins.http.HttpCallback;
import io.jenkins.plugins.http.LogServiceApi;
import io.jenkins.plugins.module.AppBean;
import io.jenkins.plugins.module.HttpResponse;
import io.jenkins.plugins.module.ServiceInfo;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.util.HashMap;

import static io.jenkins.plugins.util.Utils.throwPluginException;

public class ServiceClient {
    private final ILogPrintListener listener;
    private final LogServiceApi serviceApi;
    private final String downloadServiceIp;
    private final String uploadServiceIp;

    public ServiceClient(ILogPrintListener listener, String downloadServiceIp, String serviceIp) {
        this.listener = listener;
        this.serviceApi = LogServiceApi.createApi(serviceIp);
        this.downloadServiceIp = downloadServiceIp;
        this.uploadServiceIp = serviceIp;
    }

    private void println(String msg) {
        if (listener != null) {
            listener.println(msg);
        } else {
            System.out.println(msg);
        }
    }

    public void sendRequest(boolean isUploadAppFile, AppBean appBean) throws PluginException {
        try {
            //上传APP图标
            uploadFile(true, appBean.getIconPath(), appBean);
            if (isUploadAppFile) {
                //上传APP安装包
                uploadFile(false, appBean.getFilePath(), appBean);
            } else {
                //如果安装包在jenkin服务器，则需要上传jenkin服务器ip地址
                registerIpAddress(appBean, downloadServiceIp);
            }
            //上传APP数据信息
            serviceApi.uploadAppInfo(new Gson().toJson(appBean), new HttpCallback<HttpResponse<String>>() {
                @Override
                public void onSuccess(HttpResponse<String> response) throws PluginException {
                    String msg = "Failed to upload the app info [" + appBean + "].";
                    if (response != null) {
                        msg = response.getMsg();
                        if (response.isSuccess()) {
                            String data = response.getData();
                            if (data != null && data.length() > 1 && data.startsWith("/")) {
                                data = data.substring(1);
                            }
                            String url = LogServiceApi.formatUploadHost(data, uploadServiceIp);
                            println(io.jenkins.plugins.sample.Messages.AppPublisher_deployed() + "\n" +
                                    " Your app short url: " +
                                    HyperlinkNote.encodeTo(url, url));
                            return;
                        }
                    }
                    throw new AppInfoException(msg);
                }

                @Override
                public void onFailure(Throwable e) throws PluginException {
                    if (e instanceof AppInfoException) {
                        throwPluginException(e);
                    } else {
                        throw new AppInfoException(e);
                    }
                }
            });
        } catch (Exception e) {
            throwPluginException(e);
        }
    }

    /**
     * 上传文件
     *
     * @param filePath 文件路径
     * @param appBean  app 信息
     * @author dingpeihua
     * @date 2020/7/17 16:30
     * @version 1.0
     */
    public void uploadFile(boolean isIcon, String filePath, AppBean appBean) throws PluginException {
        try {
            String bundleId = appBean.getBundleId();
            println(">bundleId:" + bundleId);
            println(">filePath:" + filePath);
            if (StringUtils.isEmpty(filePath)) {
                throw new UploadFileException("The app file path is empty.");
            }
            if (StringUtils.isEmpty(bundleId)) {
                throw new UploadFileException("The app bundle id is empty.");
            }
            HashMap<String, Object> params = new HashMap<>();
            params.put("bundleId", bundleId);
            params.put("appName", appBean.getName());
            params.put("file", new File(filePath));
            params.put("versionName", appBean.getVersionName());
            params.put("versionCode", appBean.getVersionCode());
            params.put("buildType", appBean.getBuildType());
            params.put("platform", appBean.getPlatform());
            serviceApi.uploadAppFile(params, new HttpCallback<HttpResponse<String>>() {
                @Override
                public void onSuccess(HttpResponse<String> response) throws PluginException {
                    String msg = "Failed to upload the file \"" + filePath + "\".";
                    if (response != null) {
                        msg = response.getMsg();
                        if (response.isSuccess()) {
                            String data = response.getData();
                            println(">data:" + data);
                            if (!StringUtils.isEmpty(data)) {
                                if (isIcon) {
                                    appBean.setIconPath(data);
                                } else {
                                    appBean.setFilePath(data);
                                    appBean.setDownloadUrl("/" + data);
                                }
                                return;
                            }
                        }
                    }
                    throw new UploadFileException(msg);
                }

                @Override
                public void onFailure(Throwable e) throws PluginException {
                    if (e instanceof UploadFileException) {
                        throwPluginException(e);
                    } else {
                        throw new UploadFileException(e);
                    }
                }
            });
        } catch (Exception e) {
            throwPluginException(e);
        }
    }

    /**
     * 注册IP地址到服务器
     *
     * @param appBean
     * @param ipAddress ip地址➕端口号
     * @author dingpeihua
     * @date 2020/4/28 18:02
     * @version 1.0
     */
    public void registerIpAddress(AppBean appBean, String ipAddress) throws RuntimeException {
        try {
            HashMap<String, Object> data = new HashMap<>();
            data.put("ipAddress", ipAddress);
            serviceApi.registerServiceIp(data, new HttpCallback<HttpResponse<ServiceInfo>>() {
                @Override
                public void onSuccess(HttpResponse<ServiceInfo> response) throws PluginException {
                    println("RegisterIp>>httpResponse:" + response);
                    String msg = "register service ip address failure.";
                    if (response != null) {
                        msg = response.getMsg();
                        if (response.isSuccess()) {
                            ServiceInfo serviceInfo = response.getData();
                            if (serviceInfo != null) {
                                appBean.setServerId(serviceInfo.getId());
                                return;
                            }
                        }
                    }
                    throw new PluginException(msg);
                }

                @Override
                public void onFailure(Throwable e) throws PluginException {
                    throwPluginException(e);
                }
            });
        } catch (Exception e) {
            throwPluginException(e);
        }
    }

    /**
     * 上传多语言到服务器
     *
     * @param bundleId 包名
     * @param data     多语言数据
     * @author dingpeihua
     * @date 2020/7/17 16:30
     * @version 1.0
     */
    public void uploadMultiLanguage2(String bundleId, Object data) throws AppInfoException {
        try {
            serviceApi.uploadMultiLanguage2(bundleId, new Gson().toJson(data), new HttpCallback<HttpResponse<String>>() {
                @Override
                public void onSuccess(HttpResponse<String> response) throws PluginException {
                    println("multiLanguage>>" + response);
                    String msg = "upload multi language failure.";
                    if (response != null) {
                        msg = response.getMsg();
                        if (response.isSuccess()) {
                            return;
                        }
                    }
                    throw new PluginException(msg);
                }

                @Override
                public void onFailure(Throwable e) throws PluginException {
                    throwPluginException(e);
                }
            });
        } catch (Exception e) {
            throwPluginException(e);
        }
    }

    /**
     * 上传多语言到服务器
     *
     * @param language 语言简码
     * @param data     多语言数据
     * @author dingpeihua
     * @date 2020/7/17 16:30
     * @version 1.0
     */
    public void uploadMultiLanguage(String language, Object data) throws AppInfoException {
        try {
            serviceApi.uploadMultiLanguage(language, new Gson().toJson(data), new HttpCallback<HttpResponse<String>>() {
                @Override
                public void onSuccess(HttpResponse<String> response) throws PluginException {
                    println("multiLanguage>>" + response);
                    String msg = "upload multi language failure.";
                    if (response != null) {
                        msg = response.getMsg();
                        if (response.isSuccess()) {
                            return;
                        }
                    }
                    throw new PluginException(msg);
                }

                @Override
                public void onFailure(Throwable e) throws PluginException {
                    throwPluginException(e);
                }
            });
        } catch (Exception e) {
            throwPluginException(e);
        }
    }

//    protected void uploadIcon(String url, AppBean appBean) throws PluginException {
//        String iconPath = "";
//        try {
//            String bundleId = appBean.getBundleId();
//            iconPath = appBean.getIconPath();
//            println("uploadIcon>>>bundleId:" + bundleId);
//            println("uploadIcon>>>iconPath:" + iconPath);
//            if (StringUtils.isEmpty(iconPath)) {
//                throw new UploadFileException("Icon file path is empty.");
//            }
//            String result = postFile(url, MediaType.parse("image/*"), bundleId, appBean.getName(),
//                    appBean.getVersionName(), appBean.getVersionCode(),
//                    appBean.getBuildType(), appBean.getPlatform(),
//                    iconPath);
//            if (!StringUtils.isEmpty(result)) {
//                HttpResponse<String> response = Utils.parseResponse(result);
//                if (response != null) {
//                    if (response.isSuccess()) {
//                        String data = response.getData();
//                        println("uploadIcon>>>data:" + data);
//                        if (!StringUtils.isEmpty(data)) {
//                            appBean.setIconPath(data);
//                            return;
//                        }
//                    }
//                    throw new RuntimeException(response.getMsg());
//                }
//            }
//            throw new RuntimeException("upload Icon file  failure.");
//        } catch (Exception e) {
//            throw new UploadFileException(e);
//        } finally {
//            try {
//                if (StringUtils.isNotEmpty(iconPath)) {
//                    FileUtils.forceDelete(new File(iconPath));
//                }
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//    }

//    public Response postFile(String url, MediaType mediaType, final Map<String, Object> map) throws IOException {
//        url = url + Configs.URL_UPLOAD_FILE;
//        //补全请求地址
//        MultipartBody.Builder builder = new MultipartBody.Builder();
//        //设置类型
//        builder.setType(MultipartBody.FORM);
//        Set<String> keys = map.keySet();
//        //追加参数
//        for (String key : keys) {
//            final Object object = map.get(key);
//            if (object instanceof File) {
//                final File file = (File) object;
//                builder.addFormDataPart(key, file.getName(), RequestBody.create(file, mediaType));
//            } else {
//                builder.addFormDataPart(key, object.toString());
//            }
//        }
//        Request request = new Request.Builder().url(url).post(builder.build()).build();
//        return httpClient.newCall(request).execute();
//    }

    //    public String postFile(String url, MediaType mediaType, String bundleId, String appName,
//                           String versionName, String versionCode, String buildType,
//                           String platform,
//                           String filePath) throws PluginException {
//        try {
//            Map<String, Object> params = new HashMap<>();
//            params.put("bundleId", bundleId);
//            params.put("appName", appName);
//            params.put("file", new File(filePath));
//            params.put("versionName", versionName);
//            params.put("versionCode", versionCode);
//            params.put("buildType", buildType);
//            params.put("platform", platform);
//            println(">filePath:" + filePath);
//            Response response = postFile(url, mediaType, params);
//            ResponseBody body;
//            String content = (body = response.body()) != null ? body.string() : "";
//            if (response.isSuccessful()) {
//                return content;
//            }
//            return response.message();
//        } catch (Exception e) {
//            throw new UploadFileException(e);
//        }
//    }
//    public boolean uploadApp(String url, AppBean appBean) throws PluginException {
//        try {
//            String bundleId = appBean.getBundleId();
//            String filePath = appBean.getFilePath();
//            println(">bundleId:" + bundleId);
//            println(">filePath:" + filePath);
//            if (StringUtils.isEmpty(filePath)) {
//                throw new UploadFileException("the app file path is empty.");
//            }
//            HashMap<String, Object> params = new HashMap<>();
//            params.put("bundleId", bundleId);
//            params.put("appName", appBean.getName());
//            params.put("file", new File(filePath));
//            params.put("versionName", appBean.getVersionName());
//            params.put("versionCode", appBean.getVersionCode());
//            params.put("buildType", appBean.getBuildType());
//            params.put("platform", appBean.getPlatform());
//            serviceApi.uploadAppFile(params, new HttpCallback<HttpResponse<String>>() {
//                @Override
//                public void onSuccess(HttpResponse<String> response) throws PluginException {
//                    if (response != null) {
//                        if (response.isSuccess()) {
//                            String data = response.getData();
//                            println(">data:" + data);
//                            if (!StringUtils.isEmpty(data)) {
//                                appBean.setFilePath(data);
//                                appBean.setDownloadUrl("/" + data);
//                                return;
//                            }
//                        }
//                        throw new PluginException(response.getMsg());
//                    }
//                }
//
//                @Override
//                public void onFailure(Throwable e) throws PluginException {
//                    throw new PluginException(e);
//                }
//            });
////            String result = postFile(url, MediaType.parse("application/octet-stream"), bundleId, appBean.getName(),
////                    appBean.getVersionName(), appBean.getVersionCode(),
////                    appBean.getBuildType(), appBean.getPlatform(),
////                    filePath);
////            println(">result:" + result);
////            if (!StringUtils.isEmpty(result)) {
////                HttpResponse<String> response = Utils.parseResponse(result);
////                if (response != null) {
////                    if (response.isSuccess()) {
////                        String data = response.getData();
////                        println(">data:" + data);
////                        if (!StringUtils.isEmpty(data)) {
////                            appBean.setFilePath(data);
////                            appBean.setDownloadUrl("/" + data);
////                            return true;
////                        }
////                    }
////                    throw new RuntimeException(response.getMsg());
////                }
////            }
//        } catch (Exception e) {
//            throw new UploadFileException(e);
//        }
//        return false;
//    }
//    /**
//     * 注册IP地址到服务器
//     *
//     * @param appBean
//     * @param ipAddress ip地址➕端口号
//     * @author dingpeihua
//     * @date 2020/4/28 18:02
//     * @version 1.0
//     */
//    public boolean registerIpAddress(String url, AppBean appBean, String ipAddress) throws Exception {
//            RequestBody requestBody = RequestBody.create(new Gson().toJson(data), Configs.JSON);
//            Request request = new Request.Builder().url().post(requestBody).build();
//            Response response = httpClient.newCall(request).execute();
//            println("RegisterIp>>message:" + response.message() + ",code:" + response.code());
//            println("RegisterIp>>" + response.message());
//            if (response.isSuccessful()) {
//                ResponseBody body = null;
//                String result = (body = response.body()) != null ? body.string() : "";
//                HttpResponse<ServiceInfo> httpResponse = new Gson().fromJson(result, new TypeToken<HttpResponse<ServiceInfo>>() {
//                }.getType());
//                println("RegisterIp>>httpResponse:" + httpResponse);
//                if (httpResponse != null && httpResponse.isSuccess()) {
//                    ServiceInfo serviceInfo = httpResponse.getData();
//                    if (serviceInfo != null) {
//                        appBean.setServerId(serviceInfo.getId());
//                        return true;
//                    }
//                }
//            }
//            return true;
//        } catch (Exception e) {
//            println("RegisterIp>>" + Utils.getStackTraceMessage(e));
//        }
//        return false;
//    }
}
