package com.fererlab;

import com.fererlab.config.Configuration;
import com.fererlab.content.ContentLoader;
import com.fererlab.content.ContentLoaderFactory;
import com.fererlab.log.FLogger;
import com.fererlab.reload.ReloaderFactory;
import com.fererlab.service.*;
import com.fererlab.util.Maybe;
import com.owlike.genson.Genson;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URI;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ServiceExecutor {

    private static final Logger log = FLogger.getLogger(ServiceExecutor.class.getSimpleName());

    private final URI scriptURI;
    private final URI configURI;

    private Set<ServiceListener> serviceListeners = new HashSet<>();
    private Service service = null;
    private Configuration configuration = null;
    private ServiceProxy serviceProxy;

    public ServiceExecutor(URI scriptURI, URI configURI) {
        this.scriptURI = scriptURI;
        this.configURI = configURI;
    }

    public ServiceExecutor(URI scriptURI, URI configURI, Set<ServiceListener> serviceListeners) {
        this.scriptURI = scriptURI;
        this.configURI = configURI;
    }

    public Configuration readConfiguration() {
        if (configURI == null) {
            try {
                configuration = new Genson().deserialize(new FileReader("config.json"), Configuration.class);
            } catch (FileNotFoundException e) {
                String error = "configuration URI is null, tried to read the builtin config.json file, got exception: '" + e.getMessage() + "' will use default values for configuration";
                log.log(Level.SEVERE, error, e);
                configuration = Configuration.createDefault();
            }
        } else {
            ContentLoader contentLoader = ContentLoaderFactory.createLoader(configURI.getScheme());
            Maybe<String> content = contentLoader.load(configURI);
            if (content.isPresent()) {
                // create service executor
                configuration = new Genson().deserialize(content.get(), Configuration.class);
            } else {
                log.severe("could not get content or could not create configuration from content, will use the default one");
                configuration = Configuration.createDefault();
            }
        }
        return configuration;
    }

    public Maybe<Service> createService() {
        ContentLoader contentLoader = ContentLoaderFactory.createLoader(scriptURI.getScheme());
        Maybe<String> content = contentLoader.load(scriptURI);
        if (content.isPresent()) {
            // create service executor
            service = ServiceFactory.create(ServiceType.find(scriptURI.getPath()));
            serviceListeners.addAll(ServiceListenerFactory.defaultListeners(service));
            serviceListeners.forEach(ServiceListener::onCreate);
        } else {
            log.severe("could not get content, will quit now");
        }
        return Maybe.create(service);
    }

    public Maybe<Reloader> createReloader() {
        if (scriptURI.getPath().lastIndexOf(".") != -1) {
            String extension = scriptURI.getPath().substring(scriptURI.getPath().lastIndexOf(".") + 1);
            Reloader reloader = ReloaderFactory.createReloader(scriptURI, extension);
            if (reloader != null) {
                reloader.addServiceReloadListener(getServiceProxy());
            } else {
                log.severe("could not create reloader for type: " + extension + " of URI: " + scriptURI);
            }
            return Maybe.create(reloader);
        } else {
            return Maybe.empty();
        }

    }

    public Service getService() {
        return service;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public ServiceProxy getServiceProxy() {
        if (serviceProxy == null) {
            serviceProxy = new ServiceProxy(service);
        }
        return serviceProxy;
    }

}
