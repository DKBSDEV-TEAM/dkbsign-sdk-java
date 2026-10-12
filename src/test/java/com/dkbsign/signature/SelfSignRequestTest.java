package com.dkbsign.signature;

import com.dkbsign.model.SignPage;
import com.dkbsign.model.SignatureMark;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelfSignRequestTest {

    @TempDir
    Path dir;

    @Test
    void groupsMarksByPageAndKeepsPagelessMarksSeparate() throws IOException {
        SelfSignRequest request = SelfSignRequest.builder()
                .file(file("doc.pdf", 10))
                .mark(0, SignatureMark.at(10, 10))
                .mark(0, SignatureMark.at(20, 20))
                .mark(SignatureMark.anchor("{{signature}}"))
                .build();

        List<SignPage> pages = request.pages();
        assertEquals(2, pages.size());
        assertEquals(0, pages.get(0).getPage());
        assertEquals(2, pages.get(0).getSignatures().size());
        assertNull(pages.get(1).getPage());
    }

    @Test
    void blankCodesAreTreatedAsAbsentAndOthersAreTrimmed() throws IOException {
        SelfSignRequest request = SelfSignRequest.builder()
                .file(file("doc.pdf", 10))
                .mark(0, SignatureMark.at(10, 10))
                .otpCode("  ")
                .mfaCode(" 123456 ")
                .build();

        assertNull(request.otpCode());
        assertEquals("123456", request.mfaCode());
        assertNull(request.secondOtpCode());
        assertNull(request.level());
        assertNull(request.signatureImage());
    }

    @Test
    void requiresAFileAndAMark() throws IOException {
        Path pdf = file("doc.pdf", 10);

        assertThrows(IllegalStateException.class,
                () -> SelfSignRequest.builder().mark(0, SignatureMark.at(1, 1)).build());
        assertThrows(IllegalStateException.class,
                () -> SelfSignRequest.builder().file(pdf).build());
    }

    @Test
    void checksFileExtensionsAndExistence() throws IOException {
        Path pdf = file("doc.pdf", 10);
        Path txt = file("doc.txt", 10);

        assertThrows(IllegalArgumentException.class, () -> SelfSignRequest.builder().file(txt));
        assertThrows(IllegalArgumentException.class, () -> SelfSignRequest.builder().signatureImage(pdf));
        assertThrows(IllegalArgumentException.class,
                () -> SelfSignRequest.builder().file(dir.resolve("missing.pdf")));
    }

    @Test
    void checksFileSizes() throws IOException {
        Path bigPdf = file("big.pdf", 10L * 1024 * 1024 + 1);
        Path bigImage = file("big.png", 2L * 1024 * 1024 + 1);

        IllegalArgumentException error =
                assertThrows(IllegalArgumentException.class, () -> SelfSignRequest.builder().file(bigPdf));
        assertTrue(error.getMessage().contains("exceeds 10 MB"));
        assertThrows(IllegalArgumentException.class, () -> SelfSignRequest.builder().signatureImage(bigImage));
    }

    @Test
    void fileReplacesWhileAddFileAppends() throws IOException {
        Path a = file("a.pdf", 10);
        Path b = file("b.pdf", 10);

        SelfSignRequest replaced = SelfSignRequest.builder()
                .addFile(a).file(b).mark(0, SignatureMark.at(1, 1)).build();
        SelfSignRequest appended = SelfSignRequest.builder()
                .addFile(a).addFile(b).mark(0, SignatureMark.at(1, 1)).build();

        assertEquals(List.of(b), replaced.files());
        assertEquals(List.of(a, b), appended.files());
    }

    @Test
    void limitsTheNumberOfFiles() throws IOException {
        Path pdf = file("doc.pdf", 10);
        List<Path> tooMany = new ArrayList<>();
        for (int i = 0; i < 101; i++) {
            tooMany.add(pdf);
        }

        assertThrows(IllegalArgumentException.class, () -> SelfSignRequest.builder().files(tooMany));
    }

    @Test
    void filesKeepsPreviousListWhenOneFileIsInvalid() throws IOException {
        Path a = file("a.pdf", 10);
        SelfSignRequest.Builder builder = SelfSignRequest.builder().addFile(a);

        assertThrows(IllegalArgumentException.class,
                () -> builder.files(List.of(a, dir.resolve("missing.pdf"))));

        SelfSignRequest request = builder.mark(0, SignatureMark.at(1, 1)).build();
        assertEquals(List.of(a), request.files());
    }

    private Path file(String name, long size) throws IOException {
        Path path = Files.createFile(dir.resolve(name));
        try (RandomAccessFile raf = new RandomAccessFile(path.toFile(), "rw")) {
            raf.setLength(size);
        }
        return path;
    }
}
