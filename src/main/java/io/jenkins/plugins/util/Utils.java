package io.jenkins.plugins.util;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import hudson.model.*;
import io.jenkins.plugins.exception.PluginException;
import io.jenkins.plugins.http.OkHttpProxy;
import io.jenkins.plugins.module.HttpResponse;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ReflectionToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

public class Utils {
    //    private static final String IP_ADDRESS_AND_PORT = "(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,
    //    2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])\\.(\\d{1,2}|1\\d\\d|2[0-4]\\d|25[0-5])
    //    \\:([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-5]{2}[0-3][0-5])";
    private static final String IP_ADDRESS_AND_PORT =
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9])\\:([0-9]|[1-9]\\d{1,3}|[1-5]\\d{4}|6[0-5]{2}[0-3][0-5]))";
    private static final String IP_ADDRESS_STRING =
            "((25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9])\\.(25[0-5]|2[0-4]"
                    + "[0-9]|[0-1][0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1]"
                    + "[0-9]{2}|[1-9][0-9]|[1-9]|0)\\.(25[0-5]|2[0-4][0-9]|[0-1][0-9]{2}"
                    + "|[1-9][0-9]|[0-9]))";
    public static final Pattern IP_ADDRESS = Pattern.compile(IP_ADDRESS_STRING);
    public static final Pattern IP_ADDRESS_PORT = Pattern.compile(IP_ADDRESS_AND_PORT);

    /**
     * 验证是否是ip
     *
     * @param target 要验证的文本
     * @return 是返回true, 否则返回false
     */
    public static boolean isIpAddress(String target) {
        return StringUtils.isNotEmpty(target) && IP_ADDRESS.matcher(target).matches();
    }

    /**
     * 验证是否是ip加端口号
     *
     * @param target 要验证的文本
     * @return 是返回true, 否则返回false
     */
    public static boolean isIpAddressPort(String target) {
        return StringUtils.isNotEmpty(target) && IP_ADDRESS_PORT.matcher(target).matches();
    }

    /**
     * 获取apk编译类型
     *
     * @param buildType 编译类型
     * @param apkFile   apk 文件
     * @author dingpeihua
     * @date 2019/7/11 18:21
     * @version 1.0
     */
    public static String getBuildType(boolean isIOSPlatform, String oriBuildType, String buildType, File apkFile) {
        if (StringUtils.isNotEmpty(buildType)) {
            return buildType;
        }
        if (isIOSPlatform) {
            return oriBuildType;
        }
        if (apkFile == null) {
            return buildType;
        }
        return getBuildType(apkFile);
    }

    /**
     * 根据apk路径获取当前apk构建类型
     * 路径如：app/build/outputs/apk/release/demo.apk,截取release作为构建类型
     *
     * @param apkFile apk 文件
     * @author dingpeihua
     * @date 2019/8/27 16:07
     * @version 1.0
     */
    private static String getBuildType(File apkFile) {
        File file = apkFile.getParentFile();
        return file.getName();
    }

    /**
     * 去掉前后双引号
     *
     * @param text
     * @author dingpeihua
     * @date 2019/7/19 20:01
     * @version 1.0
     */
    public static String removeDoubleQuotes(String text) {
        if (StringUtils.isEmpty(text)) {
            return "";
        }
        if (text.startsWith("\"")) {
            text = text.substring(1);
        }
        if (text.endsWith("\"")) {
            text = text.substring(0, text.length() - 1);
        }
        return text;
    }

    public static String getStackTraceMessage(Exception ex) {
        StringWriter errors = new StringWriter();
        ex.printStackTrace(new PrintWriter(errors));
        return errors.toString();
    }

    public static HttpResponse<String> parseResponse(String result) {
        if (StringUtils.isEmpty(result)) {
            return null;
        }
        return new Gson().fromJson(result, new TypeToken<HttpResponse<String>>() {
        }.getType());
    }

    /**
     * 将Object对象转成String类型
     *
     * @param value
     * @return 如果value不能转成String，则默认""
     */
    public static String toString(Object value) {
        return toString(value, "");
    }

    /**
     * 将Object对象转成String类型
     *
     * @param value
     * @return 如果value不能转成String，则默认defaultValue
     */
    public static String toString(Object value, String defaultValue) {
        if (value instanceof String) {
            return (String) value;
        } else if (value != null) {
            return String.valueOf(value);
        }
        return defaultValue;
    }

    /**
     * 将Object对象转成boolean类型
     *
     * @param value
     * @return 如果value不能转成boolean，则默认false
     */
    public static Boolean toBoolean(Object value) {
        return toBoolean(value, false);
    }

    /**
     * 将Object对象转成boolean类型
     *
     * @param value
     * @return 如果value不能转成boolean，则默认defaultValue
     */
    public static Boolean toBoolean(Object value, boolean defaultValue) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        } else if (value instanceof String) {
            return "true".equalsIgnoreCase((String) value);
        }
        return defaultValue;
    }

    public static void throwPluginException(Throwable e) throws PluginException {
        if (e instanceof PluginException) {
            throw (PluginException) e;
        } else {
            throw new PluginException(e);
        }
    }

    public static String toString(BuildListener listener, Action action) {
        if (action instanceof ParametersAction) {
            return toString(listener, (ParametersAction) action);
        } else {
            return ReflectionToStringBuilder.toString(action, ToStringStyle.MULTI_LINE_STYLE);
        }
    }

    public static String toString(BuildListener listener, ParametersAction action) {
        return ReflectionToStringBuilder.toString(action, ToStringStyle.MULTI_LINE_STYLE);
    }

    public static String toString(BuildListener listener, Descriptor descriptor) {
        if (descriptor instanceof ParameterDefinition.ParameterDescriptor) {
            return toString(listener, (ParameterDefinition.ParameterDescriptor) descriptor);
        } else {
            return ReflectionToStringBuilder.toString(descriptor, ToStringStyle.MULTI_LINE_STYLE);
        }
    }

    public static String toString(BuildListener listener, ParameterDefinition.ParameterDescriptor descriptor) {
        return ReflectionToStringBuilder.toString(descriptor, ToStringStyle.MULTI_LINE_STYLE);
    }
}
