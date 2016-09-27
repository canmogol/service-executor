package com.fererlab;

import com.fererlab.commandline.CommandLineArguments;
import com.fererlab.commandline.CommandLineParser;
import com.fererlab.config.Configuration;
import com.fererlab.filter.ReloadFilter;
import com.fererlab.language.javascript.JavaScriptService;
import com.fererlab.log.FLogger;
import com.fererlab.restful.RestfulApplication;
import com.fererlab.service.Reloader;
import com.fererlab.service.ScriptingService;
import com.fererlab.service.Service;
import com.fererlab.util.Maybe;
import com.fererlab.util.PerfCounter;
import io.undertow.Undertow;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.DeploymentInfo;
import io.undertow.servlet.api.FilterInfo;
import io.undertow.servlet.api.InstanceFactory;
import io.undertow.servlet.api.InstanceHandle;
import org.jboss.resteasy.plugins.server.undertow.UndertowJaxrsServer;
import org.jboss.resteasy.spi.ResteasyDeployment;
import org.jruby.embed.ScriptingContainer;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main implements Runnable {

    /**
     * Logger
     */
    private static final Logger log = FLogger.getLogger(Main.class.getName());

    /**
     * command line arguments
     */
    private final String[] args;


    public Main(String[] args) {
        this.args = args;
    }

    /**
     * main method for command line executions
     *
     * @param args command line arguments
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        // example argument
//        args = new String[]{"../../src/main/resources/Authentication.py"};
        args = new String[]{"http://localhost/service/Authentication.py"};

        // create an object
        Main main = new Main(args);

        // call run method, may be called in a thread
        main.run();

    }

    @Override
    public void run() {
        try {
            // Parse command line parameters
            CommandLineParser parser = new CommandLineParser();
            CommandLineArguments arguments = parser.parse(args);

            // Service Executor
            ServiceExecutor serviceExecutor = new ServiceExecutor(arguments.getScriptURI(), arguments.getConfigURI());
            Maybe<Service> mService = serviceExecutor.createService();
            mService.notEmpty(service -> {
                // run service in another Thread
                Thread webServerThread = new Thread(() -> {
                    // initial time
                    long time = System.currentTimeMillis();

                    // RELOADER
                    Maybe<Reloader> mReloader = serviceExecutor.createReloader();
                    long reloaderCreationTime = (System.currentTimeMillis() - time);
                    log.info("reloader created: " + reloaderCreationTime + " milli seconds");
                    PerfCounter.add(PerfCounter.RELOADER_CREATION_TIME, reloaderCreationTime);

                    // CONFIGURATION
                    time = System.currentTimeMillis();
                    Configuration configuration = serviceExecutor.readConfiguration();
                    long configReadTime = (System.currentTimeMillis() - time);
                    log.info("configuration read: " + configReadTime + " milli seconds");
                    PerfCounter.add(PerfCounter.CONFIGURATION_READ_TIME, configReadTime);

                    // JAXRS SERVER
                    log.info("starting web server");
                    time = System.currentTimeMillis();
                    UndertowJaxrsServer server = new UndertowJaxrsServer();
                    long serverCreationTime = (System.currentTimeMillis() - time);
                    log.info("server created: " + serverCreationTime + " milli seconds");
                    PerfCounter.add(PerfCounter.SERVER_CREATION_TIME, serverCreationTime);

                    // SERVER START
                    time = System.currentTimeMillis();
                    Undertow.Builder serverBuilder = Undertow.builder().addHttpListener(
                            configuration.getWebServerPort(),
                            configuration.getWebServerHostname()
                    );
                    log.info("web server port: " + configuration.getWebServerPort() + " hostname: " + configuration.getWebServerHostname());
                    server.start(serverBuilder);
                    long serverStartTime = (System.currentTimeMillis() - time);
                    log.info("server started: " + serverStartTime + " milli seconds");
                    PerfCounter.add(PerfCounter.SERVER_START_TIME, serverStartTime);

                    // DEPLOY
                    time = System.currentTimeMillis();
                    RestfulApplication restfulApplication = new RestfulApplication() {
                        @Override
                        public Set<Object> getSingletons() {
                            Set<Object> singletons = new HashSet<>();
                            singletons.add(serviceExecutor.getServiceProxy());
                            return singletons;
                        }
                    };
                    ResteasyDeployment deployment = new ResteasyDeployment();
                    deployment.setApplication(restfulApplication);
                    DeploymentInfo deploymentInfo = server.undertowDeployment(deployment, "/test");
                    deploymentInfo.setClassLoader(getClass().getClassLoader());
                    deploymentInfo.setContextPath("/");
                    deploymentInfo.setDeploymentName("");
                    deploymentInfo.setDefaultEncoding("UTF-8");

                    // FILTER
                    FilterInfo filter = Servlets.filter("ReloadFilter", ReloadFilter.class, new InstanceFactory<Filter>() {
                        @Override
                        public InstanceHandle<Filter> createInstance() throws InstantiationException {
                            return new InstanceHandle<Filter>() {
                                @Override
                                public Filter getInstance() {
                                    return new ReloadFilter(mReloader.get());
                                }

                                @Override
                                public void release() {
                                    System.out.println("-----release");
                                }
                            };
                        }
                    });
                    deploymentInfo.addFilter(filter);
                    deploymentInfo.addFilterUrlMapping("ReloadFilter", "/*", DispatcherType.REQUEST);

                    // DEPLOY SERVER
                    server.deploy(deploymentInfo);
                    long serverDeployTime = (System.currentTimeMillis() - time);
                    log.info("server deployed: " + serverDeployTime + " milli seconds");
                    PerfCounter.add(PerfCounter.SERVER_DEPLOY_TIME, serverDeployTime);

                });

                // start web server Thread
                webServerThread.start();

            });
        } catch (Exception exception) {
            String error = "could not get the content of file, args: " + Arrays.toString(args) + " will quit now, exception: " + exception.getMessage();
            log.log(Level.SEVERE, error, exception);
        }
    }


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

    private Reloader reloader;
    private Service service;

    private void createJavascriptService(String content, String requestServiceName, String filePath) {
        try {
            javascriptInterpreter = getJavascriptInterpreter();
//            reloader = () -> {
//                String newContent = Files.readAllLines(Paths.get(new URL(filePath).toURI()))
//                        .stream()
//                        .collect(Collectors.joining("\n"));
//                javascriptInterpreter.eval(newContent);
//                Invocable invocable = (Invocable) javascriptInterpreter;
//                Object instance = invocable.invokeFunction("instance");
//                service = new JavaScriptService();
//                ((ScriptingService) service).setInstance(instance);
//
//            };
            javascriptInterpreter.eval(content);
            Invocable invocable = (Invocable) javascriptInterpreter;
            Object instance = invocable.invokeFunction("instance");
            service = new JavaScriptService();
            ((ScriptingService) service).setInstance(instance);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void createRubyService(String content, String requestServiceName, String filePath) {
        rubyInterpreter = getRubyInterpreter();
//        reloader = () -> {
//            String newContent = Files.readAllLines(Paths.get(new URL(filePath).toURI()))
//                    .stream()
//                    .collect(Collectors.joining("\n"));
//            Object rubyObject = rubyInterpreter.runScriptlet(newContent);
//            service = (Service) rubyObject;
//        };
        Object rubyObject = rubyInterpreter.runScriptlet(content);
        service = (Service) rubyObject;
    }

    private void createPythonService(String content, String requestServiceName, String filePath) {
        pythonInterpreter = getPythonInterpreter();
//        reloader = () -> {
//            String newContent = Files.readAllLines(Paths.get(new URL(filePath).toURI()))
//                    .stream()
//                    .collect(Collectors.joining("\n"));
//            pythonInterpreter.exec(newContent);
//            PyObject pyServiceObject = pythonInterpreter.get(requestServiceName);
//            PyObject serviceObject = pyServiceObject.__call__();
//            service = (Service) serviceObject.__tojava__(Service.class);
//        };
        pythonInterpreter.exec(content);
        PyObject pyServiceObject = pythonInterpreter.get(requestServiceName);
        PyObject serviceObject = pyServiceObject.__call__();
        service = (Service) serviceObject.__tojava__(Service.class);
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
            plugin = (Service) groovyObj;

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
            plugin = (Service) greeter;

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
        Class<? extends Service> pluginClass = Class.forName(requestServiceName).asSubclass(Service.class);
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
