package io.jenkins.plugins.module;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Setter
@Getter
@ToString
public class LanguageBean {
    private String language;
    private String shortCode;
    private String countryCode;

    public String getLanguage() {
        return language;
    }

    public LanguageBean() {
    }

    public LanguageBean(String shortCode) {
        this.shortCode = shortCode;
    }

    public LanguageBean(String language, String shortCode) {
        this.language = language;
        this.shortCode = shortCode;
    }

    public LanguageBean(String language, String shortCode, String countryCode) {
        this.language = language;
        this.shortCode = shortCode;
        this.countryCode = countryCode;
    }
}
