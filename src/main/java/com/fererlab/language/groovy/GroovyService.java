package com.fererlab.language.groovy;

import com.fererlab.content.ContentLoader;
import com.fererlab.content.ContentLoaderFactory;
import com.fererlab.event.Event;
import com.fererlab.event.Request;
import com.fererlab.log.FLogger;
import com.fererlab.service.ScriptingService;
import com.fererlab.service.Service;
import com.fererlab.util.Maybe;
import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class GroovyService implements Service, ScriptingService {

    private static final Logger log = FLogger.getLogger(GroovyService.class.getSimpleName());

    private final URI scriptURI;
    private Service service;

    public GroovyService(URI scriptURI) {
        this.scriptURI = scriptURI;
    }

    @Override
    public Object handle(Event event) {
        return getService().handle(event);
    }

    @Override
    public Object execute(String methodName, Request request) {
        Object result = null;
        Service service = getService();
        Method callMethod = null;
        for (Method method : service.getClass().getMethods()) {
            if (method.getName().equals(methodName)) {
                callMethod = method;
                break;
            }
        }
        if (callMethod != null) {
            try {
                result = callMethod.invoke(service, request);
            } catch (Exception e) {
                String error = "got exception while executing method: " + callMethod.getName() + " on service: " + service;
                log.log(Level.SEVERE, error, e);
            }
        } else {
            throw new RuntimeException("could not find method: " + methodName + " in service: " + service);
        }
        return result;
    }

    private Service getService() {
        if (service == null) {
            ContentLoader contentLoader = ContentLoaderFactory.createLoader(scriptURI.getScheme());
            Maybe<String> content = contentLoader.load(scriptURI);
            if (content.isPresent()) {
                GroovyClassLoader classLoader = new GroovyClassLoader();
                Class serviceClass = classLoader.parseClass(content.get());
                GroovyObject groovyObj = null;
                try {
                    groovyObj = (GroovyObject) serviceClass.newInstance();
                } catch (InstantiationException | IllegalAccessException e) {
                    e.printStackTrace();
                }
                service = (Service) groovyObj;
            } else {
                log.severe("could not get content from URI: " + scriptURI);
            }
        }
        return service;
    }

}
