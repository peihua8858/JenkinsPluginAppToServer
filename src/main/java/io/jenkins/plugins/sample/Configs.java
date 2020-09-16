package io.jenkins.plugins.sample;

import okhttp3.MediaType;
import org.apache.commons.lang.StringUtils;

/**
 * 配置工具
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2019/7/20 11:21
 */
public class Configs {
    /**
     * 多语言编译后文件夹
     */
    public static final String STRINGS_BUILD_PATH_INCLUDE = "/build/**/values*/*.xml";
    /**
     * 多语言编译前文件夹
     */
    public static final String STRINGS_MAIN_PATH_INCLUDE = "/main/res/**/values*/strings.xml";
    /**
     * 项目文件夹
     */
    public static final String PROJECT_APP_FOLDER = "**/app/**";
    public static final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
    /**
     * 下载APP安装包地址
     */
    public static final String URL_DOWNLOAD_IP = "10.32.5.200:8080";
    /**
     * IOS Jenkins Job目录
     */
    public static final String JOB_IOS_JENKINS = "/job/";
    /**
     * Android Jenkins Job目录
     */
    public static final String JOB_ANDROID_JENKINS = "/jenkins/job/";

    /**
     * Jenkins服务器
     */
    public static final int APK_FILE_TYPE_JENKINS = 1;
    /**
     * 日志服务器
     */
    public static final int APK_FILE_TYPE_SERVICE = 0;

    public static String checkJenkinsJobPath(boolean isAndroid, String jenkinsJob) {
        if (StringUtils.isEmpty(jenkinsJob)) {
            return isAndroid ? JOB_ANDROID_JENKINS : JOB_IOS_JENKINS;
        }
        return jenkinsJob;
    }
}
