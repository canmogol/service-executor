package com.fererlab.content;

import com.fererlab.util.Maybe;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;

public class HttpContentLoader implements ContentLoader {
    @Override
    public Maybe<String> load(URI uri) {
        String content = null;
        try {
            URL url = uri.toURL();
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            if (connection.getResponseCode() == 200) {
                InputStream is = connection.getInputStream();
                StringBuilder builder = new StringBuilder();
                int i;
                while ((i = is.read()) != -1) {
                    char c = (char) i;
                    builder.append(c);
                }
                content = builder.toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Maybe.create(content);
    }
}
