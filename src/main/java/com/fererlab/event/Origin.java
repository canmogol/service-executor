package com.fererlab.event;

import com.owlike.genson.Genson;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Origin {

    private String name;
    private String domain;
    private String ip;
    private Integer port;
    private String memoryMappedFile;
    private Long startedAt;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getMemoryMappedFile() {
        return memoryMappedFile;
    }

    public void setMemoryMappedFile(String memoryMappedFile) {
        this.memoryMappedFile = memoryMappedFile;
    }

    public Long getStartedAt() {
        return startedAt;
    }

    public void setStartedAt(Long startedAt) {
        this.startedAt = startedAt;
    }

    @Override
    public String toString() {
        return new Genson().serialize(this);
    }
}
