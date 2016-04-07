package com.fererlab.service;

import com.fererlab.Main;

import java.util.HashMap;

public class ServiceProxy implements Service {

    @Override
    public Object handle(HashMap<String, String> event) {
        return Main.service.handle(event);
    }

}
