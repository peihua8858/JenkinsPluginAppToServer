package io.jenkins.plugins;

import hudson.model.BuildListener;

/**
 * 日志打印接口
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/7/17 18:33
 */
public interface ILogPrintListener {
    /**
     * 打印文本信息
     *
     * @param msg
     */
    void println(String msg);

    /**
     * 打印错误信息
     */
    void error(String msg);

    /**
     * 接收构建过程中所发生的事件。
     *
     * @return
     */
    BuildListener getListener();
}
