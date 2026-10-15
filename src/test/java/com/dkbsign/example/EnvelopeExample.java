package com.dkbsign.example;

import com.dkbsign.DkbSignClient;
import com.dkbsign.auth.AuthService;
import com.dkbsign.auth.LoginResult;
import com.dkbsign.auth.MfaChallenge;
import com.dkbsign.auth.MfaMethod;
import com.dkbsign.envelope.CreateEnvelopeRequest;
import com.dkbsign.envelope.CreateEnvelopeResponse;
import com.dkbsign.envelope.EnvelopeSigner;
import com.dkbsign.model.SignatureMark;

import java.nio.file.Path;

/**
 * Creates an envelope after login. Set {@code DKBSIGN_EMAIL} and {@code DKBSIGN_PASSWORD},
 * or pass email and password as program arguments. For MFA email, pass the OTP as a third argument
 * or set {@code DKBSIGN_MFA_CODE}.
 */
public final class EnvelopeExample {

    private EnvelopeExample() {}

    public static void main(String[] args) {
        String baseUrl = env("DKBSIGN_BASE_URL", "https://api.dkbsigns.com");
        String email = arg(args, 0, "DKBSIGN_EMAIL");
        String password = arg(args, 1, "DKBSIGN_PASSWORD");
        if (email == null || password == null) {
            System.err.println("Set DKBSIGN_EMAIL and DKBSIGN_PASSWORD (or pass as args 0 and 1).");
            return;
        }

        AuthService auth = AuthService.create(baseUrl);
        LoginResult login = auth.login(email, password);

        if (login.isMfaRequired()) {
            MfaChallenge challenge = login.getMfaChallenge();
            String code = arg(args, 2, "DKBSIGN_MFA_CODE");
            if (code == null) {
                if (challenge.getMethods().contains(MfaMethod.EMAIL)) {
                    auth.sendMfaEmail(challenge.getChallengeToken());
                    System.err.println("MFA email sent to " + challenge.getEmailHint()
                            + ". Run again with the code as arg 2 or set DKBSIGN_MFA_CODE.");
                } else {
                    System.err.println("Enter TOTP as arg 2 or set DKBSIGN_MFA_CODE.");
                }
                return;
            }
            MfaMethod method = challenge.getMethods().contains(MfaMethod.TOTP)
                    ? MfaMethod.TOTP
                    : MfaMethod.EMAIL;
            login = auth.verifyMfa(challenge.getChallengeToken(), method, code);
        }

        DkbSignClient client = auth.toClient(login);
        Path pdf = Path.of("src/test/resources/TEST JAVA.pdf");

        CreateEnvelopeResponse created = client.envelopes().create(CreateEnvelopeRequest.builder()
                .document(pdf)
                .title("SDK test envelope")
                .message("Please sign")
                .signer(EnvelopeSigner.builder(email)
                        .mark(0, 0, SignatureMark.at(120, 200).size(200, 70))
                        .build())
                .build());

        System.out.println(created.getMessage());
        System.out.println("Batch: " + created.getBatchId());
        for (CreateEnvelopeResponse.SignerResult signer : created.getSigners()) {
            System.out.println("Signer " + signer.getEmail() + " → " + signer.getSigningUrl());
            if (signer.getDevOtpCode() != null) {
                System.out.println("  Test OTP: " + signer.getDevOtpCode());
            }
        }
    }

    private static String env(String name, String defaultValue) {
        String value = System.getenv(name);
        return value != null && !value.isBlank() ? value : defaultValue;
    }

    private static String arg(String[] args, int index, String envName) {
        if (args.length > index && args[index] != null && !args[index].isBlank()) {
            return args[index].trim();
        }
        String env = System.getenv(envName);
        return env != null && !env.isBlank() ? env.trim() : null;
    }
}
