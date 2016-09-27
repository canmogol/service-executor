package com.fererlab.content;

import com.fererlab.log.FLogger;
import com.fererlab.util.Maybe;

import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ClassPathContentLoader implements ContentLoader {

    private static final Logger log = FLogger.getLogger(ClassPathContentLoader.class.getSimpleName());

    @Override
    public Maybe<String> load(URI uri) {
        String result = null;
        try {
            Class<?> serviceClass = this.getClass().getClassLoader().loadClass(uri.getHost());
            result = "Class found with name: " + serviceClass.getName();
        } catch (ClassNotFoundException e) {
            String error = "got exception while loading class from URI: " + uri + " exception: " + e.toString();
            log.log(Level.SEVERE, error, e);
        }
        return Maybe.create(result);
    }
}
