package io.application.sample.spring;

import com.fererlab.event.Event;
import com.fererlab.service.Service;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringExample implements Service {

    private final ClassPathXmlApplicationContext context;

    public SpringExample() {
        context = new ClassPathXmlApplicationContext("ApplicationContext.xml");
    }

    @Override
    public Object handle(Event event) {
        Service service = context.getBean("authenticationSpring", Service.class);
        return service.handle(event);
    }

}
