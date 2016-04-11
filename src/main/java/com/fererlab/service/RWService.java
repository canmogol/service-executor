package com.fererlab.service;


import com.fererlab.Main;
import com.fererlab.event.Event;
import com.fererlab.event.Request;
import com.fererlab.event.Status;
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

@Path("/api")
@Produces({"application/json"})
@Consumes({"*/*"})
public class RWService {

    @GET
    @POST
    @PUT
    @DELETE
    @Path("/{operation}/")
    @SuppressWarnings("unchecked")
    public Object execute(@PathParam("operation") String operation, @Context HttpServletRequest httpRequest) {
        WSService WSService = Main.WSService;
        Method operationMethod = null;
        for (Method method : WSService.getClass().getDeclaredMethods()) {
            if (method.getName().equals(operation)) {
                operationMethod = method;
                break;
            }
        }

        try {

            Map<String, String> headers = new TreeMap<>();
            if (httpRequest.getHeaderNames() != null) {
                Enumeration<String> enumeration = httpRequest.getHeaderNames();
                while (enumeration.hasMoreElements()) {
                    String headerName = enumeration.nextElement();
                    headers.put(headerName, httpRequest.getHeader(headerName));
                }
            }

            Map<String, Object> body = new TreeMap<>();
            try {
                String requestContent = CharStreams.toString(httpRequest.getReader());
                Genson genson = new GensonBuilder().useClassMetadata(true).useRuntimeType(true).create();
                body = genson.deserialize(requestContent, Map.class);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }

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
                    .requestedURL(httpRequest.getRequestURL())
                    .build();

            if (operationMethod != null) {

                Object response = operationMethod.invoke(WSService, request);
                if (response == null) {
                    Genson genson = new GensonBuilder().useClassMetadata(true).useRuntimeType(true).create();
                    response = genson.serialize(new com.fererlab.event.Response());
                }
                return Response.ok(response).build();

            } else if (WSService instanceof ProxyWSService) {
                ProxyWSService proxyWSService = (ProxyWSService) WSService;
                Object response = proxyWSService.execute(operation, request);
                return Response.ok(response).build();

            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            Genson genson = new GensonBuilder().useClassMetadata(true).useRuntimeType(true).create();
            String response = genson.serialize(new com.fererlab.event.Response(Status.ERROR.getMessage(), e.toString()));
            return Response.serverError().entity(response).build();
        }
    }

    @POST
    @Path("/handle/")
    public Object execute(@Context HttpServletRequest httpRequest, Event event) {
        WSService WSService = Main.WSService;
        Method operationMethod = null;
        for (Method method : WSService.getClass().getDeclaredMethods()) {
            if (method.getName().equals("handle")) {
                operationMethod = method;
                break;
            }
        }
        try {
            if (operationMethod != null) {

                if (event == null) {
                    throw new Exception("Event is null, check post data");
                }

                TreeMap<String, String> headers = new TreeMap<>();
                if (httpRequest.getHeaderNames() != null) {
                    Enumeration<String> enumeration = httpRequest.getHeaderNames();
                    while (enumeration.hasMoreElements()) {
                        String headerName = enumeration.nextElement();
                        headers.put(headerName, httpRequest.getHeader(headerName));
                    }
                }

                Object response = operationMethod.invoke(WSService, event);
                if (response == null) {
                    response = "{\"status\":\"OK\"}";
                }
                return Response.ok(response).build();
            } else {
                return Response.status(Response.Status.NOT_FOUND).build();
            }
        } catch (Exception e) {
            String response = "{\"status\":\"ERROR\", \"error\":\"" + e.toString() + "\"}";
            return Response.serverError().entity(response).build();
        }
    }


}
