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

import de.w3is.jdial.model.DialServer;
import de.w3is.jdial.protocol.ProtocolFactory;
import de.w3is.jdial.protocol.model.DeviceDescriptor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Discovery can be used to find dial servers in the local network
 *
 * @author Simon Weis
 */
public class Discovery {

    private static final Logger LOGGER = Logger.getLogger(Discovery.class.getName());
    private final ProtocolFactory protocolFactory;

    public Discovery(ProtocolFactory protocolFactory) {

        this.protocolFactory = protocolFactory;
    }

    public Discovery() {
        this(ProtocolFactory.createInstance());
    }

    /**
     * The discover method returns all servers in the local network that support
     * discovery via udp msearch and support the upnp device descriptor.
     *
     * IOExceptions are not thrown to the user of this method. Instead an empty list
     * will be returned.
     *
     * @return Returns a list of discovered servers.
     */
    public List<DialServer> discover() {

        List<DialServer> dialServers;

        try {

            dialServers = protocolFactory.createMSearch().sendAndReceive();

        } catch (IOException e) {

            LOGGER.log(Level.WARNING, "IOException while discovering devices:", e);
            return Collections.emptyList();
        }

        List<DialServer> devicesToRemove = new ArrayList<>();

        for (DialServer device : dialServers) {

            try {

                Optional<DeviceDescriptor> optionalDescriptor
                        = protocolFactory.createDeviceDescriptorResource().getDescriptor(device.getDeviceDescriptorUrl());

                if (optionalDescriptor.isPresent()) {

                    DeviceDescriptor descriptor = optionalDescriptor.get();

                    device.setFriendlyName(descriptor.getFriendlyName());
                    device.setApplicationResourceUrl(descriptor.getApplicationResourceUrl());
                } else {

                    devicesToRemove.add(device);
                }

            } catch (IOException e) {

                devicesToRemove.add(device);
                LOGGER.log(Level.WARNING, "IOException while reading device descriptor " + device.getDeviceDescriptorUrl(), e);
            }
        }

        dialServers.removeAll(devicesToRemove);

        return dialServers;
    }
}
