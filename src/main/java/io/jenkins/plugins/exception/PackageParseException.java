package io.jenkins.plugins.exception;

public class PackageParseException extends PluginException {
    public PackageParseException(String message) {
        super(message);
    }

    public PackageParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public PackageParseException(Throwable cause) {
        super(cause);
    }
}
