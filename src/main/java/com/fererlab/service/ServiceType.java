package com.fererlab.service;

import com.fererlab.log.FLogger;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public enum ServiceType {
    JAVASCRIPT, PYTHON, RUBY, PHP, CLOJURE, GROOVY, JAVA, SCALA, CLASS;

    private static final Logger log = FLogger.getLogger(ServiceType.class.getSimpleName());

    public static ServiceType find(URI scriptURI) {
        String fileExtension = null;
        if (scriptURI.getPath() != null) {
            fileExtension = scriptURI.getPath().substring(scriptURI.getPath().lastIndexOf(".") + 1);
        } else if ("jdbc".equalsIgnoreCase(scriptURI.getScheme())) {
            try {
                fileExtension = scriptURI.getSchemeSpecificPart().substring(scriptURI.getSchemeSpecificPart().lastIndexOf("programming_language")).split("=")[1].split("\\&")[0];
            } catch (Exception e) {
                String error = "got exception while getting the service type, exception: " + e;
                log.log(Level.SEVERE, error, e);
            }
        }
        if (fileExtension != null) {
            switch (fileExtension.toLowerCase()) {
                case "javascript":
                case "js":
                    return JAVASCRIPT;
                case "python":
                case "py":
                    return PYTHON;
                case "ruby":
                case "rb":
                    return RUBY;
                case "php":
                    return PHP;
                case "clojure":
                case "cj":
                    return CLOJURE;
                case "groovy":
                    return GROOVY;
                case "scala":
                    return SCALA;
                case "java":
                    // there will be a java interpreter in java 9,
                    // until then we will use groovy interpreter for java source files
                    return GROOVY;
            }
        }
        // if there is no file extension then it is a compiled class
        return CLASS;
    }

}
