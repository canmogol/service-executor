package io.application.sample.ejb;

import com.fererlab.event.Event;
import com.fererlab.service.Service;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

public class EJBExample implements Service {

    private final AuthenticationEJB authenticationEJB;

    public EJBExample() {
        Weld weld = new Weld();
        WeldContainer container = weld.initialize();
        authenticationEJB = container.instance().select(AuthenticationEJB.class).get();
        weld.shutdown();
    }

    @Override
    public Object handle(Event event) {
        return authenticationEJB.handle(event);
    }
}
