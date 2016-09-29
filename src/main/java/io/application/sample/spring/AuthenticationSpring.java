package io.application.sample.spring;

import com.fererlab.event.Event;
import com.fererlab.service.Service;

import java.util.Map;
import java.util.TreeMap;

public class AuthenticationSpring implements Service {

    private AuthenticationSpringService authenticationSpringService;

    @Override
    public Object handle(Event event) {
        System.out.println("Java Spring Example object: " + this);
        System.out.println("Java Spring Example Event: " + event);
        System.out.println("Java Spring Example username: " + event.getBody().get("username"));

        boolean logged = authenticationSpringService.authenticate(
                String.valueOf(event.getBody().get("username")),
                String.valueOf(event.getBody().get("password"))
        );
        Map<String, Object> response = new TreeMap<>();
        response.put("Java Spring Example", true);
        response.put("logged", logged);
        return response;
    }

    public void setAuthenticationSpringService(AuthenticationSpringService authenticationSpringService) {
        this.authenticationSpringService = authenticationSpringService;
    }

}
