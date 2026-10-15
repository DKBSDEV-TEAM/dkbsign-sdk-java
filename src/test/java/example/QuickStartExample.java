package example;

import com.dkbsign.DkbSignClient;
import com.dkbsign.enums.SignatureLevel;
import com.dkbsign.enums.SignatureType;
import com.dkbsign.exception.DkbSignException;
import com.dkbsign.model.SignatureMark;
import com.dkbsign.response.OtpResponse;
import com.dkbsign.response.SelfSignResponse;
import com.dkbsign.signature.SelfSignRequest;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public final class QuickStartExample {

    private QuickStartExample() {}

    public static void main(String[] args) {
        String baseUrl = firstNonBlank(System.getenv("DKBSIGN_BASE_URL"), "https://api.dkbsigns.com");
        String token = System.getenv("DKBSIGN_TOKEN");
        if (token == null || token.isBlank()) {
            System.err.println("""
                    DKBSIGN_TOKEN is missing.
                    Obtain a Bearer token (POST /api/v1/login) or use AuthService.login() then auth.toClient(login).
                    Set DKBSIGN_TOKEN=1|… in Run Configuration → Environment variables.
                    Optional: DKBSIGN_BASE_URL (default https://api.dkbsigns.com).
                    """);
            return;
        }

        DkbSignClient client = DkbSignClient.builder()
                .baseUrl(baseUrl)
                .token(token.trim())
                .build();

        String otpRaw = args.length > 0 ? args[0] : System.getenv("DKBSIGN_OTP");
        boolean hasOtp = otpRaw != null && !otpRaw.isBlank() && !"0".equals(otpRaw.trim());

        if (!hasOtp) {
            try {
                OtpResponse otpResponse = client.selfSign().sendOtp();
                System.out.println("OTP sent: " + otpResponse.getMessage());
                if (otpResponse.getEmail() != null) {
                    System.out.println("Sent to: " + otpResponse.getEmail());
                }
                if (otpResponse.getDevOtpCode() != null) {
                    System.out.println("Test mode OTP: " + otpResponse.getDevOtpCode());
                }
                System.err.println(
                        "Run again with Program arguments = the OTP (e.g. 482913), or set DKBSIGN_OTP. "
                                + "Do not request a new OTP until signing succeeds or the code expires.");
            } catch (DkbSignException e) {
                printApiError(e);
            }
            return;
        }

        String otpCode = otpRaw.trim();
        System.out.println("Signing with OTP (" + otpCode.length() + " digits)...");

        Path pdf = requireTestResource("TEST JAVA.pdf");
        Path image = testResource("image.jpeg");

        SelfSignRequest.Builder signBuilder = SelfSignRequest.builder()
                .file(pdf)
                .level(SignatureLevel.SIMPLE)
                .mark(0, SignatureMark.at(120, 200).size(200, 70).type(SignatureType.SIGNATURE))
                .otpCode(otpCode);

        if (image != null) {
            signBuilder.signatureImage(image);
        } else {
            System.out.println("No image.jpeg in src/test/resources — signing without custom signature image.");
        }

        try {
            SelfSignResponse response = client.selfSign().sign(signBuilder.build());
            System.out.println(response.getMessage());
            System.out.println("Document UUID: " + response.getDocumentUuid());
            System.out.println("Signed PDF: " + response.getSignedPdfUrl());
            System.out.println("Verification: " + response.getVerificationUrl());
        } catch (DkbSignException e) {
            printApiError(e);
        } catch (IllegalArgumentException e) {
            System.err.println(e.getMessage());
            System.err.println("Working directory: " + Paths.get("").toAbsolutePath());
            System.err.println("Run with Working directory = project root (dkbsign-java-sdk).");
        }
    }

    /** Relative to project root: src/test/resources/{name}. */
    private static Path testResource(String name) {
        Path relative = Path.of("src", "test", "resources", name);
        if (Files.isRegularFile(relative)) {
            return relative.toAbsolutePath().normalize();
        }
        Path fromUserDir = Path.of(System.getProperty("user.dir")).resolve(relative);
        if (Files.isRegularFile(fromUserDir)) {
            return fromUserDir.normalize();
        }
        return null;
    }

    private static Path requireTestResource(String name) {
        Path path = testResource(name);
        if (path == null) {
            throw new IllegalArgumentException(
                    "PDF not found: src/test/resources/" + name
                            + " (cwd=" + Paths.get("").toAbsolutePath() + ")");
        }
        return path;
    }

    private static void printApiError(DkbSignException e) {
        System.err.println("API error HTTP " + e.getStatusCode()
                + (e.getErrorCode() != null ? " [" + e.getErrorCode() + "]" : "")
                + ": " + (e.getApiMessage() != null ? e.getApiMessage() : e.getMessage()));
        if (e.isUnauthorized()) {
            System.err.println("→ Check DKBSIGN_TOKEN (expired or invalid).");
        }
    }

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        return null;
    }
}
