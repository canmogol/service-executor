package com.fererlab.language.java;

import com.fererlab.event.Event;
import com.fererlab.log.FLogger;
import com.fererlab.service.Service;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaService implements Service {

    private static final Logger log = FLogger.getLogger(JavaService.class.getSimpleName());

    private final URI uri;
    private Service service;

    public JavaService(URI uri) {
        this.uri = uri;
    }

    @Override
    public Object handle(Event event) {
        Service service = getService();
        return service.handle(event);
    }

    public Service getService() {
        if (service == null) {
            try {
                Class<?> serviceClass = this.getClass().getClassLoader().loadClass(uri.getHost());
                service = (Service) serviceClass.newInstance();
            } catch (Exception e) {
                String error = "got exception while creating an instance of service from uri: " + uri + " exception: " + e;
                log.log(Level.SEVERE, error, e);
            }
        }
        return service;
    }
}
