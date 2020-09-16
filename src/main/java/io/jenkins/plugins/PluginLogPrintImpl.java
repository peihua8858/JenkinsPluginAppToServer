package io.jenkins.plugins;

import hudson.model.BuildListener;

import java.io.PrintStream;

/**
 * 日志打印实现
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/7/17 18:34
 */
public class PluginLogPrintImpl implements ILogPrintListener {
    private PrintStream out;
    private BuildListener listener;

    public PluginLogPrintImpl(BuildListener listener) {
        this(listener.getLogger());
        this.listener = listener;
    }

    public PluginLogPrintImpl(PrintStream printStream) {
        this.out = printStream == null ? System.out : printStream;
    }

    @Override
    public void println(String msg) {
        out.println(msg);
    }

    @Override
    public void error(String msg) {
        if (listener != null) {
            listener.error(msg);
        } else {
            out.print("ERROR: ");
            out.println(msg);
        }
    }

    @Override
    public BuildListener getListener() {
        return listener;
    }
}
