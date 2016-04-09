package com.fererlab.service;

import com.fererlab.Main;
import com.fererlab.event.Event;

public class ServiceProxy implements Service {

    @Override
    public Object handle(Event event) {
        return Main.service.handle(event);
    }

}
