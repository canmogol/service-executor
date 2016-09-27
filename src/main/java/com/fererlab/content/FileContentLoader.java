package com.fererlab.content;

import com.fererlab.log.FLogger;
import com.fererlab.util.Maybe;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class FileContentLoader implements ContentLoader {

    private static final Logger log = FLogger.getLogger(FileContentLoader.class.getSimpleName());

    @Override
    public Maybe<String> load(URI uri) {
        String fileContent = null;
        try {
            fileContent = Files.readAllLines(Paths.get(uri)).stream().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            String error = "could not get file content of uri: " + uri + " error: " + e.getMessage();
            log.log(Level.SEVERE, error);
        }
        return Maybe.create(fileContent);
    }

}
