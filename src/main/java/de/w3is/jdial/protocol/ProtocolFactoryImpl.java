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

import lombok.Data;

import java.net.URL;

/**
 * @author Simon Weis
 */
@Data
public class ProtocolFactoryImpl implements ProtocolFactory {

    private boolean legacyCompatibility;
    private int httpClientReadTimeoutMs = 1500;
    private int httpClientConnectionTimeoutMs = 1500;
    private int socketTimeoutMs = 1500;
    private int mSearchResponseDelay = 0;

    public ProtocolFactoryImpl(boolean legacyCompatibility) {

        this.legacyCompatibility = legacyCompatibility;
    }

    @Override
    public MSearch createMSearch() {

        return new MSearchImpl(mSearchResponseDelay, socketTimeoutMs);
    }

    @Override
    public DeviceDescriptorResource createDeviceDescriptorResource() {

        return new DeviceDescriptorResourceImpl();
    }

    @Override
    public ApplicationResource createApplicationResource(String clientFriendlyName, URL applicationResourceUrl) {

        ApplicationResourceImpl applicationResource = new ApplicationResourceImpl(clientFriendlyName, applicationResourceUrl);
        applicationResource.setSendQueryParameter(!legacyCompatibility);
        applicationResource.setConnectionTimeout(httpClientConnectionTimeoutMs);
        applicationResource.setReadTimeout(httpClientReadTimeoutMs);

        return applicationResource;
    }
}
