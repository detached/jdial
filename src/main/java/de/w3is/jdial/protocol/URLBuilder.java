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

    private final StringBuilder paths = new StringBuilder();
    private final StringBuilder query = new StringBuilder();

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

    private URLBuilder host(String host) {
        this.host = host;
        return this;
    }

    private URLBuilder port(int port) {
        this.port = port;
        return this;
    }

    URLBuilder path(String path) {

        if (paths.length() != 0) {
            this.paths.append(PATH_SEPARATOR);
        }

        this.paths.append(path);
        return this;
    }

    void query(String key, String value) {

        appendQueryOrPathSeparator();

        this.query.append(key).append(QUERY_KEY_VALUE_SEPARATOR).append(value);

    }

    void query(String queryPart) {

        appendQueryOrPathSeparator();

        this.query.append(QUERY_SEPARATOR).append(queryPart);
    }

    URL build() throws MalformedURLException {

        String joinedPath = paths + query.toString();

        return new URL(protocol, host, port, joinedPath);
    }

    private void appendQueryOrPathSeparator() {

        if (this.query.length() == 0) {

            this.query.append(PATH_QUERY_SEPARATOR);
        } else {

            this.query.append(QUERY_SEPARATOR);
        }
    }
}
