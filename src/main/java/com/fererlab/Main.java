package com.fererlab;

import com.fererlab.event.Event;
import com.fererlab.filter.ReloadFilter;
import com.fererlab.service.JavascriptWSService;
import com.fererlab.service.RWService;
import com.fererlab.service.Reloader;
import com.fererlab.service.WSService;
import com.owlike.genson.Genson;
import io.undertow.Handlers;
import io.undertow.Undertow;
import io.undertow.server.handlers.resource.ClassPathResourceManager;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.DeploymentManager;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.websockets.WebSocketConnectionCallback;
import io.undertow.websockets.core.*;
import io.undertow.websockets.spi.WebSocketHttpExchange;
import org.jboss.resteasy.plugins.server.servlet.HttpServlet30Dispatcher;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jruby.embed.ScriptingContainer;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.DispatcherType;
import javax.servlet.ServletException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {

    /**
     * GamApp logger
     */
    private static Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * Current WSService instance
     */
    public static WSService WSService;

    /**
     * undertow web server
     */
    private Undertow server;

    /**
     * undertow web server thread
     */
    private Thread webServerThread;


    /**
     * WSService object reloader
     */
    public static Reloader reloader;

    /**
     * Python pythonInterpreter
     */
    private PythonInterpreter pythonInterpreter = null;

    /**
     * Ruby Interpreter
     */
    private ScriptingContainer rubyInterpreter = null;

    /**
     * Javascript Interpreter
     */
    private ScriptEngine javascriptInterpreter;

    public static void main(String[] args) throws Exception {
//        args = new String[]{"/mnt/sda1/IdeaProjects/canmogol/service-executor/src/main/resources/", "Authentication", "py"};
//        args = new String[]{"/mnt/sda1/IdeaProjects/canmogol/service-executor/src/main/resources/", "Authentication", "rb"};
        args = new String[]{"/mnt/sda1/IdeaProjects/canmogol/service-executor/src/main/resources/", "Authentication", "js"};
        String filePath = "file://" + args[0] + args[1] + "." + args[2];
        String content = Files.readAllLines(Paths.get(new URL(filePath).toURI()))
                .stream()
                .collect(Collectors.joining("\n"));

        Main main = new Main();
//        main.createPythonService(content, args[1], filePath);
//        main.createRubyService(content, args[1], filePath);
        main.createJavascriptService(content, args[1], filePath);
        main.startServer();
    }

    private void startServer() {
        if (webServerThread == null) {
            logger.info("starting web server");
            webServerThread = new Thread() {
                @Override
                public void run() {
                    try {
                        restartServer();
                    } catch (ServletException e) {
                        e.printStackTrace();
                    }
                }
            };
            webServerThread.start();
        } else {
            logger.info("web server already started, using existing server");
        }
    }

    private void restartServer() throws ServletException {
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.getActualResourceClasses().add(RWService.class);

        ServletInfo resteasyServlet = Servlets.servlet("ResteasyServlet", HttpServlet30Dispatcher.class)
                .setAsyncSupported(true)
                .setLoadOnStartup(1)
                .addMapping("/*");

        FilterInfo filter = Servlets.filter("ReloadFilter", ReloadFilter.class);

        DeploymentInfo di = new DeploymentInfo()
                .setContextPath("/")
                .addServletContextAttribute(ResteasyDeployment.class.getName(), deployment)
                .addServlet(resteasyServlet).setDeploymentName("")
                .setDefaultEncoding("UTF-8")
                .addFilter(filter)
                .addFilterUrlMapping("ReloadFilter", "/*", DispatcherType.REQUEST)
                .setClassLoader(ClassLoader.getSystemClassLoader());

        DeploymentManager deploymentManager = Servlets.defaultContainer().addDeployment(di);
        deploymentManager.deploy();

        server = Undertow.builder()
                .addHttpListener(9876, "localhost")
                .setHandler(Handlers.path()
                        .addPrefixPath("/event", Handlers.websocket(new WebSocketConnectionCallback() {

                            @Override
                            public void onConnect(WebSocketHttpExchange exchange, WebSocketChannel channel) {
                                channel.getReceiveSetter().set(new AbstractReceiveListener() {

                                    @Override
                                    protected void onFullTextMessage(WebSocketChannel channel, BufferedTextMessage message) {
                                        String response = "{\"status\":\"OK\"}";
                                        try {
                                            String data = message.getData();
                                            Event event = new Genson().deserialize(data, Event.class);
                                            if (event == null) {
                                                throw new Exception("Event is null, post data is : '" + data + "'");
                                            }
                                            Main.WSService.handle(event);
                                        } catch (Exception e) {
                                            response = "{\"status\":\"ERROR\", \"error\":\"" + e.toString() + "\"}";
                                        }
                                        WebSockets.sendText(response, channel, new WebSocketCallback<Void>() {
                                            @Override
                                            public void complete(WebSocketChannel channel, Void context) {
                                                System.out.println("Main.complete");
                                            }

                                            @Override
                                            public void onError(WebSocketChannel channel, Void context, Throwable throwable) {
                                                System.out.println("Main.onError");
                                            }
                                        });
                                    }

                                    @Override
                                    protected void onClose(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
                                        super.onClose(webSocketChannel, channel);
                                        System.out.println("closed webSocketChannel: " + webSocketChannel + " channel: " + channel);
                                    }

                                    @Override
                                    protected void onError(WebSocketChannel channel, Throwable error) {
                                        super.onError(channel, error);
                                        System.out.println("error channel: " + channel + " error: " + error);
                                    }

                                    @Override
                                    protected void onPing(WebSocketChannel webSocketChannel, StreamSourceFrameChannel channel) throws IOException {
                                        super.onPing(webSocketChannel, channel);
                                        System.out.println("ping webSocketChannel: " + webSocketChannel + " channel: " + channel);
                                    }

                                    @Override
                                    protected void onPong(WebSocketChannel webSocketChannel, StreamSourceFrameChannel messageChannel) throws IOException {
                                        super.onPong(webSocketChannel, messageChannel);
                                        System.out.println("pong webSocketChannel: " + webSocketChannel + " messageChannel: " + messageChannel);
                                    }
                                });
                                channel.resumeReceives();
                            }
                        }))
                        .addPrefixPath("/index", Handlers.resource(new ClassPathResourceManager(Main.class.getClassLoader(), "")).addWelcomeFiles("index.html"))
                        .addPrefixPath("/api", deploymentManager.start())
                ).build();
        server.start();
    }

    private void createJavascriptService(String content, String requestServiceName, String filePath) {
        try {
            javascriptInterpreter = getJavascriptInterpreter();
            reloader = () -> {
                String newContent = Files.readAllLines(Paths.get(new URL(filePath).toURI()))
                        .stream()
                        .map(i -> i)
                        .collect(Collectors.joining("\n"));
                javascriptInterpreter.eval(newContent);
                Invocable invocable = (Invocable) javascriptInterpreter;
                Object instance = invocable.invokeFunction("instance");
                WSService = new JavascriptWSService(instance);
            };
            javascriptInterpreter.eval(content);
            Invocable invocable = (Invocable) javascriptInterpreter;
            Object instance = invocable.invokeFunction("instance");
            WSService = new JavascriptWSService(instance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRubyService(String content, String requestServiceName, String filePath) {
        rubyInterpreter = getRubyInterpreter();
        reloader = () -> {
            String newContent = Files.readAllLines(Paths.get(new URL(filePath).toURI()))
                    .stream()
                    .map(i -> i)
                    .collect(Collectors.joining("\n"));
            Object rubyObject = rubyInterpreter.runScriptlet(newContent);
            WSService = (WSService) rubyObject;
        };
        Object rubyObject = rubyInterpreter.runScriptlet(content);
        WSService = (WSService) rubyObject;
    }

    private void createPythonService(String content, String requestServiceName, String filePath) {
        pythonInterpreter = getPythonInterpreter();
        reloader = () -> {
            String newContent = Files.readAllLines(Paths.get(new URL(filePath).toURI()))
                    .stream()
                    .map(i -> i)
                    .collect(Collectors.joining("\n"));
            pythonInterpreter.exec(newContent);
            PyObject pyServiceObject = pythonInterpreter.get(requestServiceName);
            PyObject serviceObject = pyServiceObject.__call__();
            WSService = (WSService) serviceObject.__tojava__(WSService.class);
        };
        pythonInterpreter.exec(content);
        PyObject pyServiceObject = pythonInterpreter.get(requestServiceName);
        PyObject serviceObject = pyServiceObject.__call__();
        WSService = (WSService) serviceObject.__tojava__(WSService.class);
    }

    public PythonInterpreter getPythonInterpreter() {
        if (pythonInterpreter == null) {
            pythonInterpreter = new PythonInterpreter();
        }
        return pythonInterpreter;
    }

    public ScriptingContainer getRubyInterpreter() {
        if (rubyInterpreter == null) {
            rubyInterpreter = new ScriptingContainer();
        }
        return rubyInterpreter;
    }

    public ScriptEngine getJavascriptInterpreter() {
        if (javascriptInterpreter == null) {
            javascriptInterpreter = new ScriptEngineManager().getEngineByName("nashorn");
        }
        return javascriptInterpreter;
    }


/*

    public static void main2(String[] args) throws Exception {
        Main main = new Main();
        main.testJavaServiceMethod();
        main.testScalaServiceMethod();
        main.testPythonServiceMethod();
        main.testRubyServiceMethod();
        main.testGroovyServiceMethod();
        main.testJavaScriptServiceMethod();
    }

    private void testJavaScriptServiceMethod() {
        String response = "{\"error\":\"could not execute plugin\"}";

        String requestServiceName = "UserDBService";
        String requestMethodName = "findLecturesOfUser";
        String requestParameterString = "[{\"name\":\"john\",\"number\":220033}]";
        List<TreeMap> requestParameters = new Genson().deserialize(requestParameterString, new GenericType<List<TreeMap>>() {
        });

        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(requestServiceName + ".js");
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            String content = scanner.next();

            ScriptEngine engine = new ScriptEngineManager().getEngineByName("nashorn");
            engine.eval(content);
            Invocable invocable = (Invocable) engine;
            Object instance = invocable.invokeFunction("instance");

            List<Object> paramList = new ArrayList<>();
            if (requestParameters.size() > 0) {
                TreeMap parameters = requestParameters.get(0);
                for (Object value : parameters.values()) {
                    paramList.add(value);
                }
            }
            Object[] params = new Object[paramList.size()];
            params = paramList.toArray(params);
            Object result = ((ScriptObjectMirror) instance).callMember(requestMethodName, params);
            response = new Genson().serialize(result);

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("response = " + response);
    }

    private void testGroovyServiceMethod() {
        String response = "{\"error\":\"could not execute plugin\"}";

        String requestServiceName = "UserDBService";
        String requestMethodName = "findLecturesOfUser";
        String requestParameterString = "[{\"name\":\"john\",\"number\":220033}]";
        List<TreeMap> requestParameters = new Genson().deserialize(requestParameterString, new GenericType<List<TreeMap>>() {
        });

        try {

            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(requestServiceName + ".groovy");
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            String content = scanner.next();
            GroovyClassLoader classLoader = new GroovyClassLoader();
            Class pluginClass = classLoader.parseClass(content);
            GroovyObject groovyObj = null;
            try {
                groovyObj = (GroovyObject) pluginClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
            plugin = (WSService) groovyObj;

            Object[] parametersArray = null;
            Method callMethod = null;
            for (Method method : pluginClass.getMethods()) {
                if (method.getName().equals(requestMethodName)
                        && method.getParameterCount() == requestParameters.size()) {
                    callMethod = method;
                    break;
                }
            }
            if (callMethod != null) {
                List<Object> parameterList = new ArrayList<>();
                Class<?>[] parameterTypes = callMethod.getParameterTypes();
                for (int i = 0; i < requestParameters.size(); i++) {
                    String parameterString = new Genson().serialize(requestParameters.get(i));
                    Object parameterObject = new Genson().deserialize(parameterString, parameterTypes[i]);
                    parameterList.add(parameterObject);
                }
                parametersArray = parameterList.toArray();
                Object output = callMethod.invoke(plugin, parametersArray);
                response = new Genson().serialize(output);
            } else if (requestParameters.size() == 1) {
                TreeMap map = requestParameters.get(0);
                for (Method method : pluginClass.getMethods()) {
                    if (method.getName().equals(requestMethodName)
                            && method.getParameterCount() == map.size()) {
                        callMethod = method;
                        break;
                    }
                }
                if (callMethod != null) {
                    List<Object> parameterList = new ArrayList<>();
                    Class<?>[] parameterTypes = callMethod.getParameterTypes();
                    int i = 0;
                    for (Object key : map.keySet()) {
                        String parameterString = new Genson().serialize(map.get(key));
                        Object parameterObject = new Genson().deserialize(parameterString, parameterTypes[i]);
                        parameterList.add(parameterObject);
                        i++;
                    }
                    parametersArray = parameterList.toArray();
                }
            }

            if (callMethod != null) {
                Object output = callMethod.invoke(plugin, parametersArray);
                response = new Genson().serialize(output);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("response = " + response);
    }

    private void testRubyServiceMethod() throws InvocationTargetException, IllegalAccessException {
        String response = "{\"error\":\"could not execute plugin\"}";

        String requestServiceName = "UserDBService";
        String requestMethodName = "findLecturesOfUser";
        String requestParameterString = "[{\"name\":\"john\",\"number\":220033}]";
        List<TreeMap> requestParameters = new Genson().deserialize(requestParameterString, new GenericType<List<TreeMap>>() {
        });

        try {
            ScriptingContainer container = new ScriptingContainer();
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream(requestServiceName + ".rb");
            Scanner scanner = new Scanner(inputStream).useDelimiter("\\A");
            String content = scanner.next();
            Object greeter = container.runScriptlet(content);
            plugin = (WSService) greeter;

            List<IRubyObject> paramList = new ArrayList<>();
            if (requestParameters.size() > 0) {
                TreeMap parameters = requestParameters.get(0);
                for (Object value : parameters.values()) {
                    IRubyObject rubyObject = JavaUtil.convertJavaToRuby(Ruby.getGlobalRuntime(), value);
                    paramList.add(rubyObject);
                }
            }
            IRubyObject[] params = new IRubyObject[paramList.size()];
            params = paramList.toArray(params);

            IRubyObject result = ((RubyObject) plugin).callMethod(requestMethodName, params);
            response = new Genson().serialize(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("response = " + response);
    }

    private void testJavaServiceMethod() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        String response = "{\"error\":\"could not execute plugin\"}";

        String requestServiceName = "io.sato.UserDBService";
        String requestMethodName = "findLecturesOfUser";
        String requestParameterString = "[{\"name\":\"john\",\"number\":220033}]";

        List<TreeMap> requestParameters = new Genson().deserialize(requestParameterString, new GenericType<List<TreeMap>>() {
        });

        Object[] parametersArray = null;
        Method callMethod = null;
        Class<? extends WSService> pluginClass = Class.forName(requestServiceName).asSubclass(WSService.class);
        plugin = pluginClass.newInstance();
        for (Method method : pluginClass.getMethods()) {
            if (method.getName().equals(requestMethodName)
                    && method.getParameterCount() == requestParameters.size()) {
                callMethod = method;
                break;
            }
        }

        if (callMethod != null) {
            List<Object> parameterList = new ArrayList<>();
            Class<?>[] parameterTypes = callMethod.getParameterTypes();
            for (int i = 0; i < requestParameters.size(); i++) {
                String parameterString = new Genson().serialize(requestParameters.get(i));
                Object parameterObject = new Genson().deserialize(parameterString, parameterTypes[i]);
                parameterList.add(parameterObject);
            }
            parametersArray = parameterList.toArray();
            Object output = callMethod.invoke(plugin, parametersArray);
            response = new Genson().serialize(output);

        } else if (requestParameters.size() == 1) {
            TreeMap map = requestParameters.get(0);
            for (Method method : pluginClass.getMethods()) {
                if (method.getName().equals(requestMethodName)
                        && method.getParameterCount() == map.size()) {
                    callMethod = method;
                    break;
                }
            }
            if (callMethod != null) {
                List<Object> parameterList = new ArrayList<>();
                Class<?>[] parameterTypes = callMethod.getParameterTypes();
                int i = 0;
                for (Object key : map.keySet()) {
                    String parameterString = new Genson().serialize(map.get(key));
                    Object parameterObject = new Genson().deserialize(parameterString, parameterTypes[i]);
                    parameterList.add(parameterObject);
                    i++;
                }
                parametersArray = parameterList.toArray();
            }
        }

        if (callMethod != null) {
            Object output = callMethod.invoke(plugin, parametersArray);
            response = new Genson().serialize(output);
        }

        System.out.println("response = " + response);
    }

    private void testScalaServiceMethod() throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException {
        String response = "{\"error\":\"could not execute plugin\"}";

        String requestServiceName = "io.sato.UserDBServiceScala";
        String requestMethodName = "findLecturesOfUser";
        String requestParameterString = "[{\"name\":\"john\",\"number\":220033}]";

        List<TreeMap> requestParameters = new Genson().deserialize(requestParameterString, new GenericType<List<TreeMap>>() {
        });

        Object[] parametersArray = null;
        Method callMethod = null;
        Class<? extends WSService> pluginClass = Class.forName(requestServiceName).asSubclass(WSService.class);
        plugin = pluginClass.newInstance();
        for (Method method : pluginClass.getMethods()) {
            if (method.getName().equals(requestMethodName)
                    && method.getParameterCount() == requestParameters.size()) {
                callMethod = method;
                break;
            }
        }

        if (callMethod != null) {
            List<Object> parameterList = new ArrayList<>();
            Class<?>[] parameterTypes = callMethod.getParameterTypes();
            for (int i = 0; i < requestParameters.size(); i++) {
                String parameterString = new Genson().serialize(requestParameters.get(i));
                Object parameterObject = new Genson().deserialize(parameterString, parameterTypes[i]);
                parameterList.add(parameterObject);
            }
            parametersArray = parameterList.toArray();
            Object output = callMethod.invoke(plugin, parametersArray);
            response = new Genson().serialize(output);

        } else if (requestParameters.size() == 1) {
            TreeMap map = requestParameters.get(0);
            for (Method method : pluginClass.getMethods()) {
                if (method.getName().equals(requestMethodName)
                        && method.getParameterCount() == map.size()) {
                    callMethod = method;
                    break;
                }
            }
            if (callMethod != null) {
                List<Object> parameterList = new ArrayList<>();
                Class<?>[] parameterTypes = callMethod.getParameterTypes();
                int i = 0;
                for (Object key : map.keySet()) {
                    String parameterString = new Genson().serialize(map.get(key));
                    Object parameterObject = new Genson().deserialize(parameterString, parameterTypes[i]);
                    parameterList.add(parameterObject);
                    i++;
                }
                parametersArray = parameterList.toArray();
            }
        }

        if (callMethod != null) {
            Object output = callMethod.invoke(plugin, parametersArray);
            response = new Genson().serialize(output);
        }

        System.out.println("response = " + response);
    }
*/

}
