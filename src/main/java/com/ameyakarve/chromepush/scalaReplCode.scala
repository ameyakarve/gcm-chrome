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
