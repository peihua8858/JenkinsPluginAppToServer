package io.jenkins.plugins.helper;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.*;
import hudson.scm.ChangeLogSet;
import io.jenkins.plugins.ILogPrintListener;
import io.jenkins.plugins.IParser;
import io.jenkins.plugins.exception.PluginException;
import io.jenkins.plugins.exception.UploadFileException;
import io.jenkins.plugins.http.LogServiceApi;
import io.jenkins.plugins.module.AppBean;
import io.jenkins.plugins.sample.*;
import io.jenkins.plugins.util.Utils;
import jenkins.model.Jenkins;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 * App 上传操作
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/1/17 12:02
 */
public class UploadHelper {

    /**
     * App上传操作
     *
     * @author dingpeihua
     * @date 2020/1/16 15:31
     * @version 1.0
     */
    public static boolean upload(AppPublisher appPublisher, AbstractBuild<?, ?> build, final ILogPrintListener listener) throws IOException, InterruptedException, PluginException {
        Result result = build.getResult();
        boolean buildFailure = result == null || result.isWorseOrEqualTo(Result.FAILURE);
        if (buildFailure) {
            listener.println("Build FAILURE");
            return true;
        }
        EnvVars environment = build.getEnvironment(listener.getListener());

        Node buildNode = Jenkins.get().getNode(build.getBuiltOnStr());
        System.out.println("exIncludesPattern" + appPublisher.exIncludesPattern);
        FileFinder fileFinder = new FileFinder("**/*.apk,**/*.ipa", appPublisher.exIncludesPattern);
        FilePath rootDir;
        List<? extends Run<?, ?>.Artifact> artifacts =
                build.getProject().getBuildByNumber(build.number).getArtifactsUpTo(build.number);
        boolean isUploadAppFile = isUploadAppFile(artifacts, appPublisher.isUploadAppFile());
        if (!StringUtils.isBlank(appPublisher.appPath)) {
            appPublisher.appPath = environment.expand(appPublisher.appPath);
            System.out.println(appPublisher.appPath);
            if (buildNode != null) {
                rootDir = new FilePath(buildNode.getChannel(), appPublisher.appPath);
            } else {
                rootDir = new FilePath(new File(appPublisher.appPath));
            }
        } else {
            rootDir = isUploadAppFile ? build.getWorkspace() : new FilePath(build.getRootDir());
            if (rootDir == null) {
                listener.error(io.jenkins.plugins.sample.Messages.AppPublisher_buildWorkspaceUnavailable());
                return false;
            }
        }
        listener.println("rootDir ===== " + rootDir);
        Iterator<? extends ChangeLogSet.Entry> changeLogIt = build.getChangeSet().iterator();
        StringBuilder changeLog = new StringBuilder();
        int index = 1;
        while (changeLogIt.hasNext()) {
            final ChangeLogSet.Entry entry = changeLogIt.next();
            changeLog.append(index).append(".")
                    .append(entry.getMsg())
                    .append(" (")
                    .append(entry.getAuthor())
                    .append(")")
                    .append("\n");
            index++;
        }
        listener.println("Plugin>>changeLog : " + changeLog);
        listener.println(io.jenkins.plugins.sample.Messages.AppPublisher_RootDirectory(rootDir));
        String downloadServiceIp = Configs.URL_DOWNLOAD_IP;
        String uploadServiceIp = LogServiceApi.URL_UPLOAD_IP;
        if (StringUtils.isNotEmpty(appPublisher.downloadServiceIp)) {
            downloadServiceIp = appPublisher.downloadServiceIp;
        }
        if (StringUtils.isNotEmpty(appPublisher.uploadServiceIp)) {
            uploadServiceIp = appPublisher.uploadServiceIp;
        }
        ServiceClient client = new ServiceClient(listener, downloadServiceIp, uploadServiceIp);
        FilePath workspace = build.getWorkspace();
        listener.println("workspace ===== " + workspace);
        try {
            AppBean appBean;
            if (isUploadAppFile(artifacts, isUploadAppFile)) {
                appBean = processWorkspaceFile(client, build, appPublisher, listener, rootDir, fileFinder,
                        changeLog);
            } else {
                appBean = processArtifact(client, build, artifacts, appPublisher, listener, rootDir,
                        fileFinder, changeLog);
            }
            listener.println("Plugin>>UploadMultiLanguage : " + appPublisher.isUploadMultiLanguage());
            if (appBean != null) {
                boolean isIOS = "ios".equalsIgnoreCase(appBean.getPlatform());
                if (!isIOS && appPublisher.isUploadMultiLanguage()) {
                    listener.println("upload multi language ===== ");
                    StringsUploadHelper2 helper = new StringsUploadHelper2(client, appBean,
                            appPublisher.includesStringPattern, workspace, listener);
                    helper.uploadMultiLanguage();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            listener.println(io.jenkins.plugins.sample.Messages.AppPublisher_deploymentFailed(e.getMessage()));
            throw new PluginException(e);
        }
        return true;
    }

    /**
     * @author dingpeihua
     * @date 2020/1/17 10:57
     * @version 1.0
     */
    private static AppBean processWorkspaceFile(ServiceClient client, AbstractBuild<?, ?> build, AppPublisher appPublisher,
                                                ILogPrintListener listener, FilePath rootDir, FileFinder fileFinder,
                                                StringBuilder changeLog) throws PluginException {
        try {
            List<String> fileNames = rootDir.act(fileFinder);
            String projectName = build.getProject().getName();
            listener.println(io.jenkins.plugins.sample.Messages.AppPublisher_foundFiles(fileNames));
            if (fileNames.isEmpty()) {
                listener.error(io.jenkins.plugins.sample.Messages.AppPublisher_noArtifactsFound("**/*.apk,**/*.ipa"));
                return null;
            }
            AppBean appBean = null;
            for (String filename : fileNames) {
                appBean = uploadApp(build, appPublisher, listener, changeLog, projectName, filename,
                        rootDir, true, client);
            }
            return appBean;
        } catch (Exception e) {
            throw new PluginException(e);
        }
    }

    private static AppBean processArtifact(ServiceClient client, AbstractBuild<?, ?> build, List<? extends Run<?, ?>.Artifact> artifacts,
                                           AppPublisher appPublisher,
                                           ILogPrintListener listener, FilePath rootDir, FileFinder fileFinder,
                                           StringBuilder changeLog) throws PluginException {
        AppBean appBean = null;
        try {
            String projectName = build.getProject().getName();
            listener.println("Plugin>>>rootFile : " + rootDir.getRemote() + "!");
            FilePath appFileRoot;
            if (artifacts == null || artifacts.size() == 0) {
                appFileRoot = rootDir;
            } else {
                appFileRoot = new FilePath(rootDir, "archive");
            }
            listener.println("Plugin>>appFileRoot : " + appFileRoot);
            List<String> fileNames = appFileRoot.act(fileFinder);
            listener.println(io.jenkins.plugins.sample.Messages.AppPublisher_foundFiles(fileNames));
            for (String filename : fileNames) {
                appBean = uploadApp(build, appPublisher, listener, changeLog, projectName, filename,
                        appFileRoot, false, client);
            }
        } catch (Exception e) {
            throw new PluginException(e);
        }
        return appBean;
    }

    private static AppBean uploadApp(
            AbstractBuild<?, ?> build, AppPublisher appPublisher, ILogPrintListener listener, StringBuilder changeLog,
            String projectName, String filename, FilePath rootDir, boolean isUploadApp,
            ServiceClient client) throws PluginException {
        AppBean appBean = null;
        File tmpArchive = null;
        try {
            listener.println("Plugin>>filename : " + filename);
            listener.println("Plugin>>projectName : " + projectName);
            listener.println("Plugin>>rootDir : " + rootDir);
            tmpArchive = File.createTempFile(filename.replace("." + FilenameUtils.getExtension(filename), ""), "." +
                    FilenameUtils.getExtension(filename));
            listener.println("Plugin>>tmpArchive : " + tmpArchive);
            FilePath tmpLocalFile = new FilePath(tmpArchive);
            FilePath remoteFile = rootDir.child(filename);
            listener.println("Plugin>>tmpLocalFile : " + tmpLocalFile);
            listener.println("Plugin>>remoteFile : " + remoteFile);
            remoteFile.copyTo(tmpLocalFile);
            String remoteFilePath = tmpLocalFile.getRemote();
            listener.println("Plugin>>remoteFilePath : " + remoteFilePath);
            appBean = parseApp(appPublisher.parsers, listener, new File(tmpLocalFile.getRemote()));
            if (appBean == null) {
                return null;
            }
            boolean isIOSPlatform = "ios".equalsIgnoreCase(appBean.getPlatform());
            appBean.setFilePath(remoteFilePath);
            appBean.setBuildNumber(build.number);
            appBean.setBuildType(Utils.getBuildType(isIOSPlatform,
                    appBean.getBuildType(), appPublisher.checkBuildType(),
                    new File(remoteFilePath)));
            appBean.setFilePathType(isUploadApp ? Configs.APK_FILE_TYPE_SERVICE : Configs.APK_FILE_TYPE_JENKINS);
            appBean.setProjectName(projectName);
            appBean.setFileSize(tmpLocalFile.length());
            appBean.setFileName(new File(filename).getName());
            appBean.setChangeLog(changeLog.toString());
            appBean.setDuration(build.getDuration());
            StringBuilder sbDownloadUrl = new StringBuilder();
            String jenkinsJobPath = Configs.checkJenkinsJobPath(!isIOSPlatform, appPublisher.jenkinsJob);
            sbDownloadUrl.append(jenkinsJobPath);
            if (isIOSPlatform) {
                if (appBean.getBuildType() == null) {
                    throw new PluginException("ERROR: Not support this ipa type, only support inhouse or adhoc ipa, " +
                            "please check your configure.");
                }
            }
            sbDownloadUrl.append(projectName).append("/").append(build.number).append("/artifact/").append(filename);
            //文件路径
            appBean.setFilePath(isUploadApp ? remoteFilePath : sbDownloadUrl.toString());
            //下载地址，相对路径，不带ip地址
            appBean.setDownloadUrl(sbDownloadUrl.toString());
            client.sendRequest(isUploadApp, appBean);
        } catch (Exception e) {
            throw new PluginException(e);
        } finally {
            FileUtils.deleteQuietly(tmpArchive);
        }
        return appBean;
    }

    static boolean isUploadAppFile(List<?> artifacts, boolean isUploadAppFile) {
        return isUploadAppFile || (artifacts == null || artifacts.size() == 0);
    }

    private static AppBean parseApp(HashMap<String, IParser> parsers, ILogPrintListener listener, File appFile) throws UploadFileException {
        String extension = FilenameUtils.getExtension(appFile.getName());
        listener.println("Plugin==   extension " + extension);
        try {
            if (StringUtils.isNotEmpty(extension)) {
                return parsers.get(extension.toLowerCase(Locale.ENGLISH)).onParser(listener, appFile);
            }
        } catch (Exception e) {
            throw new UploadFileException(e);
        }
        return null;
    }
}
