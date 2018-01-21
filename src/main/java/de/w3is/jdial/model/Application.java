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

package de.w3is.jdial.model;

import lombok.Data;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.net.URL;

/**
 * The Application class represents an app that can be
 * run on a first-screen device.
 *
 * For all reserved DIAL application names see
 * http://www.dial-multiscreen.org/dial-registry/namespace-database
 *
 * @author Simon Weis
 */
@Data
public class Application implements Serializable {

    public static final String NETFLIX = "Netflix";
    public static final String YOUTUBE = "YouTube";
    public static final String AMAZON_INSTANT_VIDEO = "AmazonInstantVideo";

    // The name of the application
    private String name;

    // The state of the application
    private State state;

    // True if the client is allowed to stop the app
    private boolean allowStop;

    // The installUrl can be used to issue an installation of the app.
    private URL installUrl;

    /*
     * The url of a running instance.
     * The installUrl is null when no instance is running.
     */
    private URL instanceUrl;

    // Additional data defined by the app author.
    private Node additionalData;
}
