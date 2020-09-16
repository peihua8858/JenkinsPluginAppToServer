package io.jenkins.plugins.ipa;

import com.dd.plist.NSArray;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListParser;
import com.kylinworks.IPngConverter;
import io.jenkins.plugins.ILogPrintListener;
import io.jenkins.plugins.IParser;
import io.jenkins.plugins.exception.PackageParseException;
import io.jenkins.plugins.module.AppBean;
import net.lingala.zip4j.ZipFile;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

public class IpaParser implements IParser {
    public IpaInfo parse(ILogPrintListener listener, File ipaFile) throws PackageParseException {
        try {
            String sysTemp = System.getProperty("java.io.tmpdir");
            File dir = new File(sysTemp + File.separator + "ipaparser");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String path = dir.getPath();
            ZipFile zipFile = new ZipFile(ipaFile.getAbsoluteFile());
            listener.println(path);
            zipFile.extractAll(path);
            File payloadFolder = new File(path + File.separator + "Payload");
            File appFolder = payloadFolder.listFiles()[0];
            listener.println("App Folder: " + appFolder);
            File infoPlist = new File(appFolder + File.separator + "Info.plist");
            NSDictionary rootDict = (NSDictionary) PropertyListParser.parse(infoPlist);
            File file = new File(appFolder + File.separator + "embedded.mobileprovision");
            IpaInfo ipa = new IpaInfo();

            if (file.exists()) {
                String str = inputStream2String(new FileInputStream(file));
                boolean isAdHoc = str.contains("<key>ProvisionedDevices</key>");
                if (isAdHoc) {
                    ipa.setBuildType("Adhoc");
                } else {
                    boolean isInhouse = str.contains("<key>ProvisionsAllDevices</key>");
                    if (isInhouse) {
                        ipa.setBuildType("Inhouse");
                    }
                }
            }
            ipa.setBuild(rootDict.objectForKey("CFBundleVersion").toString());
            if (rootDict.objectForKey("CFBundleDisplayName") != null) {
                ipa.setBundleName(rootDict.objectForKey("CFBundleDisplayName").toString());
                ipa.setBundleDisplayName(rootDict.objectForKey("CFBundleDisplayName").toString());
            } else if (rootDict.objectForKey("AppIDName") != null) {
                ipa.setBundleName(rootDict.objectForKey("AppIDName").toString());
                ipa.setBundleDisplayName(rootDict.objectForKey("AppIDName").toString());
            } else {
                ipa.setBundleName("Unkown Name");
                ipa.setBundleDisplayName(ipa.getBundleName());
            }
            ipa.setBundleIdentifier(rootDict.objectForKey("CFBundleIdentifier").toString());
            ipa.setBundleShortVersionString(rootDict.objectForKey("CFBundleShortVersionString").toString());
            File iconFile = null;
            if (rootDict.containsKey("CFBundleIconFiles")) {
                NSObject[] iconFiles = ((NSArray) rootDict.objectForKey("CFBundleIconFiles")).getArray();
                if (iconFiles.length > 0) {
                    listener.println("icons -------<" + appFolder + File.separator + iconFiles[0].toString());
                    iconFile = new File(appFolder + File.separator + iconFiles[iconFiles.length - 1].toString());
                }
            }
            if (rootDict.containsKey("CFBundleIcons") && iconFile == null) {
                NSDictionary cfIcons = (NSDictionary) rootDict.objectForKey("CFBundleIcons");
                if (cfIcons.objectForKey("CFBundlePrimaryIcon") != null) {
                    NSDictionary nd = (NSDictionary) cfIcons.objectForKey("CFBundlePrimaryIcon");
                    if (nd.objectForKey("CFBundleIconFiles") != null) {
                        NSObject[] nsArray = ((NSArray) nd.objectForKey("CFBundleIconFiles")).getArray();
                        if (nsArray.length > 0) {
                            iconFile = new File(appFolder + File.separator + nsArray[nsArray.length - 1].toString() + "@2x.png");
                        }
                    }
                }
            } else if (iconFile == null) {
                iconFile = new File(appFolder + File.separator + "iTunesArtwork");
            }
            try {
                listener.println("iconFile:" + (iconFile != null ? iconFile.getAbsolutePath() : "icon 文件路径不存在"));
                if (iconFile.exists()) {
                    File covertFile = new File(appFolder + File.separator + "convertIcon.png");
                    (new IPngConverter(iconFile, covertFile)).convert();
                    BufferedImage iconImage = ImageIO.read(covertFile);
                    ipa.setIcon(iconImage);
                }
            } catch (Exception e) {
                iconFile = new File(appFolder + File.separator + "iTunesArtwork");
                e.printStackTrace();
            }
            FileUtils.deleteQuietly(dir);
            return ipa;
        } catch (Exception e) {
            throw new PackageParseException("Unfortunately, an error has occurred while processing parse ios app file", e);
        }
    }

    String inputStream2String(InputStream is) {
        BufferedReader in = new BufferedReader(new InputStreamReader(is));
        StringBuffer buffer = new StringBuffer();
        String line = "";
        try {
            while ((line = in.readLine()) != null) {
                buffer.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer.toString();
    }

    @Override
    public AppBean onParser(ILogPrintListener listener, File appPath) throws PackageParseException {
        IpaParser ipaParser = new IpaParser();
        IpaInfo ipa = ipaParser.parse(listener, appPath);
        listener.println("Plugin>>ipa " + ipa);
        AppBean app = new AppBean();
        app.setBuildType(ipa.getBuildType());
        app.setVersionCode(ipa.getBuild());
        app.setName(ipa.getBundleDisplayName());
        app.setPlatform("iOS");
        app.setBundleId(ipa.getBundleIdentifier());
        app.setFilePath(appPath.getAbsolutePath());
        if (ipa.getIcon() != null) {
            BufferedImage iconImage = ipa.getIcon();
            if (iconImage != null) {
                byte[] bytes = imageToBytes(ipa.getIcon(), "png");
                createFile(bytes, appPath.getParent(), "ic_launcher.png", app);
            }
        }
        app.setVersionName(ipa.getBundleShortVersionString());
        listener.println("Plugin>>ipa " + app);
        return app;
    }

    public static byte[] imageToBytes(Image image, String format) throws PackageParseException {
        BufferedImage bImage = new BufferedImage(image.getWidth(null), image.getHeight(null), 2);
        Graphics bg = bImage.getGraphics();
        bg.drawImage(image, 0, 0, null);
        bg.dispose();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ImageIO.write(bImage, format, out);
        } catch (IOException e) {
            throw new PackageParseException("Unfortunately, an error has occurred while processing write ios icon file", e);
        }
        return out.toByteArray();
    }
}
