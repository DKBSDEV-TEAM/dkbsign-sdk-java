package com.dkbsign.envelope;

import com.dkbsign.enums.SignatureLevel;
import com.dkbsign.enums.SignatureType;
import com.dkbsign.model.SignatureMark;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnvelopePayloadTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void serializesPayloadMatchingDocumentationExample() throws Exception {
        EnvelopePayload payload = EnvelopePayload.builder()
                .title("NDA Q1")
                .message("Please sign")
                .signingOrder(SigningOrder.ORDERED)
                .signatureLevel(SignatureLevel.ADVANCED)
                .signer(EnvelopeSigner.builder("jean@example.com")
                        .name("Dupont")
                        .firstName("Jean")
                        .phone("+2250700000001")
                        .priority(1)
                        .mark(0, SignatureMark.at(120, 200)
                                .onPage(0)
                                .size(200, 70)
                                .type(SignatureType.SIGNATURE))
                        .build())
                .build();

        String json = mapper.writeValueAsString(payload);

        assertTrue(json.contains("\"title\":\"NDA Q1\""));
        assertTrue(json.contains("\"signing_order\":\"ordered\""));
        assertTrue(json.contains("\"signature_level\":\"advanced\""));
        assertTrue(json.contains("\"first_name\":\"Jean\""));
        assertTrue(json.contains("\"positions\":{\"0\":["));
        assertTrue(json.contains("\"page\":0"));
        assertTrue(json.contains("\"x\":120.0"));
    }

    @Test
    void omitsNullOptionalFields() throws Exception {
        EnvelopePayload payload = EnvelopePayload.builder()
                .title("Contract")
                .signer(EnvelopeSigner.builder("a@b.test").build())
                .build();

        String json = mapper.writeValueAsString(payload);

        assertTrue(json.contains("\"title\":\"Contract\""));
        assertTrue(!json.contains("signing_order"));
        assertTrue(!json.contains("signature_level"));
        assertTrue(!json.contains("positions"));
    }

    @Test
    void requiresTitleAndSigners() {
        assertThrows(IllegalStateException.class,
                () -> EnvelopePayload.builder().signer(EnvelopeSigner.builder("a@b.test").build()).build());
        assertThrows(IllegalStateException.class, () -> EnvelopePayload.builder().title("x").build());
        assertThrows(IllegalArgumentException.class, () -> EnvelopeSigner.builder(" "));
    }

    @Test
    void passwordSerializesWhenSet() throws Exception {
        EnvelopePayload payload = EnvelopePayload.builder()
                .title("Secret")
                .password(EnvelopePassword.userPassword("1234"))
                .signer(EnvelopeSigner.builder("a@b.test").build())
                .build();

        String json = mapper.writeValueAsString(payload);
        assertTrue(json.contains("\"password\":{\"enabled\":true,\"user_password\":\"1234\"}"));
    }
}
