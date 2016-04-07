package com.fererlab;

import com.fererlab.filter.ReloadFilter;
import com.fererlab.restful.RestfulApplication;
import com.fererlab.service.Interpreter;
import com.fererlab.service.Service;
import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jruby.embed.ScriptingContainer;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import javax.servlet.DispatcherType;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Main {

    /**
     * GamApp logger
     */
    private static Logger logger = Logger.getLogger(Main.class.getName());

    /**
     * undertow web server
     */
    private UndertowJaxrsServer server;

    /**
     * undertow web server thread
     */
    private Thread webServerThread;

    /**
     * Python pythonInterpreter
     */
    private PythonInterpreter pythonInterpreter = null;
    private ScriptingContainer rubyInterpreter = null;
    public static Interpreter interpreter;

    public static void main(String[] args) throws Exception {
        //args = new String[]{"/mnt/sda1/IdeaProjects/canmogol/service-executor/src/main/resources/", "Authentication", "py"};
        args = new String[]{"/mnt/sda1/IdeaProjects/canmogol/service-executor/src/main/resources/", "Authentication", "rb"};
        String filePath = "file://" + args[0] + args[1] + "." + args[2];
        String content = Files.readAllLines(Paths.get(new URL(filePath).toURI()))
                .stream()
                .map(i -> i)
                .collect(Collectors.joining("\n"));

        Main main = new Main();
//        Service service = main.createPythonService(content, args[1], filePath);
        Service service = main.createRubyService(content, args[1], filePath);
        main.startServer(service);
    }

    private void startServer(Service service) {
        if (webServerThread == null) {
            logger.info("starting web server");
            webServerThread = new Thread() {
                @Override
                public void run() {
                    restartServer(service);
                }
            };
            webServerThread.start();
        } else {
            logger.info("web server already started, using existing server");
        }
    }

    private void restartServer(Service service) {
        // create server
        long time = System.currentTimeMillis();
        server = new UndertowJaxrsServer();
        logger.info("server created: " + (System.currentTimeMillis() - time) + " milli seconds");

        time = System.currentTimeMillis();
        Undertow.Builder serverBuilder = Undertow.builder().addHttpListener(9876, "localhost");
        logger.info("server port: " + 9876);
        server.start(serverBuilder);
        logger.info("server started: " + (System.currentTimeMillis() - time) + " milli seconds");

        // start server
        time = System.currentTimeMillis();
        RestfulApplication restfulApplication = new RestfulApplication() {

            @Override
            public Set<Object> getSingletons() {
                Set<Object> singletons = new HashSet<>();
                singletons.add(service);
                return singletons;
            }

            @Override
            public Set<Class<?>> getClasses() {
                Set<Class<?>> classes = new HashSet<>();
//                classes.add(service.getClass());
                return classes;
            }
        };
        ResteasyDeployment deployment = new ResteasyDeployment();
        deployment.setApplication(restfulApplication);
        DeploymentInfo deploymentInfo = server.undertowDeployment(deployment, "");
        deploymentInfo.setClassLoader(getClass().getClassLoader());
        deploymentInfo.setContextPath("/");
        deploymentInfo.setDeploymentName("");
        deploymentInfo.setDefaultEncoding("UTF-8");

        FilterInfo filter = Servlets.filter("ReloadFilter", ReloadFilter.class);
        deploymentInfo.addFilter(filter);
        deploymentInfo.addFilterUrlMapping("ReloadFilter", "/*", DispatcherType.REQUEST);

        // deploy server
        server.deploy(deploymentInfo);
        logger.info("server deployed: " + (System.currentTimeMillis() - time) + " milli seconds");
    }

    private Service createRubyService(String content, String requestServiceName, String filePath) {
        rubyInterpreter = getRubyInterpreter();
        interpreter = () -> {
            String newContent = Files.readAllLines(Paths.get(new URL(filePath).toURI()))
                    .stream()
                    .map(i -> i)
                    .collect(Collectors.joining("\n"));
            Object rubyObject = rubyInterpreter.runScriptlet(newContent);
            Service service = (Service) rubyObject;
        };
        Object rubyObject = rubyInterpreter.runScriptlet(content);
        Service service = (Service) rubyObject;
        return service;
    }

    private Service createPythonService(String content, String requestServiceName, String filePath) {
        pythonInterpreter = getPythonInterpreter();
        interpreter = () -> {
            String newContent = Files.readAllLines(Paths.get(new URL(filePath).toURI()))
                    .stream()
                    .map(i -> i)
                    .collect(Collectors.joining("\n"));
            pythonInterpreter.exec(newContent);
            PyObject pyServiceObject = pythonInterpreter.get(requestServiceName);
            PyObject serviceObject = pyServiceObject.__call__();
            Service service = (Service) serviceObject.__tojava__(Service.class);
        };
        pythonInterpreter.exec(content);
        PyObject pyServiceObject = pythonInterpreter.get(requestServiceName);
        PyObject serviceObject = pyServiceObject.__call__();
        Service service = (Service) serviceObject.__tojava__(Service.class);
        return service;
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
            Service plugin = (Service) groovyObj;

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
            Service plugin = (Service) greeter;

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
        Class<? extends Service> pluginClass = Class.forName(requestServiceName).asSubclass(Service.class);
        Service plugin = pluginClass.newInstance();
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
        Class<? extends Service> pluginClass = Class.forName(requestServiceName).asSubclass(Service.class);
        Service plugin = pluginClass.newInstance();
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
