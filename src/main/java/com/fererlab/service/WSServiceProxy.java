package com.fererlab.service;

import com.fererlab.Main;
import com.fererlab.event.Event;

public class WSServiceProxy implements WSService {

    @Override
    public void handle(Event event) {
        Main.WSService.handle(event);
    }

}
