package io.jenkins.plugins.sample;

/**
 * 插架版本号
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/1/19 9:06
 */
public class AppPluginVersion {
    /**
     * 版本号必须保持3位
     */
    public static final String PLUGIN_VERSION = "2.0.1";

    private AppPluginVersion() {
        throw new AssertionError("Not support operate!");
    }
}
