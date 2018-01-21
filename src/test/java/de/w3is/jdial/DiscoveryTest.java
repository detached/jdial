package de.w3is.jdial;

import de.w3is.jdial.model.DialServer;
import de.w3is.jdial.protocol.ApplicationResource;
import de.w3is.jdial.protocol.DeviceDescriptorResource;
import de.w3is.jdial.protocol.MSearch;
import de.w3is.jdial.protocol.ProtocolFactory;
import de.w3is.jdial.protocol.model.DeviceDescriptor;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DiscoveryTest {

    private static final String APPLICATION_RESOURCE = "http://127.0.0.1:8080/app.xml";
    private static final String FRIENDLY_NAME = "friendlyName";
    private static final String SERVER_DESCRIPTION = "serverDescription";
    private static final String UUID = "uuid";
    private static final int WAKE_ON_LAN_TIMEOUT = 1;
    private static final String MAC = "mac";
    private static final String DEVICE_DESCRIPTOR = "http://127.0.0.1:8080/description.xml";

    @Test
    void testDiscovery() throws Exception {

        DialServer device = createSecondScreenDevice();

        DeviceDescriptor descriptor = createDeviceDescriptor();

        MSearch mSearch = mock(MSearch.class);
        when(mSearch.sendAndReceive()).thenReturn(Collections.singletonList(device));

        DeviceDescriptorResource deviceDescriptorResource = mock(DeviceDescriptorResource.class);
        when(deviceDescriptorResource.getDescriptor(device.getDeviceDescriptorUrl())).thenReturn(descriptor);

        ProtocolFactory protocolFactory = createFactoryOf(mSearch, deviceDescriptorResource);

        List<DialServer> dialServers = new Discovery(protocolFactory).discover();

        assertThat(dialServers).hasSize(1);
        assertThat(dialServers.get(0).getDeviceDescriptorUrl().toString()).isEqualTo(DEVICE_DESCRIPTOR);
        assertThat(dialServers.get(0).getUniqueServiceName()).isEqualTo(UUID);
        assertThat(dialServers.get(0).getServerDescription()).isEqualTo(SERVER_DESCRIPTION);
        assertThat(dialServers.get(0).isWakeOnLanSupport()).isTrue();
        assertThat(dialServers.get(0).getWakeOnLanMAC()).isEqualTo(MAC);
        assertThat(dialServers.get(0).getWakeOnLanTimeout()).isEqualTo(WAKE_ON_LAN_TIMEOUT);
        assertThat(dialServers.get(0).getFriendlyName()).isEqualTo(FRIENDLY_NAME);
        assertThat(dialServers.get(0).getApplicationResourceUrl().toString()).isEqualTo(APPLICATION_RESOURCE);
    }

    @Test
    void testFailedDiscovery() throws Exception {

        MSearch mSearch = mock(MSearch.class);
        when(mSearch.sendAndReceive()).thenThrow(new IOException());

        ProtocolFactory protocolFactory = createFactoryOf(mSearch, null);

        List<DialServer> dialServers = new Discovery(protocolFactory).discover();

        assertThat(dialServers).isEmpty();
    }

    @Test
    void testFailedDescriptorLookup() throws Exception {

        DialServer device1 = createSecondScreenDevice();
        device1.setDeviceDescriptorUrl(new URL("http://localhost/1"));

        DialServer device2 = createSecondScreenDevice();
        device2.setDeviceDescriptorUrl(new URL("http://localhost/2"));

        List<DialServer> dialServers = new ArrayList<>();
        dialServers.add(device1);
        dialServers.add(device2);

        DeviceDescriptor descriptor = createDeviceDescriptor();

        MSearch mSearch = mock(MSearch.class);
        when(mSearch.sendAndReceive()).thenReturn(dialServers);

        DeviceDescriptorResource deviceDescriptorResource = mock(DeviceDescriptorResource.class);
        when(deviceDescriptorResource.getDescriptor(device1.getDeviceDescriptorUrl())).thenReturn(descriptor);
        when(deviceDescriptorResource.getDescriptor(device2.getDeviceDescriptorUrl())).thenReturn(null);

        ProtocolFactory protocolFactory = createFactoryOf(mSearch, deviceDescriptorResource);

        List<DialServer> discoveredDevices = new Discovery(protocolFactory).discover();

        assertThat(discoveredDevices).hasSize(1);
        assertThat(discoveredDevices.get(0).getDeviceDescriptorUrl()).isEqualTo(device1.getDeviceDescriptorUrl());
    }

    private ProtocolFactory createFactoryOf(final MSearch mSearch, final DeviceDescriptorResource deviceDescriptorResource) {

        return new ProtocolFactory() {
            @Override
            public MSearch createMSearch() {
                return mSearch;
            }

            @Override
            public DeviceDescriptorResource createDeviceDescriptorResource() {
                return deviceDescriptorResource;
            }

            @Override
            public ApplicationResource createApplicationResource(String clientFriendlyName, URL applicationResourceUrl) {
                return null;
            }
        };
    }

    private DeviceDescriptor createDeviceDescriptor() throws MalformedURLException {

        DeviceDescriptor deviceDescriptor = new DeviceDescriptor();

        deviceDescriptor.setApplicationResourceUrl(new URL(APPLICATION_RESOURCE));
        deviceDescriptor.setFriendlyName(FRIENDLY_NAME);

        return deviceDescriptor;
    }

    private DialServer createSecondScreenDevice() throws MalformedURLException {

        DialServer dialServer = new DialServer();
        dialServer.setServerDescription(SERVER_DESCRIPTION);
        dialServer.setUniqueServiceName(UUID);
        dialServer.setWakeOnLanTimeout(WAKE_ON_LAN_TIMEOUT);
        dialServer.setWakeOnLanMAC(MAC);
        dialServer.setDeviceDescriptorUrl(new URL(DEVICE_DESCRIPTOR));
        dialServer.setWakeOnLanSupport(true);

        return dialServer;
    }
}