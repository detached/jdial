package de.w3is.jdial;

import de.w3is.jdial.model.Application;
import de.w3is.jdial.model.DialClientException;
import de.w3is.jdial.model.DialServer;
import de.w3is.jdial.model.State;
import de.w3is.jdial.protocol.ProtocolFactoryImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Tags;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Tags({@Tag("smoketest"), @Tag("example")})
@Disabled
@ExtendWith(LoggingExtension.class)
class DialApplicationTest {

    @Test
    void testStartApplication() throws DialClientException {

        DialClientConnection myTv = discoverMyTv();

        Optional<Application> youtube = myTv.getApplication(Application.YOUTUBE);
        assertThat(youtube).isPresent();

        assertThat(myTv.startApplication(youtube.get(), "{}"::getBytes)).isPresent();
    }

    @Test
    void testStopApplication() throws DialClientException {

        DialClientConnection myTv = discoverMyTv();

        Optional<Application> app = myTv.getApplication(Application.YOUTUBE);
        assertThat(app).isPresent();

        Application youtube = app.get();

        assertThat(youtube.getState()).isEqualTo(State.RUNNING);
        myTv.stopApplication(youtube);
    }

    @Test
    void testHideApplication() throws DialClientException {

        DialClientConnection myTv = discoverMyTv();

        Optional<Application> app = myTv.getApplication(Application.YOUTUBE);
        assertThat(app).isPresent();

        Application youtube = app.get();

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
