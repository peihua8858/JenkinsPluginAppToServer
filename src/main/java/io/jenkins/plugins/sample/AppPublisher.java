package io.jenkins.plugins.sample;

import hudson.Extension;
import hudson.Launcher;
import hudson.model.*;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.util.FormValidation;
import io.jenkins.plugins.IParser;
import io.jenkins.plugins.PluginLogPrintImpl;
import io.jenkins.plugins.apk.ApkParser;
import io.jenkins.plugins.exception.PluginException;
import io.jenkins.plugins.helper.UploadHelper;
import io.jenkins.plugins.ipa.IpaParser;
import io.jenkins.plugins.util.Utils;
import net.sf.json.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.QueryParameter;
import org.kohsuke.stapler.StaplerRequest;

import javax.servlet.ServletException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;

/**
 * app上传操作
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/1/16 15:02
 */
public class AppPublisher extends Recorder {
    /**
     * 平台名称
     */
    private final String buildType;
    private final boolean isUploadAppFile;
    private final boolean isUploadMultiLanguage;
    public final String exIncludesPattern;
    public final String includesStringPattern;
    public String uploadServiceIp;
    public String downloadServiceIp;
    public HashMap<String, IParser> parsers = new HashMap<>();
    public String appPath;
    public String jenkinsJob;
    private String paramBuildType;
    /**
     * APP安装包渠道名称
     */
    private String flavorName;
    private boolean paramUploadMultiLanguage;
    private boolean paramUploadAppFile;

    @DataBoundConstructor
    public AppPublisher(boolean isUploadMultiLanguage, boolean isUploadAppFile, String buildType, String appPath,
                        String exIncludesPattern, String includesStringPattern) {
        this.isUploadAppFile = isUploadAppFile;
        this.exIncludesPattern = exIncludesPattern.trim();
        this.appPath = appPath.trim();
        this.buildType = buildType.trim();
        this.isUploadMultiLanguage = isUploadMultiLanguage;
        this.includesStringPattern = includesStringPattern.trim();
        parsers.put("apk", new ApkParser());
        parsers.put("ipa", new IpaParser());
    }

    @DataBoundSetter
    public void setUploadServiceIp(String uploadServiceIp) {
        this.uploadServiceIp = uploadServiceIp;
    }

    @DataBoundSetter
    public void setDownloadServiceIp(String downloadServiceIp) {
        this.downloadServiceIp = downloadServiceIp;
    }

    public String getFlavorName() {
        return flavorName;
    }

    public boolean isUploadAppFile() {
        return isUploadAppFile || paramUploadAppFile;
    }

    public String getExIncludesPattern() {
        return exIncludesPattern;
    }

    public String getBuildType() {
        return buildType;
    }

    public String checkBuildType() {
        if (StringUtils.isEmpty(buildType)) {
            return paramBuildType;
        }
        return buildType;
    }

    public boolean isUploadMultiLanguage() {
        return isUploadMultiLanguage || paramUploadMultiLanguage;
    }

