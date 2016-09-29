package com.fererlab.reload;

import com.fererlab.language.groovy.GroovyReloader;
import com.fererlab.language.javascript.JavascriptReloader;
import com.fererlab.language.python.PythonReloader;
import com.fererlab.language.ruby.RubyReloader;
import com.fererlab.service.Reloader;

import java.net.URI;

public class ReloaderFactory {
    public static Reloader createReloader(URI scriptURI, String fileExtension) {
        switch (fileExtension) {
            case "python":
            case "py":
                return new PythonReloader(scriptURI);
            case "ruby":
            case "rb":
                return new RubyReloader(scriptURI);
            case "javascript":
            case "js":
                return new JavascriptReloader(scriptURI);
            case "java":
            case "clojure":
            case "cj":
            case "scala":
            case "groovy":
                return new GroovyReloader(scriptURI);
            default:
                return null;
        }
    }
}
