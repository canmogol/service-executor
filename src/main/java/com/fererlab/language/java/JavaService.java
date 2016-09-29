package com.fererlab.language.java;

import com.fererlab.event.Event;
import com.fererlab.event.Request;
import com.fererlab.log.FLogger;
import com.fererlab.service.ScriptingService;
import com.fererlab.service.Service;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaService implements Service, ScriptingService {

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

    @Override
    public Object execute(String methodName, Request request) {
        Object result = null;
        Service service = getService();
        Method callMethod = null;
        for (Method method : service.getClass().getMethods()) {
            if (method.getName().equals(methodName)) {
                try {
                    callMethod = method;
                    result = method.invoke(service, request);
                } catch (Exception e) {
                    String error = "got exception while executing method: " + method.getName() + " on service: " + service;
                    log.log(Level.SEVERE, error, e);
                }
                break;
            }
        }
        if (callMethod == null) {
            throw new RuntimeException("could not find method: " + methodName + " in service: " + service);
        }
        return result;
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
