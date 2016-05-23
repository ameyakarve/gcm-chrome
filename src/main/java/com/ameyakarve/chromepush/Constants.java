package com.ameyakarve.chromepush;

import java.nio.charset.StandardCharsets;

<<<<<<< HEAD
/**
 * Created by akarve on 5/4/16.
 */
public final class Constants {

  private Constants() {
    // no op
  }

=======

/**
 * Created by akarve on 4/26/16.
 */
public class Constants {
>>>>>>> b0899ab69e7a5678a077036a37034b42eba56810
  public static final byte[] CONTENT_ENCODING = "Content-Encoding: ".getBytes(StandardCharsets.UTF_8);
  public static final byte[] AESGCM128 = "aesgcm".getBytes(StandardCharsets.UTF_8);
  public static final byte[] NONCE = "nonce".getBytes(StandardCharsets.UTF_8);
  private static final byte[] P256 = "P-256".getBytes(StandardCharsets.UTF_8);
  private static final int GCM_TAG_LENGTH = 16; // in bytes
  public static final String SECP256R1 = "secp256r1";
  public static final String HMAC_SHA256 = "HmacSHA256";
  public static final String SHA1_PRNG = "SHA1PRNG";
}
