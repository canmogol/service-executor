package com.fererlab.event;

import java.util.Map;
import java.util.TreeMap;

public class Request {

    private String authType;
    private String contextPath;
    private Cookie[] cookies = new Cookie[0];
    private String method;
    private String queryString;
    private Map<String, Object> params;
    private Map<String, String> headers;
    private Map<String, Object> body;
    private String pathInfo;
    private String pathTranslated;
    private String remoteUser;
    private String requestedURI;
    private StringBuffer requestedURL;

    private Request() {
    }

    public static class Builder {

        private String authType;
        private String contextPath;
        private Cookie[] cookies;
        private String method;
        private String queryString;
        private Map<String, Object> params = new TreeMap<>();
        private Map<String, String> headers = new TreeMap<>();
        private Map<String, Object> body = new TreeMap<>();
        private String pathInfo;
        private String pathTranslated;
        private String remoteUser;
        private String requestedURI;
        private StringBuffer requestedURL;

        public Builder authType(String authType) {
            this.authType = authType;
            return this;
        }

        public Builder contextPath(String contextPath) {
            this.contextPath = contextPath;
            return this;
        }

        public Builder cookies(Cookie[] cookies) {
            this.cookies = cookies;
            return this;
        }

        public Builder method(String method) {
            this.method = method;
            return this;
        }

        public Builder queryString(String queryString) {
            this.queryString = queryString;
            return this;
        }

        public Builder params(Map<String, Object> params) {
            this.params = params;
            return this;
        }

        public Builder headers(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public Builder body(Map<String, Object> body) {
            this.body = body;
            return this;
        }

        public Builder pathInfo(String pathInfo) {
            this.pathInfo = pathInfo;
            return this;
        }

        public Builder pathTranslated(String pathTranslated) {
            this.pathTranslated = pathTranslated;
            return this;
        }

        public Builder remoteUser(String remoteUser) {
            this.remoteUser = remoteUser;
            return this;
        }

        public Builder requestedURI(String requestedURI) {
            this.requestedURI = requestedURI;
            return this;
        }

        public Builder requestedURL(StringBuffer requestedURL) {
            this.requestedURL = requestedURL;
            return this;
        }

        public Request build() {
            Request request = new Request();

            request.authType = this.authType;
            request.contextPath = this.contextPath;
            request.cookies = this.cookies;
            request.method = this.method;
            request.queryString = this.queryString;
            request.params = this.params;
            request.headers = this.headers;
            request.body = this.body;
            request.pathInfo = this.pathInfo;
            request.pathTranslated = this.pathTranslated;
            request.remoteUser = this.remoteUser;
            request.requestedURI = this.requestedURI;
            request.requestedURL = this.requestedURL;

            return request;
        }
    }

    public String getAuthType() {
        return authType;
    }

    public String getContextPath() {
        return contextPath;
    }

    public Cookie[] getCookies() {
        return cookies;
    }

    public String getMethod() {
        return method;
    }

    public String getQueryString() {
        return queryString;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, Object> getBody() {
        return body;
    }

    public String getPathInfo() {
        return pathInfo;
    }

    public String getPathTranslated() {
        return pathTranslated;
    }

    public String getRemoteUser() {
        return remoteUser;
    }

    public String getRequestedURI() {
        return requestedURI;
    }

    public StringBuffer getRequestedURL() {
        return requestedURL;
    }
}
