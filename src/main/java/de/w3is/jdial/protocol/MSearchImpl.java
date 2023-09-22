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

import de.w3is.jdial.model.DialServer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Simon Weis
 */
class MSearchImpl implements MSearch {

    private static final Logger LOGGER = Logger.getLogger(MSearchImpl.class.getName());

    private static final String MULTICAST_IP = "239.255.255.250";
    private static final int MULTICAST_PORT = 1900;

    private static final String SEARCH_TARGET_HEADER_VALUE = "urn:dial-multiscreen-org:service:dial:1";
    private static final String SEARCH_TARGET_HEADER = "ST";
    private static final String LOCATION_HEADER = "LOCATION";
    private static final String USN_HEADER = "USN";
    private static final String WAKEUP_HEADER = "WAKEUP";
    private static final String SERVER_HEADER = "SERVER";
    private static final String WOL_MAC = "MAC";
    private static final String WOL_TIMEOUT = "TIMEOUT";

    private final String msearchRequest;
    private final int socketTimeoutMs;

    MSearchImpl(int responseDelay, int socketTimeoutMs) {

        this.msearchRequest = "M-SEARCH * HTTP/1.1\r\n" +
                "HOST: " + MULTICAST_IP + ":" + MULTICAST_PORT + "\r\n" +
                "MAN: \"ssdp:discover\"\r\n" +
                "MX: " + responseDelay + "\r\n" +
                SEARCH_TARGET_HEADER + ": " + SEARCH_TARGET_HEADER_VALUE + "\r\n" +
                "USER-AGENT: OS/version product/version\r\n\r\n";

        this.socketTimeoutMs = socketTimeoutMs;
    }

    @Override
    public List<DialServer> sendAndReceive() throws IOException {

        InetAddress inetAddress = InetAddress.getByName(MULTICAST_IP);

        byte[] requestBuffer = msearchRequest.getBytes(StandardCharsets.UTF_8);

        DatagramPacket requestPacket = new DatagramPacket(requestBuffer, requestBuffer.length, inetAddress, MULTICAST_PORT);

        MulticastSocket socket = new MulticastSocket(MULTICAST_PORT);
        socket.setReuseAddress(true);
        socket.setSoTimeout(socketTimeoutMs);
        socket.joinGroup(inetAddress);

        LOGGER.log(Level.FINE, "Send M-SEARCH request");
        socket.send(requestPacket);

        Map<String, DialServer> discoveredDevicesByNames = new HashMap<>();

        try {
            while (true) {

                byte[] responseBuffer = new byte[1024];
                DatagramPacket responsePacket = new DatagramPacket(responseBuffer, responseBuffer.length);
                socket.receive(responsePacket);

                DialServer dialServer = toServer(responsePacket);

                if (dialServer != null) {
                    if (!discoveredDevicesByNames.containsKey(dialServer.getUniqueServiceName())) {
                        LOGGER.log(Level.FINE, "Found device: " + dialServer.toString());
                        discoveredDevicesByNames.put(dialServer.getUniqueServiceName(), dialServer);
                    }
                }

            }
        } catch (SocketTimeoutException e) {

            LOGGER.log(Level.FINER, "Socket timed out: ", e);
        }

        return new ArrayList<>(discoveredDevicesByNames.values());
    }

    private DialServer toServer(DatagramPacket packet) {

        String data = new String(packet.getData(), StandardCharsets.UTF_8);

        if (!data.contains(SEARCH_TARGET_HEADER_VALUE)) {

            LOGGER.log(Level.FINER, "Ignore response for unrelated search target: " + data);
            return null;
        }

        String[] dataRows = data.split("\n");
        DialServer dialServer = new DialServer();

        for (String row : dataRows) {

            String[] headerParts = row.split(": ");

            if (headerParts.length == 2) {

                String headerName = headerParts[0].toUpperCase();

                switch (headerName) {
                    case LOCATION_HEADER:
                        parseDeviceDescriptorUrl(dialServer, headerParts[1]);
                        break;
                    case USN_HEADER:
                        dialServer.setUniqueServiceName(headerParts[1]);
                        break;
                    case WAKEUP_HEADER:
                        parseWolHeader(dialServer, headerParts[1]);
                        break;
                    case SERVER_HEADER:
                        dialServer.setServerDescription(headerParts[1]);
                        break;
                    default:
                        LOGGER.log(Level.FINE, "Ignoring unknown header: " + headerName);
                }
            }
        }

        if (dialServer.getDeviceDescriptorUrl() != null
                && dialServer.getUniqueServiceName() != null && dialServer.getUniqueServiceName().length() > 0) {

            return dialServer;
        } else {

            LOGGER.log(Level.FINER, "Ignore package with incomplete data: " + data);
            return null;
        }
    }

    private void parseDeviceDescriptorUrl(DialServer dialServer, String headerPart) {
        try {
            dialServer.setDeviceDescriptorUrl(new URL(headerPart));
        } catch (MalformedURLException e) {
            LOGGER.log(Level.WARNING, "Server provided malformed device descriptor url: ", e);
        }
    }

    private void parseWolHeader(DialServer dialServer, String headerValue) {

        String[] wolParts = headerValue.split(";");

        for (String wolPart : wolParts) {

            String[] wolHeader = wolPart.split("=");

            if (wolHeader.length == 2) {

                switch (wolHeader[0].toUpperCase()) {
                    case WOL_MAC:
                        dialServer.setWakeOnLanMAC(wolHeader[1]);
                        dialServer.setWakeOnLanSupport(true);
                        break;
                    case WOL_TIMEOUT:
                        dialServer.setWakeOnLanTimeout(Integer.parseInt(wolHeader[1]));
                        break;
                    default:
                        LOGGER.log(Level.FINE, "Ignore unknown wol header: " + wolHeader[0]);
                }
            }
        }
    }
}
