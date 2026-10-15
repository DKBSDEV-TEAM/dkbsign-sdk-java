package com.dkbsign.example;

import com.dkbsign.DkbSignClient;
import com.dkbsign.publicenvelope.PublicEnvelopeService;
import com.dkbsign.publicenvelope.PublicOtpVerifyResponse;
import com.dkbsign.publicenvelope.PublicSignRequest;
import com.dkbsign.publicenvelope.PublicSigningSession;

/**
 * Public signer flow (link from email). Set {@code DKBSIGN_SIGNER_UUID} or pass UUID as arg 0.
 * Optional OTP: arg 1 or {@code DKBSIGN_OTP}.
 */
public final class PublicSigningExample {

    private PublicSigningExample() {}

    public static void main(String[] args) {
        String baseUrl = env("DKBSIGN_BASE_URL", DkbSignClient.DEFAULT_BASE_URL);
        String signerUuid = arg(args, 0, "DKBSIGN_SIGNER_UUID");
        if (signerUuid == null) {
            System.err.println("Set DKBSIGN_SIGNER_UUID or pass signer UUID as first argument.");
            return;
        }

        PublicEnvelopeService signing = DkbSignClient.publicSigning(baseUrl, signerUuid);
        PublicSigningSession session = signing.getSession();
        System.out.println("Envelope: " + session.getTitle() + " — canSign=" + session.getCanSign());

        String otp = arg(args, 1, "DKBSIGN_OTP");
        if (otp == null) {
            signing.requestOtp();
            System.out.println("OTP sent. Re-run with OTP as second argument or DKBSIGN_OTP.");
            return;
        }

        PublicOtpVerifyResponse verified = signing.verifyOtp(otp);
        System.out.println("OTP verified: " + verified.isVerified());

        signing.sign(PublicSignRequest.builder().build());
        System.out.println("Sign request submitted.");
    }

    private static String env(String key, String defaultValue) {
        String v = System.getenv(key);
        return v != null && !v.isBlank() ? v.trim() : defaultValue;
    }

    private static String arg(String[] args, int index, String envKey) {
        if (args.length > index && args[index] != null && !args[index].isBlank()) {
            return args[index].trim();
        }
        String v = System.getenv(envKey);
        return v != null && !v.isBlank() ? v.trim() : null;
    }
}
