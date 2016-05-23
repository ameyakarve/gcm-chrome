package com.ameyakarve.chromepush;

import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;

/**
 * Created by akarve on 4/27/16.
 */
public abstract class BaseTestCase {
  final String publicKeyBase64 = "BOg5KfYiBdDDRF12Ri17y3v+POPr8X0nVP2jDjowPVI/DMKU1aQ3OLdPH1iaakvR9/PHq6tNCzJH35v/JUz2crY=";
  final BigInteger publicKeyX = new BigInteger("E83929F62205D0C3445D76462D7BCB7BFE3CE3EBF17D2754FDA30E3A303D523F", 16);
  final BigInteger publicKeyY = new BigInteger("0CC294D5A43738B74F1F589A6A4BD1F7F3C7ABAB4D0B3247DF9BFF254CF672B6", 16);

  final String privateKeyBase64 = "uDNsfsz91y2ywQeOHljVoiUg3j5RGrDVAswRqjP3v90=";
  final BigInteger privateKeyS = new BigInteger("B8336C7ECCFDD72DB2C1078E1E58D5A22520DE3E511AB0D502CC11AA33F7BFDD", 16);

  final String p256dh = "BCIWgsnyXDv1VkhqL2P7YRBvdeuDnlwAPT2guNhdIoW3IP7GmHh1SMKPLxRf7x8vJy6ZFK3ol2ohgn_-0yP7QQA=";

  final String _clientAuthUrlBase64 = "8eDyX_uCN0XRhSbY5hs7Hg==";

  final String expectedSharedSecret = "vgkL5otElJ7tB3jnxop9g7sGxuM4gGs5NL3qTCxe9JE="; //base64 shared secret

  final EllipticCurveKeyUtil _ellipticCurveKeyUtils;

  final KeyPair _serverKeyPair;
  final ECPublicKey _clientPublicKey;

  final String _exampleSalt = "AAAAAAAAAAAAAAAAAAAAAA==";

  final String messageToEncrypt = "Hello, World.";
  final String expectedEncryptedMessage = "CE2OS6BxfXsC2YbTdfkeWLlt4AKWbHZ3Fe53n5/4Yg==";

  protected BaseTestCase()
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException,
             InvalidKeySpecException {
    _ellipticCurveKeyUtils = new EllipticCurveKeyUtil();
    _serverKeyPair = _ellipticCurveKeyUtils.loadECKeyPair(publicKeyX, publicKeyY, privateKeyS);;
    _clientPublicKey = _ellipticCurveKeyUtils.loadP256Dh(p256dh);;
  }
}