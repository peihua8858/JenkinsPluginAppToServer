package io.jenkins.plugins.apk;

import io.jenkins.plugins.ILogPrintListener;
import io.jenkins.plugins.IParser;
import io.jenkins.plugins.exception.PackageParseException;
import io.jenkins.plugins.module.AppBean;
import io.jenkins.plugins.util.ParseUtil;
import net.dongliu.apk.parser.ApkFile;
import net.dongliu.apk.parser.bean.AdaptiveIcon;
import net.dongliu.apk.parser.bean.ApkMeta;
import net.dongliu.apk.parser.bean.Icon;
import net.dongliu.apk.parser.bean.IconFace;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * android 解析Apk
 *
 * @author dingpeihua
 * @version 1.0
 * @date 2020/1/16 15:20
 */
public class ApkParser implements IParser {

    @Override
    public AppBean onParser(ILogPrintListener listener, File appPath) throws PackageParseException {
        ApkFile apkParser = null;
        try {
            AppBean app = new AppBean();
            String path = appPath.getPath();
            app.setPlatform("Android");
            listener.println("PluginRemote--- >" + path);
            apkParser = new ApkFile(new File(path));
            ApkMeta apkMeta = apkParser.getApkMeta();
            List<IconFace> iconFaces = apkParser.getAllIcons();
            ParseIcon parseIcon = new ParseIcon(listener, iconFaces).invoke();
            String iconPath = parseIcon.getIconPath();
            byte[] iconData = parseIcon.getIconData();
            if (iconPath != null) {
                listener.println("PluginIcon--- >" + iconPath);
            }
            listener.println("PluginVersionCode--- >" + apkMeta.getVersionCode());
            listener.println("PluginVersionName--- >" + apkMeta.getVersionName());
            app.setName(apkMeta.getName());
            app.setBundleId(apkMeta.getPackageName());
            if (iconPath != null) {
                String[] strs = iconPath.split("/");
                createFile(iconData, appPath.getParent(), strs[strs.length - 1], app);
            }
            app.setVersionCode(ParseUtil.toString(apkMeta.getVersionCode()));
            app.setVersionName(apkMeta.getVersionName());
            return app;
        } catch (Exception e) {
            throw new PackageParseException("Unfortunately, an error has occurred while processing parse android app file", e);
        } finally {
            try {
                if (apkParser != null) {
                    apkParser.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private class ParseIcon {
        private ILogPrintListener listener;
        private List<IconFace> iconFaces;
        private String iconPath;
        private byte[] iconData;

        public ParseIcon(ILogPrintListener listener, List<IconFace> iconFaces) {
            this.listener = listener;
            this.iconFaces = iconFaces;
        }

        public String getIconPath() {
            return iconPath;
        }

        public byte[] getIconData() {
            return iconData;
        }

        public ParseIcon invoke() {
            if (iconFaces != null && iconFaces.size() > 0) {
                int density = 0;
                for (IconFace iconFace : iconFaces) {
                    final String iconPathTm = iconFace.getPath();
                    if (iconPathTm.endsWith(".png") || iconPathTm.endsWith(".PNG")
                            || iconPathTm.endsWith(".jpg") || iconPathTm.endsWith(".jpeg")) {
                        listener.println("PluginIcon--- >" + iconFace.getPath());
                        if (iconFace instanceof Icon) {
                            Icon icon = (Icon) iconFace;
                            if (density < icon.getDensity()) {
                                density = icon.getDensity();
                                iconPath = icon.getPath();
                                iconData = icon.getData();
                            }
                        } else if (iconFace instanceof AdaptiveIcon) {
                            AdaptiveIcon adaptiveIcon = (AdaptiveIcon) iconFace;
                            Icon foregroundIcon = adaptiveIcon.getForeground();
                            if (density < foregroundIcon.getDensity()) {
                                density = foregroundIcon.getDensity();
                                iconPath = foregroundIcon.getPath();
                                iconData = foregroundIcon.getData();
                            }
                        }
                    }
                }
            }
            return this;
        }
    }
}
