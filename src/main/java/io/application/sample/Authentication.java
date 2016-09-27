package io.application.sample;

import com.fererlab.event.Event;
import com.fererlab.event.Request;
import com.fererlab.service.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class Authentication implements Service {

    @Override
    public Object handle(Event event) {
        System.out.println("Java sample application object: " + this);
        System.out.println("Java sample application Event: " + event);
        System.out.println("Java sample application username: " + event.getBody().get("username"));
        Map<String, Object> response = new TreeMap<>();
        response.put("sample application", true);
        response.put("logged", true);
        response.put("groups", Arrays.asList("admin", "user"));
        return response;
    }

    public Object sayHi(Request request) {
        System.out.println("Java sample application object: " + this);
        System.out.println("Java sample application Request: " + request);
        Map<String, Object> response = new HashMap<>();
        response.put("sample application say", "Hi " + request.getParams().get("name"));
        return response;
    }

}
