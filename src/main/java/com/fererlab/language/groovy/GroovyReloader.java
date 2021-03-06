package com.fererlab.language.groovy;

import com.fererlab.log.FLogger;
import com.fererlab.service.Reloader;
import com.fererlab.service.Service;
import com.fererlab.service.ServiceReloadListener;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class GroovyReloader implements Reloader {

    private static final Logger log = FLogger.getLogger(GroovyReloader.class.getSimpleName());
    private URI scriptURI;
    private Set<ServiceReloadListener> listeners = new HashSet<>();

    public GroovyReloader(URI scriptURI) {
        this.scriptURI = scriptURI;
    }

    @Override
    public void addServiceReloadListener(ServiceReloadListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void reload() throws Exception {
        Service service = new GroovyService(scriptURI);
        listeners.forEach(listener -> listener.serviceChanged(service));
    }

}
