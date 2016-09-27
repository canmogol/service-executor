package com.fererlab.service;

import com.fererlab.event.Event;
import com.fererlab.event.Request;
import com.fererlab.event.Status;
import com.fererlab.log.FLogger;
import com.owlike.genson.Genson;
import com.owlike.genson.GensonBuilder;
import org.python.google.common.io.CharStreams;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import java.lang.reflect.Method;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;


@Path("/api")
@Produces({"application/json"})
@Consumes({"*/*"})
public class ServiceProxy implements Service, ServiceReloadListener {

    private static final Logger log = FLogger.getLogger(ServiceProxy.class.getSimpleName());

    private Service service;

    public ServiceProxy(Service service) {
        this.service = service;
    }

    @Override
    public void serviceChanged(Service service) {
        this.service = service;
    }

    @GET
    @POST
    @PUT
    @DELETE
    @HEAD
    @OPTIONS
    @Path("/handle/")
    public Object handle(Event event) {
        try {
            return service.handle(event);
        } catch (Exception e) {
            String error = "got exception while handling event: " + event + " for service: " + service;
            log.log(Level.SEVERE, error, e);
            return Response.serverError();
        }
    }


    @GET
    @POST
    @PUT
    @DELETE
    @HEAD
    @OPTIONS
    @Path("/{operation}/")
    @SuppressWarnings("unchecked")
    public Object execute(@PathParam("operation") String operation, @Context HttpServletRequest httpRequest) {
        try {
            // HEADERS
            Map<String, String> headers = new TreeMap<>();
            if (httpRequest.getHeaderNames() != null) {
                Enumeration<String> enumeration = httpRequest.getHeaderNames();
                while (enumeration.hasMoreElements()) {
                    String headerName = enumeration.nextElement();
                    headers.put(headerName, httpRequest.getHeader(headerName));
                }
            }

            // BODY
            Map<String, Object> body = new TreeMap<>();
            try {
                String requestContent = CharStreams.toString(httpRequest.getReader());
                Genson genson = new GensonBuilder().useClassMetadata(true).useRuntimeType(true).create();
                body = genson.deserialize(requestContent, Map.class);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

            // COOKIES
            List<com.fererlab.event.Cookie> cookies = new ArrayList<>();
            if (httpRequest.getCookies() != null) {
                for (Cookie cookie : httpRequest.getCookies()) {
                    com.fererlab.event.Cookie c = new com.fererlab.event.Cookie(
                            cookie.getComment(),
                            cookie.getDomain(),
                            cookie.getMaxAge(),
                            cookie.getName(),
                            cookie.getPath(),
                            cookie.getSecure(),
                            cookie.getValue(),
                            cookie.getVersion()
                    );
                    cookies.add(c);
                }
            }

            // QUERY PARAMETERS
            Map<String, Object> queryParams = new TreeMap<>();
            if (httpRequest.getQueryString() != null) {
                String[] pairs = httpRequest.getQueryString().split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        queryParams.put(keyValue[0], keyValue[1]);
                    }
                }
            }

            // BUILD REQUEST
            Request request = new Request.Builder()
                    .authType(httpRequest.getAuthType())
                    .contextPath(httpRequest.getContextPath())
                    .cookies(cookies.toArray(new com.fererlab.event.Cookie[cookies.size()]))
                    .method(httpRequest.getMethod())
                    .queryString(httpRequest.getQueryString())
                    .params(queryParams)
                    .headers(headers)
                    .body(body)
                    .pathInfo(httpRequest.getPathInfo())
                    .pathTranslated(httpRequest.getPathTranslated())
                    .remoteUser(httpRequest.getRemoteUser())
                    .requestedURI(httpRequest.getRequestURI())
                    .requestedURL(httpRequest.getRequestURL().toString())
                    .build();

            // FIND THE METHOD
            Method operationMethod = null;
            for (Method method : service.getClass().getDeclaredMethods()) {
                if (method.getName().equals(operation)) {
                    operationMethod = method;
                    break;
                }
            }

            // IF METHOD EXISTS CALL METHOD ON SERVICE
            if (operationMethod != null) {
                Object response = operationMethod.invoke(service, request);
                if (response == null) {
                    Genson genson = new GensonBuilder().useClassMetadata(true).useRuntimeType(true).create();
                    response = genson.serialize(new com.fererlab.event.Response());
                }
                return Response.ok(response).build();
            }

            // OTHERWISE CHECK IF THIS IS A SCRIPTING SERVICE, CALL OPERATION AS METHOD
            else if (service instanceof ScriptingService) {
                ScriptingService scriptingService = (ScriptingService) service;
                Object response = scriptingService.execute(operation, request);
                return Response.ok(response).build();
            }

            // COULD NOT FIND METHOD AND IT IS NOT A SCRIPTING SERVICE
            else {
                throw new Exception("method with name: " + operation + " not found in service: " + service);
            }

        } catch (Exception e) {
            String error = "got exception while executing operation: " + operation + " on service: " + service + " exception: " + e.toString();
            log.log(Level.SEVERE, error, e);
            Genson genson = new GensonBuilder().useClassMetadata(true).useRuntimeType(true).create();
            String response = genson.serialize(new com.fererlab.event.Response(Status.ERROR.getMessage(), error));
            return Response.serverError().entity(response).build();
        }
    }


}
