package de.w3is.jdial.protocol;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

class URLBuilder {

    private static final String PATH_SEPARATOR = "/";
    private static final String QUERY_SEPARATOR = "&";
    private static final String QUERY_KEY_VALUE_SEPARATOR = "=";
    private static final String PATH_QUERY_SEPARATOR = "?";
    private static final String PATH_QUERY_SPLITTER = "\\?";

    private String protocol = "http";
    private String host = "localhost";
    private int port = 80;

    private StringJoiner paths = new StringJoiner(PATH_SEPARATOR);
    private StringJoiner query = new StringJoiner(QUERY_SEPARATOR);

    private URLBuilder() {}

    public static URLBuilder of(URL url) {

        URLBuilder urlBuilder = new URLBuilder()
                .protocol(url.getProtocol())
                .host(url.getHost())
                .port(url.getPort())
                .path(url.getPath());

        if (url.getQuery() != null) {
            String[] queryParts = url.getQuery().split(PATH_QUERY_SPLITTER);

            for (String part : queryParts) {

                urlBuilder.query(part);
            }
        }

        return urlBuilder;
    }

    URLBuilder protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    URLBuilder host(String host) {
        this.host = host;
        return this;
    }

    URLBuilder port(int port) {
        this.port = port;
        return this;
    }

    URLBuilder path(String path) {
        this.paths.add(path);
        return this;
    }

    URLBuilder query(String key, String value) {
        this.query.add(key + QUERY_KEY_VALUE_SEPARATOR + value);
        return this;
    }

    URLBuilder query(String queryPart) {
        this.query.add(queryPart);
        return this;
    }

    URL build() throws MalformedURLException {

        String joinedPaths = paths.toString();
        String joinedQueries = query.toString();

        if (!joinedQueries.isEmpty()) {
            joinedPaths = joinedPaths + PATH_QUERY_SEPARATOR + joinedQueries;
        }

        return new URL(protocol, host, port, joinedPaths);
    }
}
