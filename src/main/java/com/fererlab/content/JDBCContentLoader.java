package com.fererlab.content;

import com.fererlab.util.Maybe;

import java.net.URI;

public class JDBCContentLoader implements ContentLoader {
    @Override
    public Maybe<String> load(URI uri) {
        return Maybe.empty();
    }
}
