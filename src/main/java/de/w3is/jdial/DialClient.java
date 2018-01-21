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
import de.w3is.jdial.protocol.ProtocolFactoryImpl;
import lombok.Data;

/**
 * The DialClient is the reusable factory for creating connections to a dialServer.
 *
 * @author Simon Weis
 */
@Data
public class DialClient {

    private final ProtocolFactory protocolFactory;

    private String clientFriendlyName = "jdial";

    public DialClient(ProtocolFactory protocolFactory) {

        this.protocolFactory = protocolFactory;
    }

    public DialClient() {
        this(new ProtocolFactoryImpl(false));
    }

    /**
     * Creates a connection to a dial server.
     *
     * @param dialServer The server to connect to.
     * @return A new connection.
     */
    public DialClientConnection connectTo(DialServer dialServer) {

        return new DialClientConnection(protocolFactory.createApplicationResource(clientFriendlyName,
                dialServer.getApplicationResourceUrl()));
    }
}
