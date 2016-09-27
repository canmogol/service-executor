package com.fererlab.service;

public enum ServiceType {
    JAVASCRIPT, PYTHON, RUBY, PHP, CLOJURE, GROOVY, SCALA, CLASS;

    public static ServiceType find(String path) {
        if (path.lastIndexOf(".") != -1) {
            String fileExtension = path.substring(path.lastIndexOf(".") + 1);
            switch (fileExtension.toLowerCase()) {
                case "js":
                    return JAVASCRIPT;
                case "py":
                    return PYTHON;
                case "rb":
                    return RUBY;
                case "php":
                    return PHP;
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
