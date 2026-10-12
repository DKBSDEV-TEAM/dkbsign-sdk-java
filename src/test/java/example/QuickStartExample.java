package example;

import com.dkbsign.DkbSignClient;
import com.dkbsign.enums.SignatureLevel;
import com.dkbsign.enums.SignatureType;
import com.dkbsign.model.SignatureMark;
import com.dkbsign.response.OtpResponse;
import com.dkbsign.response.SelfSignResponse;
import com.dkbsign.signature.SelfSignRequest;

import java.nio.file.Path;


public final class QuickStartExample {

    private QuickStartExample() {}

    public static void main(String[] args) {
        String baseUrl = System.getenv("DKBSIGN_BASE_URL");
        String token = System.getenv("DKBSIGN_TOKEN");
        if (baseUrl == null || token == null) {
            System.err.println("Set DKBSIGN_BASE_URL and DKBSIGN_TOKEN to run this example.");
            return;
        }

        DkbSignClient client = DkbSignClient.builder()
                .baseUrl(baseUrl)
                .token(token)
                .build();

        String otpRaw = args.length > 0 ? args[0] : System.getenv("DKBSIGN_OTP");
        boolean hasOtp = otpRaw != null && !otpRaw.isBlank() && !"0".equals(otpRaw.trim());

        if (!hasOtp) {
            OtpResponse otpResponse = client.selfSign().sendOtp();
            System.out.println("OTP sent: " + otpResponse.getMessage());
            if (otpResponse.getEmail() != null) {
                System.out.println("Sent to: " + otpResponse.getEmail());
            }
            if (otpResponse.getDevOtpCode() != null) {
                System.out.println("Test mode OTP: " + otpResponse.getDevOtpCode());
            }
            System.err.println(
                    "Then Run again with Program arguments = the OTP (e.g. 482913), or set DKBSIGN_OTP. "
                            + "Do not Run this step again until signing succeeds or the code expires.");
            return;
        }

        String otpCode = otpRaw.trim();
        System.out.println("Signing with OTP (" + otpCode.length() + " digits)...");

        SelfSignRequest request = SelfSignRequest.builder()
                .file(Path.of("src/test/resources/Memoire.pdf"))
                .signatureImage(Path.of("src/test/resources/image.png"))
                .level(SignatureLevel.SIMPLE)
                .mark(0, SignatureMark.at(120, 200).size(200, 70).type(SignatureType.SIGNATURE))
                .otpCode(otpCode) // exact string from email/SMS
                .build();

        SelfSignResponse response = client.selfSign().sign(request);

        System.out.println(response.getMessage());
        System.out.println("Document UUID: " + response.getDocumentUuid());
        System.out.println("Signed PDF: " + response.getSignedPdfUrl());
        System.out.println("Verification: " + response.getVerificationUrl());
    }
}
