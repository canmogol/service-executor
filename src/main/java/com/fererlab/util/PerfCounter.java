package com.fererlab.util;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class PerfCounter {

    public static final String RELOADER_CREATION_TIME = "RELOADER_CREATION_TIME";
    public static final String CONFIGURATION_READ_TIME = "CONFIGURATION_READ_TIME";
    public static final String SERVER_CREATION_TIME = "SERVER_CREATION_TIME";
    public static final String SERVER_START_TIME = "SERVER_START_TIME";
    public static final String SERVER_DEPLOY_TIME = "SERVER_DEPLOY_TIME";
    public static final String PYTHON_INTERPRETER_CREATED = "PYTHON_INTERPRETER_CREATED";

    private static final Map<String, List<Long>> performances = new HashMap<>();

    public static void add(String key, long milliSeconds) {
        if (!performances.containsKey(key)) {
            performances.put(key, new LinkedList<>());
        }
        performances.get(key).add(milliSeconds);
    }

}
