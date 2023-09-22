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

import de.w3is.jdial.model.Application;
import de.w3is.jdial.model.DialContent;
import de.w3is.jdial.model.State;
import de.w3is.jdial.protocol.model.ApplicationResourceException;
import lombok.Data;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.w3is.jdial.protocol.XMLUtil.getDocumentFromStream;
import static de.w3is.jdial.protocol.XMLUtil.getTextFromSub;

/**
 * @author Simon Weis
 */
@Data
class ApplicationResourceImpl implements ApplicationResource {

    private static final Logger LOGGER = Logger.getLogger(ApplicationResourceImpl.class.getName());
    private static final String APPLICATION_DIAL_VERSION_QUERY = "clientDialVersion=2.1";
    private static final String CLIENT_FRIENDLY_NAME_QUERY = "friendlyName";
    private static final String CONTENT_LENGTH_HEADER = "Content-Length";
    private static final String CONTENT_TYPE_HEADER = "Content-Type";

    private static final DialContent NO_CONTENT = new DialContent() {
        @Override
        public String getContentType() {
            return null;
        }

        @Override
        public byte[] getData() {
            return null;
        }
    };

    private final String clientFriendlyName;
    private final URL rootUrl;
    private boolean sendQueryParameter;
    private Integer connectionTimeout;
    private Integer readTimeout;

    ApplicationResourceImpl(String clientFriendlyName, URL rootUrl) {

        this.clientFriendlyName = clientFriendlyName;
        this.rootUrl = rootUrl;
        this.sendQueryParameter = true;
    }

    @Override
    public Application getApplication(String applicationName) throws IOException {

        URLBuilder applicationUrl = URLBuilder.of(rootUrl).path(applicationName);

        if (sendQueryParameter) {

            applicationUrl.query(APPLICATION_DIAL_VERSION_QUERY);
        }

        HttpURLConnection httpUrlConnection = (HttpURLConnection) applicationUrl.build().openConnection();
        addTimeoutParameter(httpUrlConnection);
        httpUrlConnection.setDoInput(true);

        if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

            LOGGER.log(Level.FINE, "Application not found: ", httpUrlConnection.getResponseCode());
            return null;
        }

