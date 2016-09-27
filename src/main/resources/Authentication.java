import com.fererlab.event.Event;
import com.fererlab.event.Request;
import com.fererlab.service.Service;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class Authentication implements Service {

    @Override
    public Object handle(Event event) {
        System.out.println("Java under resources object: " + this);
        System.out.println("Java under resources Event: " + event);
        System.out.println("under resources username: " + event.getBody().get("username"));
        Map<String, Object> response = new HashMap<>();
        response.put("under resources", true);
        response.put("logged", true);
        response.put("groups", Arrays.asList("admin", "user"));
        return response;
    }

    public Object sayHi(Request request) {
        System.out.println("Java object: " + this);
        System.out.println("Java Request: " + request);
        Map<String, Object> response = new HashMap<>();
        response.put("say", "Hi " + request.getParams().get("name"));
        return response;
    }

}
