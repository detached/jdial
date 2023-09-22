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

import de.w3is.jdial.protocol.model.DeviceDescriptor;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.w3is.jdial.protocol.XMLUtil.getDocumentFromStream;
import static de.w3is.jdial.protocol.XMLUtil.getTextFromSub;

/**
 * @author Simon Weis
 */
class DeviceDescriptorResourceImpl implements DeviceDescriptorResource {

    private static final Logger LOGGER = Logger.getLogger(DeviceDescriptorResourceImpl.class.getName());

    private static final String APPLICATION_URL_HEADER = "Application-URL";

    @Override
    public DeviceDescriptor getDescriptor(URL deviceDescriptorLocation) throws IOException {

        if (deviceDescriptorLocation == null) {

            throw new IllegalArgumentException("Device descriptor can't be null");
        }

        if (!deviceDescriptorLocation.getProtocol().equals("http")) {

            LOGGER.log(Level.WARNING, "Only http is supported for device descriptor resolution");
            return null;
        }

        HttpURLConnection connection = (HttpURLConnection) deviceDescriptorLocation.openConnection();

        if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {

            LOGGER.log(Level.WARNING, "Could not get device descriptor: " + connection.getResponseCode());
            return null;
        }

        String applicationUrl = connection.getHeaderField(APPLICATION_URL_HEADER);

        if (applicationUrl == null) {

            LOGGER.log(Level.WARNING, "Server didn't return applicationUrl");
            return null;
        }

        DeviceDescriptor deviceDescriptor = new DeviceDescriptor();
        deviceDescriptor.setApplicationResourceUrl(new URL(applicationUrl));

        readInfoFromBody(connection, deviceDescriptor);

        return deviceDescriptor;
    }

    private void readInfoFromBody(HttpURLConnection connection, DeviceDescriptor deviceDescriptor) throws IOException {

        try (InputStream inputStream = connection.getInputStream()) {

            Document bodyDocument = getDocumentFromStream(inputStream);

            bodyDocument.getDocumentElement().normalize();

            deviceDescriptor.setFriendlyName(getTextFromSub(bodyDocument, "friendlyName"));

        } catch (ParserConfigurationException | SAXException e) {

            LOGGER.log(Level.WARNING, "Error while parsing device descriptor:", e);
        }
    }
}
