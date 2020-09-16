package io.jenkins.plugins;

import io.jenkins.plugins.exception.PluginException;
import io.jenkins.plugins.module.AppBean;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * App 包解析器
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/1/16 15:21
 */
public interface IParser {
    /**
     * 文件创建
     *
     * @author dingpeihua
     * @date 2020/1/16 16:01
     * @version 1.0
     */
    default void createFile(byte[] bytes, String path, String name, AppBean app) {
        try {
            FileOutputStream fos = new FileOutputStream(path + "/" + name);
            fos.write(bytes);
            app.setIconPath(path + "/" + name);
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * App 包解析
     *
     * @param listener
     * @param appPath
     * @author dingpeihua
     * @date 2020/1/16 16:00
     * @version 1.0
     */
    AppBean onParser(ILogPrintListener listener, File appPath)throws PluginException;
}
