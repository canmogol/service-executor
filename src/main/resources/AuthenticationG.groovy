import com.fererlab.event.Event
import com.fererlab.event.Request
import com.fererlab.service.Service

public class AuthenticationG implements Service {

    @Override
    Object handle(Event event) {
        println("Groovy object: " + this)
        println("Groovy Event: " + event)
        println("username: " + event.getBody().get("username"))
        Map<String, Object> response = new HashMap<String, Object>()
        response.put("logged", true)
        response.put("groups", Arrays.asList("admin", "user"))
        return response;
    }

    Object sayHi(Request request) {
        println("Groovy object: " + this)
        println("Groovy Request: " + request)
        Map<String, Object> response = new HashMap<String, Object>()
        response.put("say", "Hi " + request.params["name"])
        return response;
    }

}
