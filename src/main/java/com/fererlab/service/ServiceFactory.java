package com.fererlab.service;

import com.fererlab.language.clojure.ClojureService;
import com.fererlab.language.groovy.GroovyService;
import com.fererlab.language.java.JavaService;
import com.fererlab.language.javascript.JavaScriptService;
import com.fererlab.language.php.PHPService;
import com.fererlab.language.python.PythonService;
import com.fererlab.language.ruby.RubyService;
import com.fererlab.language.scala.ScalaService;

import java.net.URI;

public class ServiceFactory {

    public static Service create(ServiceType serviceType, URI scriptURI) {
        switch (serviceType) {
            case PYTHON:
                return new PythonService(scriptURI);
            case RUBY:
                return new RubyService(scriptURI);
            case JAVASCRIPT:
                return new JavaScriptService(scriptURI);
            case JAVA:
            case GROOVY:
                return new GroovyService(scriptURI);
            case PHP:
                return new PHPService();
            case CLOJURE:
                return new ClojureService();
            case SCALA:
                return new ScalaService();
            default:
                return new JavaService(scriptURI);
        }
    }

}
