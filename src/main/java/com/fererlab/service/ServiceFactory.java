package com.fererlab.service;

import com.fererlab.language.javascript.JavaScriptService;
import com.fererlab.language.clojure.ClojureService;
import com.fererlab.language.groovy.GroovyService;
import com.fererlab.language.java.JavaService;
import com.fererlab.language.php.PHPService;
import com.fererlab.language.python.PythonService;
import com.fererlab.language.ruby.RubyService;
import com.fererlab.language.scala.ScalaService;

public class ServiceFactory {

    public static Service create(ServiceType serviceType) {
        switch (serviceType) {
            case JAVASCRIPT:
                return new JavaScriptService();
            case PYTHON:
                return new PythonService();
            case RUBY:
                return new RubyService();
            case PHP:
                return new PHPService();
            case CLOJURE:
                return new ClojureService();
            case GROOVY:
                return new GroovyService();
            case SCALA:
                return new ScalaService();
            default:
                return new JavaService();
        }
    }

}
