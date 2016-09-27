package com.fererlab.content;

import com.fererlab.util.Maybe;

import java.net.URI;

public interface ContentLoader {
    Maybe<String> load(URI uri);
}
