package de.w3is.jdial;

import de.w3is.jdial.protocol.model.generated.ServiceType;
import org.junit.jupiter.api.Test;

import javax.xml.bind.JAXB;

import static org.assertj.core.api.Java6Assertions.assertThat;

class JAXBTest {

    @Test
    void testUnmarshal() {

        ServiceType service = JAXB.unmarshal(JAXBTest.class.getResourceAsStream("/application.xml"), ServiceType.class);

        assertThat(service).isNotNull();
        assertThat(service.getName()).isNotNull();
        assertThat(service.getOptions()).isNotNull();
        assertThat(service.getLink()).isNotNull();
        assertThat(service.getState()).isNotNull();
        assertThat(service.getAdditionalData()).isNotNull();
        assertThat(service.getAdditionalData().getAny()).isNotEmpty();
    }
}
