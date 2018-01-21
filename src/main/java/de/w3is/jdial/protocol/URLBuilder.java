/*
 * Copyright (C) 2018 Simon Weis
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.w3is.jdial.protocol;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

/**
 * @author Simon Weis
 */
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
