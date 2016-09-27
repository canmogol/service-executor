package com.fererlab.config;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class Configuration {

    private String webServerHostname;
    private int webServerPort;

    public String getWebServerHostname() {
        return webServerHostname;
    }

    public void setWebServerHostname(String webServerHostname) {
        this.webServerHostname = webServerHostname;
    }

    public int getWebServerPort() {
        return webServerPort;
    }

    public void setWebServerPort(int webServerPort) {
        this.webServerPort = webServerPort;
    }

    public static Configuration createDefault() {
        Configuration configuration = new Configuration();
        configuration.setWebServerPort(9876);
        configuration.setWebServerHostname("localhost");
        return configuration;
    }
}
