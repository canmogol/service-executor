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
            case "py":
                return new PythonReloader(scriptURI);
            case "rb":
                return new RubyReloader(scriptURI);
            case "js":
                return new JavascriptReloader(scriptURI);
            case "java":
            case "groovy":
                return new GroovyReloader(scriptURI);
            case "cj":
            case "scala":
            default:
                return null;
        }
    }
}
