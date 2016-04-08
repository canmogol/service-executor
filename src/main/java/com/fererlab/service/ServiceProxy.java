package com.fererlab.service;

import com.fererlab.Main;

import java.util.Map;

public class ServiceProxy implements Service {

    @Override
    public Object handle(Map<String, Object> event) {
        return Main.service.handle(event);
    }

}
