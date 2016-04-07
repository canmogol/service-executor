package com.fererlab.service;

import jdk.nashorn.api.scripting.ScriptObjectMirror;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class JavascriptService implements Service {
    private final Object instance;

    public JavascriptService(Object instance) {
        this.instance = instance;
    }

    @Override
    public Object handle(HashMap<String, String> event) {
        Object object = ((ScriptObjectMirror) instance).callMember("handle", event);
        if (object instanceof ScriptObjectMirror) {
            object = convert((ScriptObjectMirror) object);
        }
        return object;
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
}
