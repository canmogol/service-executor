package com.fererlab.service;

import com.fererlab.event.Event;

public interface EventLifecycle {

    void notify(Event event);

    void handle(Event event);

    void send(Event event);

}
