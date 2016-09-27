package com.fererlab.reload;

import com.fererlab.content.ContentLoader;
import com.fererlab.content.ContentLoaderFactory;
import com.fererlab.interpreter.PythonInter;
import com.fererlab.log.FLogger;
import com.fererlab.service.Reloader;
import com.fererlab.service.Service;
import com.fererlab.service.ServiceReloadListener;
import com.fererlab.util.Maybe;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

public class PythonReloader implements Reloader {

    private static final Logger log = FLogger.getLogger(PythonReloader.class.getSimpleName());
    private final String requestServiceName;
    private URI scriptURI;
    private Set<ServiceReloadListener> listeners = new HashSet<>();

    public PythonReloader(URI scriptURI) {
        this.scriptURI = scriptURI;
        this.requestServiceName = getServiceName(scriptURI);
    }

    @Override
    public void addServiceReloadListener(ServiceReloadListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void reload() throws Exception {
        ContentLoader contentLoader = ContentLoaderFactory.createLoader(scriptURI.getScheme());
        Maybe<String> content = contentLoader.load(scriptURI);
        if (content.isPresent()) {
            PythonInterpreter pythonInterpreter = PythonInter.getInstance().getPythonInterpreter();
            pythonInterpreter.exec(content.get());
            PyObject pyServiceObject = pythonInterpreter.get(requestServiceName);
            PyObject serviceObject = pyServiceObject.__call__();
            Service service = (Service) serviceObject.__tojava__(Service.class);
            listeners.forEach(listener -> listener.serviceChanged(service));
        } else {
            log.severe("could not get content from URI: " + scriptURI);
        }
    }

    private String getServiceName(URI scriptURI) {
        String serviceName = null;
        String path = scriptURI.getPath();
        if (path.lastIndexOf("/") != -1) {
            String fileName = path.substring(path.lastIndexOf("/") + 1);
            if (fileName.lastIndexOf(".") != -1) {
                serviceName = fileName.substring(0, fileName.lastIndexOf("."));
            }
        }
        return serviceName;
    }
}
