package io.jenkins.plugins.module;

import io.jenkins.plugins.sample.AppPluginVersion;
import io.jenkins.plugins.sample.AppPublisher;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.apache.commons.lang.StringUtils;

@Setter
@Getter
@ToString
public class AppBean {
    private String id;
    private int buildNumber;
    private String projectName;
    private String downloadUrl;
    private String fileName;
    private String filePath;
    private long fileSize;
    private String changeLog;
    private long duration;
    private String iconPath;
    /**
     * 项目包名
     */
    private String bundleId;
    private String versionName;
    private String versionCode;
    private String platform;
    private String name;
    private String buildType;
    /**
     * {@link AppPublisher#isUploadAppFile}为true，或者Jenkins打包未归档，则当前apk文件已经上传到服务器，该参数应该取非1；
     * 当前APP文件没有上传到服务器，则表示是Jenkins目录，应该取1
     */
    private int filePathType;
    private final String pluginVersion = AppPluginVersion.PLUGIN_VERSION;
    /**
     * IOS plist文件地址
     */
    private String plistUrl;
    private Long serverId;
    /**
     * 编译描述
     */
    private String buildDescription;
    /**
     * APP安装包渠道名称
     */
    private String flavor;
    public String getProjectName(String nameAlias) {
        return StringUtils.isEmpty(nameAlias) ? projectName : nameAlias;
    }
}