        try (InputStream inputStream = httpUrlConnection.getInputStream()) {

            Document serviceDocument = getDocumentFromStream(inputStream);

            Application application = new Application();
            application.setName(getTextFromSub(serviceDocument, "name"));
            application.setInstanceUrl(getInstanceUrl(serviceDocument, application.getName()));
            application.setAllowStop(getIsAllowStopFromOption(serviceDocument));
            application.setAdditionalData(extractAdditionalData(serviceDocument));

            extractState(serviceDocument, application);

            return application;

        } catch (ParserConfigurationException | SAXException | ApplicationResourceException e) {

            LOGGER.log(Level.WARNING, "Can't parse body xml", e);
            return null;
        }
    }

    @Override
    public URL startApplication(String applicationName) throws IOException, ApplicationResourceException {

        return startApplication(applicationName, NO_CONTENT);
    }

    @Override
    public URL startApplication(String applicationName, DialContent dialContent) throws IOException, ApplicationResourceException {

        URLBuilder applicationUrl = URLBuilder.of(rootUrl).path(applicationName);

        if (clientFriendlyName != null && sendQueryParameter) {
            applicationUrl.query(CLIENT_FRIENDLY_NAME_QUERY, clientFriendlyName);
        }

        HttpURLConnection httpURLConnection = (HttpURLConnection) applicationUrl.build().openConnection();
        httpURLConnection.setRequestMethod("POST");
        httpURLConnection.setDoOutput(true);

        addTimeoutParameter(httpURLConnection);

        if (dialContent.getData() == null) {

            httpURLConnection.setRequestProperty(CONTENT_LENGTH_HEADER, "0");

            // HttpURLConnection will not send headers if the outputStream not getting opened.
            httpURLConnection.getOutputStream().close();
        } else {

            httpURLConnection.setRequestProperty(CONTENT_LENGTH_HEADER, String.valueOf(dialContent.getData().length));
            httpURLConnection.setRequestProperty(CONTENT_TYPE_HEADER, dialContent.getContentType());

            try (OutputStream outputStream = httpURLConnection.getOutputStream()) {
                outputStream.write(dialContent.getData());
            }
        }

        int code = httpURLConnection.getResponseCode();

        if (code == HttpURLConnection.HTTP_OK || code == HttpURLConnection.HTTP_CREATED) {

            String instanceLocation = httpURLConnection.getHeaderField("LOCATION");

            if (instanceLocation != null) {

                return new URL(instanceLocation);
            } else {

                return null;
            }
        } else {

            throw new ApplicationResourceException("Could not start application. Status: " + code);
        }
    }

    @Override
    public void stopApplication(URL instanceUrl) throws IOException, ApplicationResourceException {

        HttpURLConnection httpURLConnection = (HttpURLConnection) instanceUrl.openConnection();
        addTimeoutParameter(httpURLConnection);
        httpURLConnection.setRequestMethod("DELETE");

        if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new ApplicationResourceException("Could not stop the application. Status: " +
                    httpURLConnection.getResponseCode());
        }
    }

    @Override
    public void hideApplication(URL instanceURL) throws IOException, ApplicationResourceException {

        URL hidingUrl = URLBuilder.of(instanceURL).path("hide").build();
        HttpURLConnection httpURLConnection = (HttpURLConnection) hidingUrl.openConnection();
        addTimeoutParameter(httpURLConnection);
        httpURLConnection.setRequestMethod("POST");

        if (httpURLConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {
            throw new ApplicationResourceException("Could not hide the application. Status: " +
                    httpURLConnection.getResponseCode());
        }
    }

    private Node extractAdditionalData(Document document) {

        NodeList nodes = document.getElementsByTagName("additionalData");

        if (nodes.getLength() >= 1) {

            return nodes.item(0);
        }

        return null;
    }

    private boolean getIsAllowStopFromOption(Document document) {

        NodeList nodes = document.getElementsByTagName("options");

        if (nodes.getLength() < 1) {
            return false;
        }

        NamedNodeMap optionAttributes = nodes.item(0).getAttributes();
        Node allowStop = optionAttributes.getNamedItem("allowStop");

        return allowStop != null && Boolean.parseBoolean(allowStop.getTextContent());
    }

    private URL getInstanceUrl(Document document, String applicationName) throws MalformedURLException, ApplicationResourceException {

        NodeList nodes = document.getElementsByTagName("link");

        if (nodes.getLength() < 1) {
            throw new ApplicationResourceException("Document has no link element");
        }

        NamedNodeMap linkAttributes = nodes.item(0).getAttributes();
        Node href = linkAttributes.getNamedItem("href");
        Node rel = linkAttributes.getNamedItem("rel");

        if (rel == null || href == null || !rel.getTextContent().equals("run")) {

            throw new ApplicationResourceException("Unknown link type on service");
        }

        return URLBuilder.of(rootUrl).path(applicationName).path(href.getTextContent()).build();
    }

    private void extractState(Document document, Application application) throws ApplicationResourceException, MalformedURLException {

        String stateText = getTextFromSub(document, "state");

        State state = mapToState(stateText);
        application.setState(state);

        if (state == State.INSTALLABLE) {
            application.setInstallUrl(getInstallUrl(stateText));
        }
    }

    private URL getInstallUrl(String state) throws MalformedURLException {

        String[] stateParts = state.split("=");

        if (stateParts.length < 2) {
            return null;
        }

        return new URL(stateParts[1]);
    }

    private State mapToState(String value) throws ApplicationResourceException {

        if (value == null) {
            throw new ApplicationResourceException("App exists but has no state");
        }

        String lowercaseStatus = value.toLowerCase();
        if (lowercaseStatus.startsWith("installable")) {

            return State.INSTALLABLE;
        }

        switch (lowercaseStatus) {

            case "running":
                return State.RUNNING;
            case "stopped":
                return State.STOPPED;
            case "hidden":
                return State.HIDDEN;
            default:
                throw new ApplicationResourceException("Unknown state: " + value);
        }
    }

    private void addTimeoutParameter(HttpURLConnection httpUrlConnection) {

        if (connectionTimeout != null) {
            httpUrlConnection.setConnectTimeout(connectionTimeout);
        }

        if (readTimeout != null) {
            httpUrlConnection.setReadTimeout(readTimeout);
        }
    }
}
