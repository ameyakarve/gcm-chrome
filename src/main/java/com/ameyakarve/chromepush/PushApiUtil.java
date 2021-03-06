package com.ameyakarve.chromepush;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.Mac;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;


/**
 * Created by akarve on 5/4/16.
 */

public class PushApiUtil {
  private static final byte[] P256 = "P-256".getBytes(StandardCharsets.UTF_8);
  private static final int GCM_TAG_LENGTH = 16; // in bytes
  private static final String HMAC_SHA256 = "HmacSHA256";
  private static final String SHA1_PRNG = "SHA1PRNG";

  private PushApiUtil() {
    // no op
  }

  /**
   * Returns an info record. See sections 3.2 and 3.3 of
   * {https://tools.ietf.org/html/draft-ietf-httpbis-encryption-encoding-00}
   * @param serverPublicKey server public key
   * @param clientPublicKey client public key
   * @param type info type
   * @return
   * @throws IOException
   */
  public static byte[] generateInfo(final byte[] serverPublicKey, final byte[] clientPublicKey, final byte[] type)
      throws IOException {

    // The start index for each element within the buffer is:
    // value               | length | start  |
    // ---------------------------------------
    // 'Content-Encoding: '|   18   | 0      |
    // type                |   l    | 18     |
    // null byte           |   1    | 18 + l |
    // 'P-256'             |   5    | 19 + l |
    // info                |   135  | 24 + l |
    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    outputStream.write(Constants.CONTENT_ENCODING); // Append the string “Content-Encoding: “
    outputStream.write(type); // Append the |type|
    outputStream.write((byte) 0); // Append a NULL-byte
    outputStream.write(P256); // Append the string “P-256”

    // The context format is:
    // 0x00 || length(clientPublicKey) || clientPublicKey ||
    //         length(serverPublicKey) || serverPublicKey
    // The lengths are 16-bit, Big Endian, unsigned integers so take 2 bytes each.

    // The keys should always be 65 bytes each. The format of the keys is
    // described in section 4.3.6 of the (sadly not freely linkable) ANSI X9.62
    // specification.
    outputStream.write((byte) 0); // Append a NULL-byte

    outputStream.write((byte) 0); // Append the length of the recipient’s public key (here |client_public|)
    outputStream.write((byte) 65); // as a two-byte integer in network byte order.

    outputStream.write(clientPublicKey); // Append the raw bytes (65) of the recipient’s
    // public key.

    outputStream.write((byte) 0); // Append the length of the sender’s public key (here |server_public|)
    outputStream.write((byte) 65); // as a two-byte integer in network byte order.

    outputStream.write(serverPublicKey); // Append the raw bytes (65) of the
    // sender’s
    // public key.

    return outputStream.toByteArray();
  }

  public static String createEncryptionHeader(final byte[] salt) {
    // Encode |salt| using the URL-safe base64 encoding, store it in |encoded_salt|.
    // Return the result of concatenating (“salt=”, |encoded_salt|).
    return "salt=" + Base64.getUrlEncoder().encodeToString(salt);
  }

  public static String createCryptoKeyHeader(final byte[] serverPublic) {
    //Encode |server_public| using the URL-safe base64 encoding, store it in |encoded_server_public|.
    // Return the result of concatenating (“dh=”, |encoded_server_public|).
    return "dh=" + Base64.getUrlEncoder().encodeToString(serverPublic);
  }



  /**
   * Performs an hkdf extract on the message and trims to (lengthToExtract)
   *  * HMAC-based Extract-and-Expand Key Derivation Function (HKDF)
   *
   * This is used to derive a secure encryption key from a mostly-secure shared
   * secret.
   *
   * This is a partial implementation of HKDF tailored to our specific purposes.
   * In particular, for us the value of N will always be 1, and thus T always
   * equals HMAC-Hash(PRK, info | 0x01).
   *
   * See {https://www.rfc-editor.org/rfc/rfc5869.txt}
   * @param secretKey key to perform the HMAC with
   * @param salt random salt bytes
   * @param messageToExtract message to perform hkdf extract on
   * @param lengthToExtract how much to trim the output by
   * @return hkdf extract
   * @throws NoSuchAlgorithmException
   * @throws InvalidKeyException
   */
  public static byte[] hkdfExtract(final byte[] secretKey, final byte[] salt, final byte[] messageToExtract,
      final int lengthToExtract)
      throws NoSuchAlgorithmException, InvalidKeyException {
    Mac outerMac = Mac.getInstance(HMAC_SHA256);
    outerMac.init(new SecretKeySpec(salt, HMAC_SHA256));
    byte[] outerResult = outerMac.doFinal(secretKey);
    Mac innerMac = Mac.getInstance(HMAC_SHA256);
    innerMac.init(new SecretKeySpec(outerResult, HMAC_SHA256));
    byte[] message = new byte[messageToExtract.length + 1];
    System.arraycopy(messageToExtract, 0, message, 0, messageToExtract.length);
    message[messageToExtract.length] = (byte) 1;
    byte[] innerResult = innerMac.doFinal(message);
    return Arrays.copyOf(innerResult, lengthToExtract);
  }

