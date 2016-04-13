# Java library for encryption for push notification payloads for Chrome-GCM

## Getting Started

- Use `gradle build` to build
- Use `gradle writeClasspath` to write the classes to <root>/build/classpath.txt. Use this in your application (or
Scala REPL) for testing
- Requires Java8 and (dependencies)

## Usage

```java

  // Initialization
  ChromePushUtils chromePushUtils = new ChromePushUtils();

  // Reading client key, auth
  byte[] clientPublicKey = Base64.getUrlDecoder().decode(clientPublicKeyFromChrome);
  ECPublicKey clientPublic = createPublicKey(clientPublicKey);

  byte[] clientAuth = Base64.getUrlDecoder().decode(authFromChrome);

  // Generate server keys
  KeyPair serverKeys = utils.generateServerKeyPair();

  // Generate shared secret from server keys and client public key
  byte[] sharedSecret = generateSharedSecret(serverKeys, clientPublic);

  // Generate salt
  byte[] salt = salt = utils.generateSalt();

  // Generate info fields
  byte[] nonceInfo = generateInfo(clientPublic, serverKeys.getPublicKey(), NONCE);
  byte[] contentEncryptionKeyInfo = generateInfo(clientPublic, serverKeys.getPublicKey(), AESGCM128);

  // Encrypt payload
  String encryptedMessage = encryptPayload(message, sharedSecret, salt, contentEncryptionKeyInfo, nonceInfo,
  clientAuth);

  // Generate GCM headers
  String encryptionHeader = createEncryptionHeader(salt);
  String cryptoKeyHeader = createCryptoKeyHeader(serverKeys.getPublicKey());

```

You can use the above fields to talk to GCM. A request would look like:

```

curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" -H "Authorization: key=AIzaSyBzYTfZeaVLaRr4xZJqxe2Mr570AFsl8y0"  -d '{
        "registration_ids": ["<gcm_tokens>"],
        "data":{"crypto_key":"<cryptoKeyHeader>","encryption":"encryptionHeader",
        "payload":"encryptedMessage"}
        }' 'https://android.googleapis.com/gcm/send'


```



