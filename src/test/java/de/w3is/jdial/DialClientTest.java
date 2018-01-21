package de.w3is.jdial;

import com.github.tomakehurst.wiremock.WireMockServer;
import de.w3is.jdial.model.*;
import de.w3is.jdial.protocol.ProtocolFactoryImpl;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(LoggingExtension.class)
class DialClientTest {

    private static final int SERVER_PORT = 8080;
    private static WireMockServer MOCK_SERVER;

    private static final String CLIENT_FRIENDLY_NAME = "testApplication";

    @BeforeAll
    static void beforeAll() {

        MOCK_SERVER = new WireMockServer(wireMockConfig().port(SERVER_PORT));
        MOCK_SERVER.start();
    }

    @Test
    void testGetApplication() throws Exception {

        byte[] body = Files.readAllBytes(Paths.get(DialClientTest.class.getResource("/application.xml").toURI()));

        MOCK_SERVER.stubFor(get(urlPathEqualTo("/resource/app"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Length", String.valueOf(body.length))
                        .withBody(body)));

        DialClientConnection connection = getConnectionToMock();

        Optional<Application> application = connection.getApplication("app");

        assertThat(application).isPresent().hasValueSatisfying((app) -> {
           assertThat(app.getState()).isEqualTo(State.STOPPED);
           assertThat(app.getInstanceUrl().toString()).isEqualTo("http://localhost:8080/resource/applicationName/run");
           assertThat(app.getName()).isEqualTo("applicationName");
           assertThat(app.getInstallUrl()).isNull();
           assertThat(app.getAdditionalData()).isNotNull();
        });

        MOCK_SERVER.verify(getRequestedFor(urlPathEqualTo("/resource/app"))
                .withQueryParam("clientDialVersion", equalTo("2.1")));
    }

    @Test
    void testGetApplicationLegacySupport() throws Exception {

        byte[] body = Files.readAllBytes(Paths.get(DialClientTest.class.getResource("/application.xml").toURI()));

        MOCK_SERVER.stubFor(get(urlPathEqualTo("/resource/app"))
                .willReturn(aResponse().withStatus(200)
                        .withHeader("Content-Length", String.valueOf(body.length))
                        .withBody(body)));

        DialClientConnection connection = getLegacyConnectionToMock();

        Optional<Application> application = connection.getApplication("app");

        assertThat(application).isPresent().hasValueSatisfying((app) -> {
            assertThat(app.getState()).isEqualTo(State.STOPPED);
            assertThat(app.getInstanceUrl().toString()).isEqualTo("http://localhost:8080/resource/applicationName/run");
            assertThat(app.getName()).isEqualTo("applicationName");
            assertThat(app.getInstallUrl()).isNull();
            assertThat(app.getAdditionalData()).isNotNull();
        });

        MOCK_SERVER.verify(getRequestedFor(urlPathEqualTo("/resource/app")));
    }

    @Test
    void testGetApplicationNotFound() throws Exception {

        MOCK_SERVER.stubFor(get(urlPathEqualTo("/resource/app"))
                .willReturn(aResponse().withStatus(404)));

        DialClientConnection connection = getConnectionToMock();

        assertThat(connection.getApplication("app")).isNotPresent();
    }

    @Test
    void testStartApplicationWithoutPayload() throws Exception {

        String instanceUrl = "http://localhost:" + SERVER_PORT + "/resource/app/run";

        MOCK_SERVER.stubFor(post(urlPathEqualTo("/resource/app"))
                .willReturn(aResponse().withStatus(201)
                        .withHeader("LOCATION", instanceUrl)));

        DialClientConnection connection = getConnectionToMock();

        Optional<URL> result = connection.startApplication("app");

        assertThat(result).isPresent().contains(new URL(instanceUrl));

        MOCK_SERVER.verify(postRequestedFor(urlPathEqualTo("/resource/app"))
                .withHeader("Content-Length", equalTo("0"))
                .withQueryParam("friendlyName", equalTo(CLIENT_FRIENDLY_NAME)));
    }

    @Test
    void testStartApplicationServerDidntReturnLocation() throws Exception {

        MOCK_SERVER.stubFor(post(urlPathEqualTo("/resource/app"))
                .willReturn(aResponse().withStatus(201)));

        DialClientConnection connection = getConnectionToMock();

        Optional<URL> result = connection.startApplication("app");

        assertThat(result).isNotPresent();

        MOCK_SERVER.verify(postRequestedFor(urlPathEqualTo("/resource/app"))
                .withHeader("Content-Length", equalTo("0"))
                .withQueryParam("friendlyName", equalTo(CLIENT_FRIENDLY_NAME)));
    }

    @Test
    void testStartApplicationNotFound() throws Exception {

        MOCK_SERVER.stubFor(post(urlPathEqualTo("/resource/app"))
                .willReturn(aResponse().withStatus(404)));

        DialClientConnection connection = getConnectionToMock();

        assertThrows(DialClientException.class, () -> connection.startApplication("app"));
    }

    @Test
    void testStartApplicationWithoutPayloadAndLegacySupport() throws Exception {

        String instanceUrl = "http://localhost:" + SERVER_PORT + "/resource/app/run";

        MOCK_SERVER.stubFor(post(urlPathEqualTo("/resource/app"))
                .willReturn(aResponse().withStatus(201)
                        .withHeader("LOCATION", instanceUrl)));

        DialClientConnection connection = getLegacyConnectionToMock();

        Optional<URL> result = connection.startApplication("app");

        assertThat(result).isPresent().contains(new URL(instanceUrl));

        MOCK_SERVER.verify(postRequestedFor(urlPathEqualTo("/resource/app"))
                .withHeader("Content-Length", equalTo("0")));
    }

    @Test
    void testStartApplicationWithPayload() throws Exception {

        String instanceUrl = "http://localhost:" + SERVER_PORT + "/resource/app/run";

        MOCK_SERVER.stubFor(post(urlPathEqualTo("/resource/app"))
                .willReturn(aResponse().withStatus(201)
                        .withHeader("LOCATION", instanceUrl)));

        DialClientConnection connection = getConnectionToMock();

        String requestBody = "{ \"test\": \"foobaa\"}";
        String contentType = "application/json; charset=UTF-8";

        DialContent content = new DialContent() {

            @Override
            public String getContentType() {
                return contentType;
            }

            @Override
            public byte[] getData() {
                return requestBody.getBytes(StandardCharsets.UTF_8);
            }
        };

        Optional<URL> result = connection.startApplication("app", content);

        assertThat(result).isPresent().contains(new URL(instanceUrl));

        MOCK_SERVER.verify(postRequestedFor(urlPathEqualTo("/resource/app"))
                .withHeader("Content-Length", equalTo("19"))
                .withHeader("Content-Type", equalTo(contentType))
                .withQueryParam("friendlyName", equalTo(CLIENT_FRIENDLY_NAME))
                .withRequestBody(equalTo(requestBody)));
    }

    @Test
    void testStopApplication() throws Exception {

        MOCK_SERVER.stubFor(delete(urlPathEqualTo("/resource/app/run"))
                .willReturn(aResponse().withStatus(200)));

        DialClientConnection connection = getConnectionToMock();

        connection.stopApplication(new URL("http://127.0.0.1:" + SERVER_PORT + "/resource/app/run"));

        MOCK_SERVER.verify(deleteRequestedFor(urlPathEqualTo("/resource/app/run")));
    }

    @Test
    void testStopApplicationNotFound() throws Exception {

        MOCK_SERVER.stubFor(delete(urlPathEqualTo("/resource/app/run"))
                .willReturn(aResponse().withStatus(404)));

        DialClientConnection connection = getConnectionToMock();

        assertThrows(DialClientException.class,
                () -> connection.stopApplication(new URL("http://127.0.0.1:" + SERVER_PORT + "/resource/app/run")));

        MOCK_SERVER.verify(deleteRequestedFor(urlPathEqualTo("/resource/app/run")));
    }

    @Test
    void testHideApplication() throws Exception {

        MOCK_SERVER.stubFor(post(urlPathEqualTo("/resource/app/run/hide"))
                .willReturn(aResponse().withStatus(200)));

        DialClientConnection connection = getConnectionToMock();

        Application application = new Application();
        application.setInstanceUrl(new URL("http://127.0.0.1:8080/resource/app/run"));

        connection.hideApplication(application);

        MOCK_SERVER.verify(postRequestedFor(urlPathEqualTo("/resource/app/run/hide")));
    }

    @Test
    void testHideApplicationNotFound() throws Exception {

        MOCK_SERVER.stubFor(post(urlPathEqualTo("/resource/app/run/hide"))
                .willReturn(aResponse().withStatus(404)));

        DialClientConnection connection = getConnectionToMock();

        Application application = new Application();
        application.setInstanceUrl(new URL("http://127.0.0.1:8080/resource/app/run"));

        assertThrows(DialClientException.class, () -> connection.hideApplication(application));

        MOCK_SERVER.verify(postRequestedFor(urlPathEqualTo("/resource/app/run/hide")));
    }

    private DialClientConnection getConnectionToMock() throws MalformedURLException {
        DialServer dialServer = new DialServer();
        dialServer.setApplicationResourceUrl(new URL("http://localhost:" + SERVER_PORT + "/resource"));

        DialClient dialClient = new DialClient();
        dialClient.setClientFriendlyName(CLIENT_FRIENDLY_NAME);
        return dialClient.connectTo(dialServer);
    }

    private DialClientConnection getLegacyConnectionToMock() throws MalformedURLException {
        DialServer dialServer = new DialServer();
        dialServer.setApplicationResourceUrl(new URL("http://localhost:" + SERVER_PORT + "/resource"));

        DialClient dialClient = new DialClient(new ProtocolFactoryImpl(true));
        dialClient.setClientFriendlyName(CLIENT_FRIENDLY_NAME);
        return dialClient.connectTo(dialServer);
    }

    @AfterAll
    static void afterAll() {

        MOCK_SERVER.stop();
    }
}