  /**
   * Encrypts a message such that it can be sent using the Web Push protocol.
   * You can find out more about the various pieces:
   *  - {https://tools.ietf.org/html/draft-ietf-httpbis-encryption-encoding}
   *  - {https://en.wikipedia.org/wiki/Elliptic_curve_Diffie%E2%80%93Hellman}
   *  - {https://tools.ietf.org/html/draft-ietf-webpush-encryption}
   * @param message Message to encrypt
   * @param sharedSecret Shared secret computed using the server keys and the client public key
   * @param salt 16 random bytes
   * @param contentEncryptionKeyInfo
   * @param nonceInfo
   * @param clientAuth Client's auth (generated on the browser)
   * @return
   * @throws InvalidKeyException
   * @throws NoSuchAlgorithmException
   * @throws IllegalBlockSizeException
   * @throws InvalidAlgorithmParameterException
   * @throws BadPaddingException
   * @throws NoSuchProviderException
   * @throws NoSuchPaddingException
   */
  public static String encryptPayload(final String message, final byte[] sharedSecret, final byte[] salt,
      final byte[] contentEncryptionKeyInfo, final byte[] nonceInfo, final byte[] clientAuth)
      throws InvalidKeyException, NoSuchAlgorithmException, IllegalBlockSizeException,
             InvalidAlgorithmParameterException, BadPaddingException, NoSuchProviderException, NoSuchPaddingException {

    // Derive a Pseudo-Random Key (prk) that can be used to further derive our
    // other encryption parameters. These derivations are described in
    // https://tools.ietf.org/html/draft-ietf-httpbis-encryption-encoding-00
    final byte[] prk =
        hkdfExtract(sharedSecret, clientAuth, "Content-Encoding: auth\0".getBytes(StandardCharsets.UTF_8), 32);

    // Derive the Content Encryption Key
    final byte[] contentEncryptionKey = hkdfExtract(prk, salt, contentEncryptionKeyInfo, 16);

    // Derive the Nonce / iv
    final byte[] nonce = hkdfExtract(prk, salt, nonceInfo, 12);

    // Not adding any padding for now. First two bytes are reserved for number of padding bytes (0)
    final byte[] record = ("\0\0" + message).getBytes(StandardCharsets.UTF_8);

    // Set |ciphertext| to the result of encrypting |record| with AEAD_AES_128_GCM, using
    // the |content_encryption_key| as the key, the |nonce| as the nonce/IV, and an authentication tag of 16 bytes.

    final byte[] ciphertext = encryptWithAESGCM128(nonce, contentEncryptionKey, record);

    // Encode the |ciphertext| using the URL-safe base64 encoding, store it in |encoded_ciphertext|.
    return Base64.getEncoder().encodeToString(ciphertext);
  }

  /**
   * Encrypt the plaintext message using AES128/GCM
   * @param nonce The iv
   * @param contentEncryptionKey The private key to use
   * @param record The message to be encrypted
   * @return the encrypted payload
   * @throws NoSuchPaddingException
   * @throws NoSuchAlgorithmException
   * @throws NoSuchProviderException
   * @throws InvalidAlgorithmParameterException
   * @throws InvalidKeyException
   * @throws BadPaddingException
   * @throws IllegalBlockSizeException
   */
  public static byte[] encryptWithAESGCM128(final byte[] nonce, final byte[] contentEncryptionKey,
      final byte[] record)
      throws NoSuchPaddingException, NoSuchAlgorithmException, NoSuchProviderException,
             InvalidAlgorithmParameterException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
    Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding", "SunJCE");
    SecretKey key = new SecretKeySpec(contentEncryptionKey, "AES");
    GCMParameterSpec spec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, nonce);
    cipher.init(Cipher.ENCRYPT_MODE, key, spec);
    return cipher.doFinal(record);
  }

  /**
   * Generates 16 cryptographically secure random bytes
   * @return 16 byte salt
   * @throws NoSuchAlgorithmException
   */
  public static byte[] generateSalt()
      throws NoSuchAlgorithmException {
    byte[] salt = new byte[16];
    SecureRandom.getInstance(SHA1_PRNG).nextBytes(salt);
    return salt;
  }
}