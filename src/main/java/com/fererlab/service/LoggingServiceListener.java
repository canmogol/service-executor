package com.fererlab.service;

import com.fererlab.event.Event;
import com.fererlab.log.FLogger;

import java.util.logging.Logger;

public class LoggingServiceListener implements ServiceListener {

    private static final Logger log = FLogger.getLogger(LoggingServiceListener.class.getSimpleName());
    private final Service service;

    public LoggingServiceListener(Service service) {
        this.service = service;
    }

    @Override
    public void onCreate() {
        log.info("Service " + service.getClass().getSimpleName() + " created");
    }

    @Override
    public void onDeploy() {
        log.info("Service " + service.getClass().getSimpleName() + " deployed");
    }

    @Override
    public void onReload() {
        log.info("Service " + service.getClass().getSimpleName() + " reloaded");
    }

    @Override
    public void onDestroy() {
        log.info("Service " + service.getClass().getSimpleName() + " destroyed");
    }

    @Override
    public void onHandle(Event event) {
        log.info("Service " + service.getClass().getSimpleName() + " will handle Event: " + event);
    }

}
