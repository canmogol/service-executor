package com.fererlab.event;

public class Cookie {

    private String name;
    private String value;
    private String path;
    private String domain;
    private Integer maxAge;
    private boolean secure;
    private int version = 0;
    private String comment;

    public Cookie(String comment, String domain, int maxAge, String name, String path, boolean secure, String value, int version) {
        this.comment = comment;
        this.domain = domain;
        this.maxAge = maxAge;
        this.name = name;
        this.path = path;
        this.secure = secure;
        this.value = value;
        this.version = version;
    }

    public String getName() {
        return name;
    }

    public String getValue() {
        return value;
    }

    public String getPath() {
        return path;
    }

    public String getDomain() {
        return domain;
    }

    public Integer getMaxAge() {
        return maxAge;
    }

    public boolean isSecure() {
        return secure;
    }

    public int getVersion() {
        return version;
    }

    public String getComment() {
        return comment;
    }
}
