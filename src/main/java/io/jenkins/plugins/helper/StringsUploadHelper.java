//package io.jenkins.plugins.helper;
//
//import hudson.FilePath;
//import io.jenkins.plugins.ILogPrintListener;
//import io.jenkins.plugins.module.AppBean;
//import io.jenkins.plugins.module.LanguageBean;
//import io.jenkins.plugins.module.MultiLanguageBean;
//import io.jenkins.plugins.sample.Configs;
//import io.jenkins.plugins.sample.FileFinder;
//import io.jenkins.plugins.sample.ServiceClient;
//import io.jenkins.plugins.util.ParseUtil;
//import io.jenkins.plugins.util.Utils;
//import org.apache.commons.lang.StringUtils;
//import org.dom4j.Document;
//import org.dom4j.Element;
//import org.dom4j.io.SAXReader;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * 上传Android 多语言
// *
// * @author dingpeihua
// * @version 1.0
// * @date 2020/1/16 16:13
// */
//public class StringsUploadHelper {
//    ServiceClient client;
//    FilePath rootDir;
//    ILogPrintListener listener;
//    AppBean appBean;
//    String includesPattern;
//
//    public StringsUploadHelper(ServiceClient client, AppBean appBean, String includesPattern,
//                               FilePath rootDir, ILogPrintListener listener) {
//        this.client = client;
//        this.rootDir = rootDir;
//        this.appBean = appBean;
//        this.listener = listener;
//        this.includesPattern = includesPattern;
//    }
//
//    /**
//     * 上传多语言
//     *
//     * @author dingpeihua
//     * @date 2019/7/18 10:58
//     * @version 1.0
//     */
//    public void uploadMultiLanguage() {
//        if (appBean == null) {
//            return;
//        }
//        try {
//            if (StringUtils.isEmpty(includesPattern)) {
//                includesPattern = Configs.PROJECT_APP_FOLDER;
//            }
//            listener.println(rootDir.getRemote());
//            String mainFolderPattern = includesPattern + Configs.STRINGS_MAIN_PATH_INCLUDE;
//            String buildFolderPattern = includesPattern + Configs.STRINGS_BUILD_PATH_INCLUDE;
//            FileFinder mainFolderFinder = new FileFinder(mainFolderPattern, "");
//            FileFinder buildFolderFinder = new FileFinder(buildFolderPattern, "");
//            List<String> mainFolderFileNames = rootDir.act(mainFolderFinder);
//            List<String> buildFolderFileNames = rootDir.act(buildFolderFinder);
//            listener.println(io.jenkins.plugins.sample.Messages.AppPublisher_foundFiles(mainFolderFileNames));
//            listener.println(io.jenkins.plugins.sample.Messages.AppPublisher_foundFiles(buildFolderFileNames));
//            for (String buildFolderFileName : buildFolderFileNames) {
//                final FilePath buildRemoteFile = rootDir.child(buildFolderFileName);
//                final String buildFileName = buildRemoteFile.getName();
//                for (String fileName : mainFolderFileNames) {
//                    final FilePath remoteFile = rootDir.child(fileName);
//                    final FilePath parentFile = remoteFile.getParent();
//                    final String parentFileName = parentFile.getName();
//                    if (buildFileName.equalsIgnoreCase(parentFileName + ".xml")) {
//                        String languageCode = splitLanguage(parentFileName);
//                        createMultiLanguage(new LanguageBean(languageCode), appBean.getName(), appBean.getVersionName(),
//                                appBean.getVersionCode(), appBean.getBundleId(), buildRemoteFile);
//                        break;
//                    }
//                }
//            }
//        } catch (Exception e) {
//            listener.println("multiLanguage>>>" + Utils.getStackTraceMessage(e));
//        }
//    }
//
//    private void createMultiLanguage(LanguageBean languageBean, String projectName, String versionName, String versionCode, String bundleId, FilePath remoteFile) {
//        List<MultiLanguageBean> multiLanguageBeans = new ArrayList<>();
//        try {
//            SAXReader reader = new SAXReader();
//            reader.setEncoding("UTF-8");
//            Document document = reader.read(remoteFile.getRemote());
//            Element elementRoot = document.getRootElement();
//            List<Element> elements = elementRoot.elements("string");
//            if (elements != null && elements.size() > 0) {
//                listener.println("multiLanguage>>>" + remoteFile.getName());
//                for (Element element : elements) {
//                    final String translatable = element.attributeValue("translatable");
//                    if (translatable == null || ParseUtil.toBoolean(translatable)) {
//                        final String name = element.attributeValue("name");
//                        final String text = Utils.removeDoubleQuotes(element.getStringValue());
//                        if (!StringUtils.isEmpty(text)) {
//                            final MultiLanguageBean bean = new MultiLanguageBean();
//                            bean.setBundleId(bundleId);
//                            bean.setVersionName(versionName);
//                            bean.setVersionCode(versionCode);
//                            bean.setCountryCode(languageBean.getCountryCode());
//                            bean.setLanguage(languageBean.getLanguage());
//                            bean.setLanguageCode(languageBean.getShortCode());
//                            bean.setProjectName(projectName);
//                            bean.setName(name);
//                            bean.setValue(text);
//                            bean.setPlatform(appBean.getPlatform());
//                            multiLanguageBeans.add(bean);
//                        }
//                    }
//                }
//            }
//            if (multiLanguageBeans.size() > 0) {
//                client.uploadMultiLanguage(languageBean.getShortCode(), multiLanguageBeans);
//            }
//        } catch (Exception e) {
//            listener.println("multiLanguage>>" + Utils.getStackTraceMessage(e));
//        }
//    }
//
//    String splitLanguage(String valuesName) {
//        if (valuesName == null || !valuesName.contains("-")) {
//            return "en";
//        }
//        int index = valuesName.indexOf("-");
//        if (index + 1 >= valuesName.length()) {
//            return "en";
//        }
//        return valuesName.substring(index + 1);
//    }
//}
