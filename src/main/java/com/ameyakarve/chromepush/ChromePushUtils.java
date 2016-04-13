package com.ameyakarve.chromepush;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.KeyAgreement;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.ECPointUtil;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.json.JSONObject;


/*
 * This Java source file was auto generated by running 'gradle buildInit --type java-library'
 * by 'akarve' at '1/21/16 9:31 PM' with Gradle 2.1
 *
 * @author akarve, @date 1/21/16 9:31 PM
 */

public class ChromePushUtils {
  private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
  private static final byte[] CONTENT_ENCODING = "Content-Encoding: ".getBytes(StandardCharsets.UTF_8);
  private static final byte[] AESGCM128 = "aesgcm".getBytes(StandardCharsets.UTF_8);
  private static final byte[] NONCE = "nonce".getBytes(StandardCharsets.UTF_8);
  private static final byte[] P256 = "P-256".getBytes(StandardCharsets.UTF_8);
  private static final int GCM_TAG_LENGTH = 16; // in bytes

  public ChromePushUtils() {
    Security.addProvider(new org.bouncycastle.jce.provider.BouncyCastleProvider());
  }

  public static String bytesToHex(byte[] bytes) {
    char[] hexChars = new char[bytes.length * 2];
    for (int j = 0; j < bytes.length; j++) {
      int v = bytes[j] & 0xFF;
      hexChars[j * 2] = hexArray[v >>> 4];
      hexChars[j * 2 + 1] = hexArray[v & 0x0F];
    }
    return new String(hexChars);
  }

