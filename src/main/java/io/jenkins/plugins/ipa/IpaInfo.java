package io.jenkins.plugins.ipa;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.awt.image.BufferedImage;

@Getter
@Setter
@ToString
public class IpaInfo {
    private String bundleName;
    private String build;
    private String buildType;
    private String bundleDisplayName;
    private String bundleIdentifier;
    private String bundleShortVersionString;
    private BufferedImage icon;
}
