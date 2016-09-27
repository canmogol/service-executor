package com.fererlab.language.javascript;

import com.fererlab.content.ContentLoader;
import com.fererlab.content.ContentLoaderFactory;
import com.fererlab.event.Event;
import com.fererlab.event.Request;
import com.fererlab.log.FLogger;
import com.fererlab.service.ScriptingService;
import com.fererlab.service.Service;
import com.fererlab.util.Maybe;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.net.URI;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaScriptService implements Service, ScriptingService {

    private static final Logger log = FLogger.getLogger(JavaScriptService.class.getSimpleName());

    private final URI scriptURI;
    private Object instance;

    public JavaScriptService(URI scriptURI) {
        this.scriptURI = scriptURI;
    }

    @Override
    public Object handle(Event event) {
        try {
            Object instance = getInstanceObject();
            Object result = ((ScriptObjectMirror) instance).callMember("handle", event);
            if (result instanceof ScriptObjectMirror) {
                result = convert((ScriptObjectMirror) result);
            }
            return result;
        } catch (Exception e) {
            String error = "could not create/execute javascript service, error: " + e.getMessage();
            log.log(Level.SEVERE, error, e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object execute(String methodName, Request request) {
        try {
            Object instance = getInstanceObject();
            Object result = ((ScriptObjectMirror) instance).callMember(methodName, request);
            return result;
        } catch (Exception e) {
            String error = "could not create/execute javascript service, error: " + e.getMessage();
            log.log(Level.SEVERE, error, e);
            throw new RuntimeException(e);
        }
    }

    private Object getInstanceObject() throws ScriptException, NoSuchMethodException {
        if (instance == null) {
            ContentLoader contentLoader = ContentLoaderFactory.createLoader(scriptURI.getScheme());
            Maybe<String> content = contentLoader.load(scriptURI);
            if (content.isPresent()) {
                ScriptEngine javascriptInterpreter = new ScriptEngineManager().getEngineByName("nashorn");
                javascriptInterpreter.eval(content.get());
                Invocable invocable = (Invocable) javascriptInterpreter;
                instance = invocable.invokeFunction("instance");
            } else {
                log.severe("could not get content from URI: " + scriptURI);
            }
        }
        return instance;
    }

    private Map<String, Object> convert(ScriptObjectMirror scriptObjectMirror) {
        Map<String, Object> map = new TreeMap<>();
        for (String key : scriptObjectMirror.keySet()) {
            Object value = scriptObjectMirror.get(key);
            if (value instanceof ScriptObjectMirror) {
                ScriptObjectMirror scriptObjectMirrorValue = (ScriptObjectMirror) value;
                if (((ScriptObjectMirror) value).isArray()) {
                    map.put(key, scriptObjectMirrorValue.values());
                } else {
                    map.put(key, this.convert(scriptObjectMirrorValue));
                }
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

}
