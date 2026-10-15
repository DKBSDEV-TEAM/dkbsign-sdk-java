# DKBSign Java SDK

Client Java 21 pour l’[API DKBSign](https://docs.dkbsigns.com/). URL de base : `https://api.dkbsigns.com`.

## Prérequis

- Java 21
- Maven

## Installation locale

```bash
mvn install
```

Dépendance Maven (après publication) :

```xml
<dependency>
    <groupId>com.dkbsign</groupId>
    <artifactId>dkbsign-java-sdk</artifactId>
    <version>1.0-SNAPSHOT</version>
</dependency>
```

## Authentification

```java
AuthService auth = AuthService.create(); // ou .create("https://api.dkbsigns.com")
LoginResult login = auth.login("user@acme.test", "password");

if (login.isMfaRequired()) {
    auth.sendMfaEmail(login.getMfaChallenge().getChallengeToken());
    login = auth.verifyMfa(
            login.getMfaChallenge().getChallengeToken(),
            MfaMethod.EMAIL,
            "123456");
}

DkbSignClient client = auth.toClient(login);
```

Vous pouvez aussi passer un token déjà obtenu :

```java
DkbSignClient client = DkbSignClient.builder()
        .token("1|…")
        .build();
```

## Self-sign

Voir `src/test/java/example/QuickStartExample.java` (OTP en deux temps).

## Enveloppes (parapheurs)

```java
CreateEnvelopeResponse created = client.envelopes().create(
        CreateEnvelopeRequest.builder()
                .document(Path.of("contrat.pdf"))
                .title("NDA Q1")
                .signer(EnvelopeSigner.builder("signer@example.com")
                        .mark(0, 0, SignatureMark.at(120, 200).size(200, 70))
                        .build())
                .build());

System.out.println(created.getSigners().get(0).getSigningUrl());
```

Exemple complet avec login : `src/test/java/com/dkbsign/example/EnvelopeExample.java`.

## Session publique signataire

Sans compte entreprise : UUID extrait du `signing_url` renvoyé à la création de l’enveloppe.

```java
PublicEnvelopeService signing = DkbSignClient.publicSigning("signer-uuid-from-url");

signing.getSession();
signing.requestOtp();
signing.verifyOtp("123456");
signing.sign(PublicSignRequest.builder().build());

// ou refuser
signing.refuse("Motif optionnel", null);
```

Identité (si `identity.required`) : `enrollIdentity`, `rescanIdentity`, `documentCheckIdentity`, `prepareIdentity`.  
Aperçu PDF : `previewDocument(documentUuid, Path.of("preview.pdf"))`.

Exemple : `src/test/java/com/dkbsign/example/PublicSigningExample.java` (`DKBSIGN_SIGNER_UUID`, `DKBSIGN_OTP`).

## Tests

```bash
mvn test
```

## Documentation API

- [Quickstart](https://docs.dkbsigns.com/quickstart)
- [Authentication](https://docs.dkbsigns.com/authentication)
- [Signing / enveloppes](https://docs.dkbsigns.com/signing)