    public String getIncludesStringPattern() {
        return includesStringPattern;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws InterruptedException, IOException {
        DescriptorImpl descriptor = getDescriptor();
        ParametersAction action = build.getAction(ParametersAction.class);
        if (action != null) {
            List<ParameterValue> parameterValues = action.getAllParameters();
            if (parameterValues != null && !parameterValues.isEmpty()) {
                for (ParameterValue parameterValue : parameterValues) {
                    final String name = parameterValue.getName();
                    final Object value = parameterValue.getValue();
                    if ("build_type".equalsIgnoreCase(name)
                            || "buildType".equalsIgnoreCase(name)
                            || "BuildConfiguration".equalsIgnoreCase(name)) {
                        //参数控制编译类型
                        paramBuildType = Utils.toString(value, buildType);
                        if (!StringUtils.isEmpty(paramBuildType)) {
                            paramBuildType = paramBuildType.trim();
                        }
                    } else if ("upload_multi_language".equalsIgnoreCase(name)
                            || "uploadMultiLanguage".equalsIgnoreCase(name)) {
                        //参数控制是否上传多语言
                        paramUploadMultiLanguage = Utils.toBoolean(value, isUploadMultiLanguage);
                    } else if ("upload_app_file".equalsIgnoreCase(name)
                            || "uploadAppFile".equalsIgnoreCase(name)) {
                        //参数空是否上传app安装文件
                        paramUploadAppFile = Utils.toBoolean(value, isUploadAppFile);
                    } else if ("Channel".equalsIgnoreCase(name)) {
                        flavorName = Utils.toString(value, "Google");
                    }
                }
            }
        }
        listener.getLogger().println("Plugin>>>buildType:" + buildType);
        listener.getLogger().println("Plugin>>>paramBuildType:" + paramBuildType);
        listener.getLogger().println("Plugin>>>paramUploadAppFile:" + paramUploadAppFile);
        listener.getLogger().println("Plugin>>>paramUploadMultiLanguage:" + paramUploadMultiLanguage);
        listener.getLogger().println("Plugin>>>flavorName:" + flavorName);
        uploadServiceIp = descriptor.uploadServiceIp;
        downloadServiceIp = descriptor.downloadServiceIp;
        jenkinsJob = descriptor.jenkinsJob;
        if (StringUtils.isNotEmpty(jenkinsJob)) {
            if (!jenkinsJob.startsWith("/")) {
                jenkinsJob = "/" + jenkinsJob;
            }
            if (!jenkinsJob.endsWith("/")) {
                jenkinsJob = jenkinsJob + "/";
            }
        }
        try {
            return UploadHelper.upload(this, build, new PluginLogPrintImpl(listener));
        } catch (PluginException e) {
            e.printStackTrace();
            listener.getLogger().println("Plugin>>" + Utils.getStackTraceMessage(e));
            return false;
        }
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Override
    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Symbol("greet")
    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Publisher> {
        private String uploadServiceIp;
        private String downloadServiceIp;
        private String jenkinsJob;

        public DescriptorImpl() {
            load();
        }

        public FormValidation doCheckUploadServiceIp(@QueryParameter String value, @QueryParameter boolean useFrench) throws IOException, ServletException {
            if (value.length() == 0) return FormValidation.error("error");
            if (!Utils.isIpAddressPort(value)) {
                return FormValidation.error("输入格式不正确，正确格式应该如：10.8.31.5:8080");
            }
            return FormValidation.ok();
        }

        public FormValidation doCheckDownloadServiceIp(@QueryParameter String value,
                                                       @QueryParameter boolean useFrench) throws IOException,
                ServletException {
            if (value.length() == 0) return FormValidation.error("error");
            if (!Utils.isIpAddressPort(value)) {
                return FormValidation.error("输入格式不正确，正确格式应该如：10.8.31.5:8080");
            }
            return FormValidation.ok();
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
            req.bindParameters(this);
            uploadServiceIp = json.getString("uploadServiceIp");
            downloadServiceIp = json.getString("downloadServiceIp");
            jenkinsJob = json.getString("jenkinsJob");
            save();
            return super.configure(req, json);
        }

        public String getUploadServiceIp() {
            return uploadServiceIp;
        }

        public void setUploadServiceIp(String uploadServiceIp) {
            this.uploadServiceIp = uploadServiceIp;
        }

        public String getDownloadServiceIp() {
            return downloadServiceIp;
        }

        public void setDownloadServiceIp(String downloadServiceIp) {
            this.downloadServiceIp = downloadServiceIp;
        }

        public String getJenkinsJob() {
            return jenkinsJob;
        }

        public void setJenkinsJob(String jenkinsJob) {
            this.jenkinsJob = jenkinsJob;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return io.jenkins.plugins.sample.Messages.AppPublisher_DescriptorImpl_DisplayName();
        }

//        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Item context,
//                                                     @QueryParameter String plistRemote,
//                                                     @QueryParameter String credentialsId) {
//            if (context == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER) ||
//                    context != null && !context.hasPermission(Item.EXTENDED_READ)) {
//                return new StandardListBoxModel().includeCurrentValue(credentialsId);
//            }
//            return new StandardListBoxModel()
//                    .includeEmptyValue()
//                    .includeMatchingAs(
//                            context instanceof Queue.Task ? Tasks.getAuthenticationOf((Queue.Task) context) : ACL
//                            .SYSTEM,
//                            context,
//                            StandardUsernameCredentials.class,
//                            URIRequirementBuilder.fromUri(plistRemote).build(),
//                            GitClient.CREDENTIALS_MATCHER)
//                    .includeCurrentValue(credentialsId);
//        }
//
//        public FormValidation doCheckCredentialsId(@AncestorInPath Item context,
//                                                   @QueryParameter String plistRemote,
//                                                   @QueryParameter String value) {
//            if (context == null && !Jenkins.get().hasPermission(Jenkins.ADMINISTER) ||
//                    context != null && !context.hasPermission(Item.EXTENDED_READ)) {
//                return FormValidation.ok();
//            }
//            value = Util.fixEmptyAndTrim(value);
//            if (value == null) {
//                return FormValidation.ok();
//            }
//
//            plistRemote = Util.fixEmptyAndTrim(plistRemote);
//            if (plistRemote == null) {
//
//
//                // not set, can't check
//                return FormValidation.ok();
//            }
//            for (ListBoxModel.Option o : CredentialsProvider.listCredentials(
//                    StandardUsernameCredentials.class,
//                    context,
//                    context instanceof Queue.Task
//                            ? Tasks.getAuthenticationOf((Queue.Task) context)
//                            : ACL.SYSTEM,
//                    URIRequirementBuilder.fromUri(plistRemote).build(),
//                    GitClient.CREDENTIALS_MATCHER)) {
//                if (StringUtils.equals(value, o.value)) {
//                    // TODO check if this type of credential is acceptable to the Git client or does it merit warning
//                    // NOTE: we would need to actually lookup the credential to do the check, which may require
//                    // fetching the actual credential instance from a remote credentials store. Perhaps this is
//                    // not required
//                    return FormValidation.ok();
//                }
//            }
//            // no credentials available, can't check
//            return FormValidation.warning("Cannot find any credentials with id " + value);
//        }
    }
}
