package com.fererlab.language.python;

import com.fererlab.content.ContentLoader;
import com.fererlab.content.ContentLoaderFactory;
import com.fererlab.event.Event;
import com.fererlab.event.Request;
import com.fererlab.log.FLogger;
import com.fererlab.service.ScriptingService;
import com.fererlab.service.Service;
import com.fererlab.util.Maybe;
import org.python.core.Py;
import org.python.core.PyObject;
import org.python.core.PyProxy;
import org.python.util.PythonInterpreter;

import java.net.URI;
import java.util.logging.Logger;

public class PythonService implements Service, ScriptingService {

    private static final Logger log = FLogger.getLogger(PythonService.class.getSimpleName());

    private final URI scriptURI;
    private Service service;

    public PythonService(URI scriptURI) {
        this.scriptURI = scriptURI;
    }

    @Override
    public Object handle(Event event) {
        return getService().handle(event);
    }

    @Override
    public Object execute(String methodName, Request request) {
        Service service = getService();
        PyObject[] args = Py.javas2pys(request);
        PyObject result = ((PyProxy) service)._getPyInstance().invoke(methodName, args);
        return result;
    }

    private Service getService() {
        if (service == null) {
            String serviceName = null;
            String path = scriptURI.getPath();
            if (path.lastIndexOf("/") != -1) {
                String fileName = path.substring(path.lastIndexOf("/") + 1);
                if (fileName.lastIndexOf(".") != -1) {
                    serviceName = fileName.substring(0, fileName.lastIndexOf("."));
                }
            }
            ContentLoader contentLoader = ContentLoaderFactory.createLoader(scriptURI.getScheme());
            Maybe<String> content = contentLoader.load(scriptURI);
            if (content.isPresent()) {
                PythonInterpreter pythonInterpreter = PythonInter.getInstance().getPythonInterpreter();
                pythonInterpreter.exec(content.get());
                PyObject pyServiceObject = pythonInterpreter.get(serviceName);
                PyObject serviceObject = pyServiceObject.__call__();
                service = (Service) serviceObject.__tojava__(Service.class);
            } else {
                log.severe("could not get content from URI: " + scriptURI);
            }
        }
        return service;
    }

}
