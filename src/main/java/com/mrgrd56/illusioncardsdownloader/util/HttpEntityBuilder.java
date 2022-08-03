package com.mrgrd56.illusioncardsdownloader.util;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;

import java.util.Map;

public class HttpEntityBuilder {
    private final HttpHeaders httpHeaders = new HttpHeaders();

    public HttpEntityBuilder header(String name, String value) {
        httpHeaders.set(name, value);
        return this;
    }

    public HttpEntityBuilder headers(Map<String, String> headers) {
        headers.forEach(this::header);
        return this;
    }

    public HttpEntity<Void> build() {
        return new HttpEntity<>(httpHeaders);
    }
}
