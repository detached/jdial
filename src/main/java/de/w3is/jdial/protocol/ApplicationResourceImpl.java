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
import de.w3is.jdial.protocol.model.generated.LinkType;
import de.w3is.jdial.protocol.model.generated.ServiceType;
import lombok.Data;

import javax.xml.bind.JAXB;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    private static final DialContent NO_CONTENT = () -> null;

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
    public Optional<Application> getApplication(String applicationName) throws IOException {

        URLBuilder applicationUrl = URLBuilder.of(rootUrl).path(applicationName);

        if (sendQueryParameter) {

            applicationUrl.query(APPLICATION_DIAL_VERSION_QUERY);
        }

        HttpURLConnection httpUrlConnection = (HttpURLConnection) applicationUrl.build().openConnection();
        addTimeoutParameter(httpUrlConnection);
        httpUrlConnection.setDoInput(true);

        if (httpUrlConnection.getResponseCode() != HttpURLConnection.HTTP_OK) {

            LOGGER.log(Level.FINE, "Application not found: ", httpUrlConnection.getResponseCode());
            return Optional.empty();
        }

        ServiceType service = JAXB.unmarshal(httpUrlConnection.getInputStream(), ServiceType.class);

        Application application = new Application();

        try {

            application.setName(service.getName());
            application.setState(mapToState(service.getState()));

            if (application.getState() == State.INSTALLABLE) {
                application.setInstallUrl(getInstallUrl(service.getState()));
            }

            if (service.getOptions() != null) {
                application.setAllowStop(service.getOptions().isAllowStop() == null ? false : service.getOptions().isAllowStop());
            }

            if (service.getLink() != null) {
                application.setInstanceUrl(getInstanceUrl(applicationName, service.getLink()));
            }

            if (service.getAdditionalData() != null) {
                application.setAdditionalData(service.getAdditionalData().getAny());
            }

        } catch (ApplicationResourceException e) {

            LOGGER.log(Level.WARNING, "Error while parsing ApplicationResource response: ", e);
            return Optional.empty();
        }

        return Optional.of(application);
    }

    @Override
    public Optional<URL> startApplication(String applicationName) throws IOException, ApplicationResourceException {

        return startApplication(applicationName, NO_CONTENT);
    }

    @Override
    public Optional<URL> startApplication(String applicationName, DialContent dialContent) throws IOException, ApplicationResourceException {

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

                return Optional.of(new URL(instanceLocation));
            } else {

                return Optional.empty();
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

    private URL getInstanceUrl(String applicationName, LinkType link) throws MalformedURLException {

        if (!"run".equals(link.getRel())) {

            LOGGER.log(Level.WARNING, "Unknown link type on service: " + link.getRel());
            return null;
        }

        return URLBuilder.of(rootUrl).path(applicationName).path(link.getHref()).build();
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
