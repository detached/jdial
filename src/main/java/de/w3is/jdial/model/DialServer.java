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

import java.io.Serializable;
import java.net.URL;

/**
 * The representation of a remote dial server
 *
 * @author Simon Weis
 */
@Data
public class DialServer implements Serializable {

    // The friendly name is only set if the device exposes it via upnp device descriptor
    private String friendlyName;

    // The url to the application rest resource
    private URL applicationResourceUrl;

    // A unique identifier of the device
    private String uniqueServiceName;

    // The url to the upnp device descriptor
    private URL deviceDescriptorUrl;

    // Set if the server supports wol
    private boolean wakeOnLanSupport;

    // The MAC address to wake up the device
    private String wakeOnLanMAC;

    // The wake on lan timeout.
    private Integer wakeOnLanTimeout;

    // A technical description string of the server
    private String serverDescription;
}
