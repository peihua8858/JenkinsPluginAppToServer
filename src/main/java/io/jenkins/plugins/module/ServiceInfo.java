package io.jenkins.plugins.module;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;

@Getter
@Setter
@ToString
public class ServiceInfo {
    private Long id;
    private String ipAddress;
    private String portNumber;
    private int status;
}
