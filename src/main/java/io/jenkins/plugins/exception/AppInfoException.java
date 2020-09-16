package io.jenkins.plugins.exception;

public class AppInfoException extends PluginException {
    public AppInfoException(String message) {
        super(message);
    }

    public AppInfoException(Throwable cause) {
        super(cause);
    }

    public AppInfoException(String message, Throwable cause) {
        super(message, cause);
    }
}
