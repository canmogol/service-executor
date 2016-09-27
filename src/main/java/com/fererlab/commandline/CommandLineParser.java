package com.fererlab.commandline;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.Optional;

public class CommandLineParser {

    public CommandLineArguments parse(String[] args) throws Exception {
        // there should be arguments to be parsed
        if (args != null && args.length > 0) {

            // currently script to be executed expected as first parameter and it is mandatory
            URI scriptURI;
            scriptURI = createURI(args[0]);

            // setting config from command line is also possible,
            // otherwise the builtin configuration json will be used
            URI configURI = null;
            String configPrefix = "--config=";
            Optional<String> configOptional = Arrays.asList(args).stream()
                    .filter(arg -> arg != null)
                    .map(String::trim)
                    .filter(arg -> arg.startsWith(configPrefix))
                    .findFirst();
            if (configOptional.isPresent()) {
                configURI = createURI(configOptional.get().substring(configPrefix.length()));
            }
            return new CommandLineArguments(scriptURI, configURI);

        } else {
            throw new Exception("there are no command line arguments available, will exit now");
        }
    }

    private URI createURI(String arg) throws URISyntaxException {
        if (arg.lastIndexOf("://") == -1) {
            // define command line arguments
            String folder = null;
            String name = arg; // most of the time first argument will be the filename
            String extension;
            String scheme = "file";

            // set folder's initial value
            URL url = this.getClass().getClassLoader().getResource(".");
            if (url != null) {
                folder = url.getPath();
            }

            if (arg.lastIndexOf("/") != -1) {
                name = arg.substring(arg.lastIndexOf("/") + 1);
                folder = folder + File.separator + arg.substring(0, arg.lastIndexOf("/"));
            }

            // finally set the extension
            extension = name.substring(name.lastIndexOf(".") + 1);
            name = name.substring(0, name.lastIndexOf("."));

            // return command line arguments
            String uri = scheme + "://" + folder + File.separator + name + "." + extension;
            return new URI(uri);
        } else {
            return new URI(arg);
        }
    }

}
