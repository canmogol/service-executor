package com.fererlab.service;

import com.fererlab.event.Event;
import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.Map;
import java.util.TreeMap;

public class JavascriptWSService implements ProxyWSService {
    private final Object instance;

    public JavascriptWSService(Object instance) {
        this.instance = instance;
    }

    @Override
    public void handle(Event event) {
        Object object = ((ScriptObjectMirror) instance).callMember("handle", event);
        if (object instanceof ScriptObjectMirror) {
            object = convert((ScriptObjectMirror) object);
        }
//        return object;
    }

    private static Map<String, Object> convert(ScriptObjectMirror scriptObjectMirror) {
        Map<String, Object> map = new TreeMap<>();
        for (String key : scriptObjectMirror.keySet()) {
            Object value = scriptObjectMirror.get(key);
            if (value instanceof ScriptObjectMirror) {
                ScriptObjectMirror scriptObjectMirrorValue = (ScriptObjectMirror) value;
                if (((ScriptObjectMirror) value).isArray()) {
                    map.put(key, scriptObjectMirrorValue.values());
                } else {
                    map.put(key, convert(scriptObjectMirrorValue));
                }
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    @Override
    public Object execute(String methodName, Object param) {
        return ((ScriptObjectMirror) instance).callMember(methodName, param);
    }

}
