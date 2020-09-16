package io.jenkins.plugins.module;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class MultiLanguageBean {
    private String name;
    private String value;
    private String projectName;
    private String bundleId;
    private String versionName;
    private String versionCode;
    private String languageCode;
    private String language;
    private String countryCode;
    private String platform;
}
