package io.jenkins.plugins.exception;

/**
 * 上传文件异常
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/1/22 9:37
 */
public class UploadFileException extends PluginException {
    public UploadFileException(String message) {
        super(message);
    }

    public UploadFileException(Throwable cause) {
        super(cause);
    }

    public UploadFileException(String message, Throwable cause) {
        super(message, cause);
    }
}
