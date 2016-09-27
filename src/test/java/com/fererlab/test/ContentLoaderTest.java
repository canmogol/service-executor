package com.fererlab.test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class ContentLoaderTest {

    private Map<String, Integer> defaultPorts = new HashMap<>();

    public static void main(String[] args) throws Exception {
        ContentLoaderTest contentLoaderTest = new ContentLoaderTest();
//        contentLoaderTest.testURLLoad();
        contentLoaderTest.testHttpClient();
    }

    private void testHttpClient() throws URISyntaxException, IOException {
        URI uri = new URI("http://localhost/service/Authentication.py");
        printURI(uri);
        uri = new URI("ftp://localhost:21/service/Authentication.py");
        printURI(uri);
        uri = new URI("jdbc:postgresql://localhost:5432/serviceexecutor");
        printURI(uri);
        Path path = Paths.get(uri);
        String content = Files.readAllLines(path).stream().collect(Collectors.joining("\n"));
        System.out.println("content = " + content);
    }

    private void printURI(URI uri) {
        System.out.println(uri.getScheme() + "\t" + uri.getHost() + "\t" + uri.getPort() + "\t" + uri.getPath());
    }

    private void testURLLoad() throws MalformedURLException {
        // "http://localhost/service/Authentication.py";
        String scriptPath = "localhost/service";
        String host = scriptPath.split("/")[0];
        String rest = scriptPath.substring(host.length());
        String scriptFileProtocol = "http";
        Integer port = defaultPorts.get(scriptFileProtocol);
        if (host.contains(":")) {
            String[] hostAndPort = host.split(":");
            host = hostAndPort[0];
            port = Integer.valueOf(hostAndPort[1]);
        }
        String scriptName = "Authentication.py";
        String scriptExtension = "py";
//        new URLConnection(new URL(scriptFileProtocol, host, port, rest + scriptName));
    }

}
