package de.w3is.jdial;

import de.w3is.jdial.model.*;
import de.w3is.jdial.protocol.ProtocolFactoryImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.charset.Charset;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Tags({@Tag("smoketest"), @Tag("example")})
@Disabled
@ExtendWith(LoggingExtension.class)
class DialApplicationTest {

    @Test
    void testStartApplication() throws DialClientException {

        DialClientConnection myTv = discoverMyTv();

        Application youtube = myTv.getApplication(Application.YOUTUBE);
        assertThat(youtube).isNotNull();

        DialContent content = new DialContent() {
            @Override
            public String getContentType() {
                return "application/json; encoding=UTF-8";
            }

            @Override
            public byte[] getData() {
                return "{}".getBytes(Charset.forName("UTF-8"));
            }
        };

        assertThat(myTv.startApplication(youtube, content)).isNotNull();
    }

    @Test
    void testStopApplication() throws DialClientException {

        DialClientConnection myTv = discoverMyTv();

        Application youtube = myTv.getApplication(Application.YOUTUBE);
        assertThat(youtube).isNotNull();

        assertThat(youtube.getState()).isEqualTo(State.RUNNING);
        myTv.stopApplication(youtube);
    }

    @Test
    void testHideApplication() throws DialClientException {

        DialClientConnection myTv = discoverMyTv();

        Application youtube = myTv.getApplication(Application.YOUTUBE);
        assertThat(youtube).isNotNull();

        assertThat(youtube.getState()).isEqualTo(State.RUNNING);

        myTv.hideApplication(youtube);
    }

    private DialClientConnection discoverMyTv() {
        Discovery discovery = new Discovery();
        List<DialServer> devices = discovery.discover();

        assertThat(devices).hasSize(1);

        DialServer dialServer = devices.get(0);

        DialClient dialClient = new DialClient(new ProtocolFactoryImpl(true));

        return dialClient.connectTo(dialServer);
    }
}
