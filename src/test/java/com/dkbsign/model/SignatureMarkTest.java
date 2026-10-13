package com.dkbsign.model;

import com.dkbsign.enums.SignatureType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SignatureMarkTest {

    private final ObjectMapper mapper = new ObjectMapper();

    @Test
    void coordinateMarkSerializesOnlyFilledFields() throws Exception {
        SignatureMark mark = SignatureMark.at(120, 200).size(200, 70).type(SignatureType.SIGNATURE);

        assertEquals(
                "{\"x\":120.0,\"y\":200.0,\"width\":200.0,\"height\":70.0,\"type\":\"signature\"}",
                mapper.writeValueAsString(mark));
    }

    @Test
    void anchorMarkSerializesAnchorPageAndOccurrence() throws Exception {
        SignatureMark mark = SignatureMark.anchor("{{signature}}").onPage(1).occurrence(0);

        assertEquals(
                "{\"anchor\":\"{{signature}}\",\"page\":1,\"occurrence\":0}",
                mapper.writeValueAsString(mark));
    }

    @Test
    void withersReturnNewInstances() {
        SignatureMark original = SignatureMark.at(10, 10);
        SignatureMark sized = original.size(50, 20);

        assertNull(original.getWidth());
        assertEquals(50.0, sized.getWidth());
    }

    @Test
    void rejectsInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> SignatureMark.at(-1, 0));
        assertThrows(IllegalArgumentException.class, () -> SignatureMark.at(0, 0).size(0, 10));
        assertThrows(IllegalArgumentException.class, () -> SignatureMark.anchor(" "));
        assertThrows(IllegalArgumentException.class, () -> SignatureMark.anchor("x").onPage(-1));
        assertThrows(NullPointerException.class, () -> SignatureMark.at(0, 0).type(null));
    }

    @Test
    void coordinateMarkCanSetPage() throws Exception {
        SignatureMark mark = SignatureMark.at(10, 10).onPage(2);
        assertEquals("{\"x\":10.0,\"y\":10.0,\"page\":2}", mapper.writeValueAsString(mark));
    }

    @Test
    void occurrenceOnlyAppliesToAnchors() {
        assertThrows(IllegalStateException.class, () -> SignatureMark.at(10, 10).occurrence(0));
    }
}