  public static byte[] hexToBytes(String s) {
    int len = s.length();
    byte[] data = new byte[len / 2];
    for (int i = 0; i < len; i += 2) {
      data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i + 1), 16));
    }
    return data;
  }

  public ECPublicKey createPublicKey(final String publicKey)
      throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchProviderException {
    ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256r1"); // P256 curve
    KeyFactory kf = KeyFactory.getInstance("ECDH", "BC");
    ECNamedCurveSpec params = new ECNamedCurveSpec("secp256r1", spec.getCurve(), spec.getG(), spec.getN());
    ECPoint point = ECPointUtil.decodePoint(params.getCurve(), Base64.getUrlDecoder().decode(publicKey));
    ECPublicKeySpec pubKeySpec = new ECPublicKeySpec(point, params);
    ECPublicKey pk = (ECPublicKey) kf.generatePublic(pubKeySpec);
    return pk;
  }

  public KeyPair generateServerKeyPair()
      throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException {
    ECParameterSpec ecSpec = ECNamedCurveTable.getParameterSpec("secp256r1"); // P256 curve
    KeyPairGenerator g = KeyPairGenerator.getInstance("ECDH", "BC");
    g.initialize(ecSpec, new SecureRandom());
    KeyPair pair = g.generateKeyPair();
    return pair;
  }

  public byte[] publicKeyToBytes(ECPublicKey publicKey) {
    ECPoint point = publicKey.getW();
    String x = point.getAffineX().toString(16);
    String y = point.getAffineY().toString(16);
    
    /*
     *  Format is 04 followed by 32 bytes (64 hex) of X followed by 32 bytes (64 hex of Y)
    */
    StringBuilder sb = new StringBuilder();
    sb.append("04");

    for (int i = 0; i < 64 - x.length(); i++) {
      sb.append(0);
    }
    sb.append(x);

    for (int i = 0; i < 64 - y.length(); i++) {
      sb.append(0);
    }
    sb.append(y);
    byte[] output = hexToBytes(sb.toString());
    System.out.println(Base64.getUrlEncoder().encodeToString(output));
    return output;
  }

  public byte[] privateKeyToBytes(ECPrivateKey privateKey) {
    /*
     *  Format is 32 bytes (64 hex) of S 
    */
    StringBuilder sb = new StringBuilder();
    String s = privateKey.getS().toString(16);
    for (int i = 0; i < 64 - s.length(); i++) {
      sb.append(0);
    }
    sb.append(s);
    byte[] output = hexToBytes(sb.toString());
    System.out.println(Base64.getUrlEncoder().encodeToString(output));
    return output;
  }

  public byte[] generateSharedSecret(final KeyPair serverKeys, PublicKey clientPublicKey)
      throws NoSuchAlgorithmException, InvalidKeyException, NoSuchProviderException {
    KeyAgreement keyAgreement = KeyAgreement.getInstance("ECDH", "BC");
    keyAgreement.init(serverKeys.getPrivate());
    keyAgreement.doPhase(clientPublicKey, true);
    byte[] output = keyAgreement.generateSecret();
    return output;
  }

  public byte[] generateInfo(final byte[] client_public, final byte[] server_public, final byte[] type)
      throws Exception {

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(CONTENT_ENCODING); // Append the string “Content-Encoding: “
    outputStream.write(type); // Append the |type|
    outputStream.write((byte) 0); // Append a NULL-byte
    outputStream.write(P256); // Append the string “P-256”
    outputStream.write((byte) 0); // Append a NULL-byte

    outputStream.write((byte) 0); // Append the length of the recipient’s public key (here |client_public|)
    outputStream.write((byte) 65); // as a two-byte integer in network byte order.

    outputStream.write(client_public); // Append the raw bytes (65) of the recipient’s public key.

    outputStream.write((byte) 0); // Append the length of the sender’s public key (here |server_public|)
    outputStream.write((byte) 65); // as a two-byte integer in network byte order.

    outputStream.write(server_public); // Append the raw bytes (65) of the sender’s public key.

    return outputStream.toByteArray(); // Verified that the lengths are good.
  }

  public static String createEncryptionHeader(final byte[] salt) {
    // Encode |salt| using the URL-safe base64 encoding, store it in |encoded_salt|.
    // Return the result of concatenating (“salt=”, |encoded_salt|).
    return "salt=" + Base64.getUrlEncoder().encodeToString(salt);
  }

  public static String createCryptoKeyHeader(final byte[] server_public) {
    //Encode |server_public| using the URL-safe base64 encoding, store it in |encoded_server_public|.
    // Return the result of concatenating (“dh=”, |encoded_server_public|).
    return "dh=" + Base64.getUrlEncoder().encodeToString(server_public);
  }

  public byte[] computeSHA256HMAC(final byte[] secret, final byte[] message)
      throws Exception {
    Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
    SecretKeySpec secret_key = new SecretKeySpec(secret, "HmacSHA256");
    sha256_HMAC.init(secret_key);
    return sha256_HMAC.doFinal(message);
  }

  public byte[] hkdfExtract(byte[] secret_key, byte[] salt, byte[] messageToExtract, int len)
      throws Exception {
    Mac outerMac = Mac.getInstance("HmacSHA256");
    outerMac.init(new SecretKeySpec(salt, "HmacSHA256"));
    byte[] outerResult = outerMac.doFinal(secret_key);
    System.out.println("Outer mac result: " + Base64.getUrlEncoder().encodeToString(outerResult));
    Mac innerMac = Mac.getInstance("HmacSHA256");
    innerMac.init(new SecretKeySpec(outerResult, "HmacSHA256"));
    byte[] message = new byte[messageToExtract.length + 1];
    System.arraycopy(messageToExtract, 0, message, 0, messageToExtract.length);
    message[messageToExtract.length] = (byte) 1;
    System.out.println("Info: " + Base64.getUrlEncoder().encodeToString(message));
    byte[] innerResult = innerMac.doFinal(message);
    System.out.println("Inner mac result: " + Base64.getUrlEncoder().encodeToString(innerResult));
    return Arrays.copyOf(innerResult, len);
  }

  public String encryptPayload(final String plaintext, final byte[] shared_secret, final byte[] salt,
      final byte[] content_encryption_key_info, final byte[] nonce_info, final byte[] client_auth)
      throws Exception {

    final byte[] prk =
        hkdfExtract(shared_secret, client_auth, "Content-Encoding: auth\0".getBytes(StandardCharsets.UTF_8), 32);

    final byte[] content_encryption_key = hkdfExtract(prk, salt, content_encryption_key_info, 16);

    final byte[] nonce = hkdfExtract(prk, salt, nonce_info, 12);

    final byte[] record = ("\0\0" + plaintext).getBytes(StandardCharsets.UTF_8);

    // Set |ciphertext| to the result of encrypting |record| with AEAD_AES_128_GCM, using the |content_encryption_key| as the key, the |nonce| as the nonce/IV, and an authentication tag of 16 bytes. (See function below)
    final byte[] ciphertext = encryptWithAESGCM128(nonce, content_encryption_key, record);

    System.out.println("Cipher text is " + Base64.getUrlEncoder().encodeToString(ciphertext));

    return Base64.getUrlEncoder().encodeToString(
        ciphertext); // Encode the |ciphertext| using the URL-safe base64 encoding, store it in |encoded_ciphertext|.
  }

  public static byte[] encryptWithAESGCM128(final byte[] nonce, final byte[] content_encryption_key,
      final byte[] record)
      throws Exception {
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
    SecretKey key = new SecretKeySpec(content_encryption_key, "AES");
    GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
    cipher.init(Cipher.ENCRYPT_MODE, key, spec);
    return cipher.doFinal(record);
  }

  public byte[] generateSalt()
      throws Exception {
    byte[] salt = new byte[16];
    SecureRandom.getInstance("SHA1PRNG")
        .nextBytes(salt); // Generate 16 cryptographically secure random bytes, store them in |salt|.
    System.out.println(Base64.getUrlEncoder().encodeToString(salt));
    return salt;
  }

  public JSONObject getGcmChromePushParams(final byte[] server_public, final byte[] client_public,
      final byte[] shared_secret, final byte[] client_auth, final String plaintext, final byte[] salt)
      throws Exception {

    final byte[] nonce_info = generateInfo(client_public, server_public,
        NONCE); // Determine the |nonce_info| per the Steps for creating Info with |type| being the string “nonce”.
    final byte[] content_encryption_key_info = generateInfo(client_public, server_public,
        AESGCM128); // Determine the |content_encryption_key_info| per the Steps for creating Info with |type| being the string “aesgcm128”.
    final String ciphertext = encryptPayload(plaintext, shared_secret, salt, content_encryption_key_info, nonce_info,
        client_auth); // Run the steps for encrypting the payload, store it to |ciphertext|. (This is already Base64 encoded)

    final String encryption_header = createEncryptionHeader(
        salt); // Determine the |encryption_header| by running the steps for creating the Encryption header with |salt|.
    final String crypto_key_header = createCryptoKeyHeader(
        server_public); // Determine the |crypto_key_header| by running the steps for creating the Crypto-Key header with |server_public|.

    JSONObject m = new JSONObject();
    m.put("encryption",
        encryption_header); // Add a field to the “data” section of the message named “encryption”, with value |encryption_header|.
    m.put("crypto_key",
        crypto_key_header); // Add a field to the “data” section of the message named “crypto_key”, with value |crypto_key_header|.

    m.put("payload",
        ciphertext); // Add a field to the “data” section of the message named “payload”, with value |encoded_ciphertext|.

    return m;

    /*
     *  curl, request from generated data:

        curl -X POST -H "Accept: application/json" -H "Content-Type: application/json" -H "Authorization: key=AIzaSyBzYTfZeaVLaRr4xZJqxe2Mr570AFsl8y0" -H "X-RestLi-Protocol-Version: 1.0.0" -H "Cache-Control: no-cache" -H "Postman-Token: 09c535d1-2c25-b91a-ada4-cac6405d4ddf" -d '{
        "registration_ids": ["dnDhW89rq6g:APA91bFw-hPmxR6QzSRdbWkVAha8KSH2LLHKkTAbEC1F9XdihifGzyVITZBodhhm_Dynjm19h9FBA2xqF5fA9L08XfaOmlJjf2lH6oDoudXV_QXFhPJm1bCBQMrXPWemsgh-ZfV6uqxv"],
        "data":{"crypto_key":"dh=BHTXsIWvWrjzZRkjm42Rs5y_pEW6pEG7gcghdKNfJGKI8bvkoU8oGy1ZmjoD3xFy9YAZEKfAgzyWbqrTV5mzli4=","encryption":"salt=m96vU3JLK43E6SYsypgdXw==","payload":"r9zKCit7-Myg_QMwzP2baKr39NtBk3N4ugb1A48="}
        }' 'https://android.googleapis.com/gcm/send'

        POST /gcm/send HTTP/1.1
        Host: android.googleapis.com
        Accept: application/json
        Content-Type: application/json
        Authorization: key=AIzaSyBzYTfZeaVLaRr4xZJqxe2Mr570AFsl8y0
        X-RestLi-Protocol-Version: 1.0.0
        Cache-Control: no-cache
        Postman-Token: f124df06-b681-1c2d-1829-92896eb68cd2

        {
            "registration_ids": ["dnDhW89rq6g:APA91bFw-hPmxR6QzSRdbWkVAha8KSH2LLHKkTAbEC1F9XdihifGzyVITZBodhhm_Dynjm19h9FBA2xqF5fA9L08XfaOmlJjf2lH6oDoudXV_QXFhPJm1bCBQMrXPWemsgh-ZfV6uqxv"],
            "data":{"crypto_key":"dh=BHTXsIWvWrjzZRkjm42Rs5y_pEW6pEG7gcghdKNfJGKI8bvkoU8oGy1ZmjoD3xFy9YAZEKfAgzyWbqrTV5mzli4=","encryption":"salt=m96vU3JLK43E6SYsypgdXw==","payload":"r9zKCit7-Myg_QMwzP2baKr39NtBk3N4ugb1A48="}
        }

    */
  }

  public void sendPushMessage(final String endpoint, final byte[] p256dh, final byte[] auth[], final String message,
      final byte[] secret) {

  }
}
