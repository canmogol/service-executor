package com.fererlab.service;

public interface Reloader {

    void addServiceReloadListener(ServiceReloadListener listener);

    void reload() throws Exception;

}
