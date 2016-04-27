# Java library for encryption for push notification payloads for Chrome-GCM

## Getting Started

- Use `gradle build` to build
- Use `gradle writeClasspath` to write the classes to <root>/build/classpath.txt. Use this in your application (or
Scala REPL) for testing
- Requires Java8 and (dependencies)

## Usage

Sample Scala code to run this( You can do scala -cp <output-of-gradle-writeClasspath-from-build/classpath.txt>):

```scala

import com.ameyakarve.chromepush.ChromePushUtils
import com.ameyakarve.chromepush.EllipticCurveKeyUtils
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.security.interfaces.ECPublicKey

val ecku = new EllipticCurveKeyUtils()
val serverKeys = ecku.generateServerKeyPair()
val clientPublicKey = ecku.loadP256Dh("BLWz1aO3PlkI9QazDVoHDAgk16PPJqOwaVoPbmGAspkNrydlB6KGvixULOnJTMGbTFv8S915y0h1s6dTa0cxmTc=")
val salt = ChromePushUtils.generateSalt()
val clientAuth = Base64.getUrlDecoder().decode("i5wNP-TV8mUjngtA_mvF0g==")
val sharedSecret = ecku.generateSharedSecret(serverKeys, clientPublicKey)

val serverPublicKeyBytes = ecku.publicKeyToBytes(serverKeys.getPublic().asInstanceOf[ECPublicKey])
val clientPublicKeyBytes = ecku.publicKeyToBytes(clientPublicKey)

val nonceInfo = ChromePushUtils.generateInfo(serverPublicKeyBytes, clientPublicKeyBytes, "nonce".getBytes(StandardCharsets.UTF_8))
val contentEncryptionKeyInfo = ChromePushUtils.generateInfo(serverPublicKeyBytes, clientPublicKeyBytes, "aesgcm".getBytes(StandardCharsets.UTF_8))
val ciphertext = ChromePushUtils.encryptPayload("Hello Worldz", sharedSecret, salt, contentEncryptionKeyInfo, nonceInfo, clientAuth)
val encryptionHeader = ChromePushUtils.createEncryptionHeader(salt)
val cryptoKeyHeader = ChromePushUtils.createCryptoKeyHeader(serverPublicKeyBytes)

```

To send the request to GCM, use

```

curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" -H "Authorization: key=<key>" -H "Encryption: salt=eZULO4LdQPl_tpv79Ri3Ng==" -H "Crypto-Key: dh=BJ5xtRN5AkECsyoM3ljFkxrmlYjB-lsDIwUoOT5RlMV3AbDHcRg-MFFgLLfu5ef56pA5wtlz1noGLfNVPjRE_UA=" -H "Content-Encoding: aesgcm" -H "Cache-Control: no-cache" -d '{
    "registration_ids": ["d14qJGqcOI0:APA91bHwMJJegKCw1fX0IHJkicmUGlcWDHyOBEMHVgX6W5uMzUz9DqjJ1YDtqJ-rSzsb253LTcuKtlCKAXSRf5Dx16l9IE1C7XnKQY_IpLvfa7TuGT2ftKunJ4yR0XfFgQndRbu2gawu"],
    "raw_data": "RXFlOrxl7NC0QXoCJmQyZPIj/YjDveJucVhL0OQn"
}' "https://android.googleapis.com/gcm/send"

```

