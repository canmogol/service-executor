package com.fererlab.commandline;

import java.net.URI;

public class CommandLineArguments {

    private URI scriptURI;
    private URI configURI;

    public CommandLineArguments(URI scriptURI, URI configURI) {
        this.scriptURI = scriptURI;
        this.configURI = configURI;
    }

    public URI getScriptURI() {
        return scriptURI;
    }

    public URI getConfigURI() {
        return configURI;
    }
}
