package com.fererlab.service;

import com.fererlab.event.Event;


public class ServiceProxy implements Service, ServiceReloadListener {

    private Service service;

    public ServiceProxy(Service service) {
        this.service = service;
    }

    @Override
    public void serviceChanged(Service service) {
        this.service = service;
    }

    public Object handle(Event event) {
        return service.handle(event);
    }

}
