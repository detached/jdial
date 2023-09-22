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

package de.w3is.jdial;

import java.io.IOException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.w3is.jdial.model.Application;
import de.w3is.jdial.model.DialClientException;
import de.w3is.jdial.model.DialContent;
import de.w3is.jdial.model.State;
import de.w3is.jdial.protocol.ApplicationResource;
import de.w3is.jdial.protocol.model.ApplicationResourceException;

/**
 * A connection for interacting with a dial server.
 *
 * @author Simon Weis
 */
public class DialClientConnection {

    private static final Logger LOGGER = Logger.getLogger(DialClientConnection.class.getName());

    private final ApplicationResource applicationResource;

    DialClientConnection(ApplicationResource applicationResource) {
        this.applicationResource = applicationResource;
    }

    /**
     * Tests if the server supports the application.
     *
     * @param applicationName The name of the application.
     * @return True if the server supports the application.
     */
    public boolean supportsApplication(String applicationName) {

        return getApplication(applicationName) != null;
    }

    /**
     * Returns an Application instance if the app is supported.
     *
     * @param applicationName The name of the application
     * @return An instance of the Application
     */
    public Application getApplication(String applicationName) {

        try {

            return applicationResource.getApplication(applicationName);
        } catch (IOException e) {

            LOGGER.log(Level.WARNING, "IOException while getting application", e);
            return null;
        }
    }

    /**
     * Start an application
     *
     * @param application An application instance
     * @return An url to the started instance if the server provides one
     * @throws DialClientException In case of an network or protocol error
     */
    public URL startApplication(Application application) throws DialClientException {

        return startApplication(application.getName());
    }

    /**
     * Starts an application and provide additional data to send to the server
     * @param application The application to start
     * @param dialContent The additional data to send
     * @return An url to the started instance if the server provides one
     * @throws DialClientException In case of an network or protocol error
     */
    public URL startApplication(Application application, DialContent dialContent) throws DialClientException {

        return startApplication(application.getName(), dialContent);
    }

    /**
     * Start an application by name
     *
     * @param applicationName The name of the application
     * @return An url to the started instance if the server provides one
     * @throws DialClientException In case of an network or protocol error
     */
    public URL startApplication(String applicationName) throws DialClientException {

        try {
            return applicationResource.startApplication(applicationName);

        } catch (IOException | ApplicationResourceException e) {

            LOGGER.log(Level.WARNING, "Exception while starting application", e);
            throw new DialClientException(e);
        }
    }

    /**
     * Start an application by name and provide additional data to send to the server
     *
     * @param applicationName The name of the application
     * @param dialContent The additional data to send
     * @return An url to the started instance if the server provides one
     * @throws DialClientException In case of an network or protocol error
     */
    public URL startApplication(String applicationName, DialContent dialContent) throws DialClientException {

        try {

            return applicationResource.startApplication(applicationName, dialContent);

        } catch (IOException | ApplicationResourceException e) {

            LOGGER.log(Level.WARNING, "Exception while starting application", e);
            throw new DialClientException(e);
        }
    }

    /**
     * Stop an application
     *
     * @param instanceUrl An url to the app instance
     * @throws DialClientException In case of an network or protocol error
     */
    public void stopApplication(URL instanceUrl) throws DialClientException {

        if (instanceUrl == null) {
            return;
        }

        try {

            applicationResource.stopApplication(instanceUrl);

        } catch (IOException | ApplicationResourceException e) {

            LOGGER.log(Level.WARNING, "Exception while stopping the application", e);
            throw new DialClientException(e);
        }
    }

    /**
     * Stop an application that is not in the stopped state and supports stopping
     *
     * @param application The application to stop
     * @throws DialClientException In case of an network or protocol error or when the application
     * does not support stopping
     */
    public void stopApplication(Application application) throws DialClientException {

        if (application.isAllowStop()) {
            throw new DialClientException("The application doesn't support stopping");
        }

        if (application.getState() == State.STOPPED) {

            return;
        }

        stopApplication(application.getInstanceUrl());
    }

    /**
     * Hide an application
     *
     * @param application An application instance
     * @throws DialClientException In case of an network or protocol error
     */
    public void hideApplication(Application application) throws DialClientException {

        if (application.getState() == State.STOPPED || application.getState() == State.HIDDEN) {

            return;
        }

        hideApplication(application.getInstanceUrl());
    }

    /**
     * Hide an application by url
     * @param instanceUrl The url of the app instance
     * @throws DialClientException In case of an network or protocol error
     */
    private void hideApplication(URL instanceUrl) throws DialClientException {

        if (instanceUrl == null) {
            return;
        }

        try {

            applicationResource.hideApplication(instanceUrl);

        } catch (IOException | ApplicationResourceException e) {

            LOGGER.log(Level.WARNING, "Exception while hiding the application", e);
            throw new DialClientException(e);
        }
    }
}
