package com.fererlab.reload;

import com.fererlab.service.Reloader;

import java.net.URI;

public class ReloaderFactory {
    public static Reloader createReloader(URI scriptURI, String fileExtension) {
        switch (fileExtension) {
            case "py":
                return new PythonReloader(scriptURI);
            case "rb":
            case "js":
            case "cj":
            case "scala":
            case "groovy":
            case "java":
            default:
                return null;
        }
    }
}
