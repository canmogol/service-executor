package com.fererlab.service;

import java.util.HashSet;
import java.util.Set;

public class ServiceListenerFactory {

    public static Set<ServiceListener> defaultListeners(Service service) {
        Set<ServiceListener> defaultListeners = new HashSet<ServiceListener>();
        defaultListeners.add(new LoggingServiceListener(service));
        return defaultListeners;
    }

}
