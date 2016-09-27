package com.fererlab.language.ruby;

import com.fererlab.content.ContentLoader;
import com.fererlab.content.ContentLoaderFactory;
import com.fererlab.event.Event;
import com.fererlab.event.Request;
import com.fererlab.log.FLogger;
import com.fererlab.service.ScriptingService;
import com.fererlab.service.Service;
import com.fererlab.util.Maybe;
import org.jruby.embed.ScriptingContainer;

import java.net.URI;
import java.util.logging.Logger;

public class RubyService implements Service, ScriptingService{

    private static final Logger log = FLogger.getLogger(RubyService.class.getSimpleName());

    private final URI scriptURI;
    private Service service;

    public RubyService(URI scriptURI) {
        this.scriptURI = scriptURI;
    }

    @Override
    public Object handle(Event event) {
        return getService().handle(event);
    }

    @Override
    public Object execute(String methodName, Request request) {
        Service service = getService();
        Object result = RubyInter.getInstance().getRubyInterpreter().callMethod(service, methodName, request);
        return result;
    }

    private Service getService() {
        if (service == null) {
            ContentLoader contentLoader = ContentLoaderFactory.createLoader(scriptURI.getScheme());
            Maybe<String> content = contentLoader.load(scriptURI);
            if (content.isPresent()) {
                ScriptingContainer rubyInterpreter = RubyInter.getInstance().getRubyInterpreter();
                Object rubyObject = rubyInterpreter.runScriptlet(content.get());
                service = (Service) rubyObject;
            } else {
                log.severe("could not get content from URI: " + scriptURI);
            }
        }
        return service;
    }

}
