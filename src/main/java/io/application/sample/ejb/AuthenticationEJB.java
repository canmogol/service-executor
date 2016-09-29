package io.application.sample.ejb;


import com.fererlab.event.Event;
import com.fererlab.service.Service;

import javax.inject.Inject;
import java.util.Map;
import java.util.TreeMap;

public class AuthenticationEJB implements Service {

    @Inject
    private AuthenticationEJBService authenticationEJBService;

    @Override
    public Object handle(Event event) {
        System.out.println("Java EJB Exampleobject: " + this);
        System.out.println("Java EJB ExampleEvent: " + event);
        System.out.println("Java EJB Exampleusername: " + event.getBody().get("username"));

        boolean logged = authenticationEJBService.authenticate(
                String.valueOf(event.getBody().get("username")),
                String.valueOf(event.getBody().get("password"))
        );
        Map<String, Object> response = new TreeMap<>();
        response.put("Java EJB Example", true);
        response.put("logged", logged);
        return response;
    }

}
