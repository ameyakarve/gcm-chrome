import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Base64;
import org.junit.Assert;
import org.junit.Test;

import static junit.framework.Assert.*;


/**
 * Created by akarve on 4/27/16.
 */
public class TestEllipticCurveKeyUtils extends BaseTestCase{

  // Example keys


  public TestEllipticCurveKeyUtils()
      throws InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException,
             InvalidKeySpecException {
    super();
  }

  @Test
  public void testLoadECKeyPair()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {

    ECPublicKey publicKey = (ECPublicKey) _serverKeyPair.getPublic();
    ECPrivateKey privateKey = (ECPrivateKey) _serverKeyPair.getPrivate();

    assertEquals(publicKeyX, publicKey.getW().getAffineX());
    assertEquals(publicKeyY, publicKey.getW().getAffineY());
    assertEquals(privateKeyS, privateKey.getS());
  }

  @Test
  public void testPublicKeyToBytes()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
    ECPublicKey publicKey = (ECPublicKey) _serverKeyPair.getPublic();
    byte[] publicKeyBytes = _ellipticCurveKeyUtils.publicKeyToBytes(publicKey);
    byte[] expectedPublicKeyBytes = Base64.getDecoder().decode(publicKeyBase64);
    Assert.assertArrayEquals(expectedPublicKeyBytes, publicKeyBytes);
  }

  @Test
  public void testPrivateKeyToBytes()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
    ECPrivateKey privateKey = (ECPrivateKey) _serverKeyPair.getPrivate();
    byte[] privateKeyBytes = _ellipticCurveKeyUtils.privateKeyToBytes(privateKey);
    byte[] expectedPrivateKeyBytes = Base64.getDecoder().decode(privateKeyBase64);
    Assert.assertArrayEquals(expectedPrivateKeyBytes, privateKeyBytes);
  }

  @Test
  public void testLoadP256Dh()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
    byte[] clientPublicKeyBytes = _ellipticCurveKeyUtils.publicKeyToBytes(_clientPublicKey);
    Assert.assertEquals(p256dh, Base64.getUrlEncoder().encodeToString(clientPublicKeyBytes));
  }

  @Test
  public void testGenerateSharedSecret()
      throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidKeyException {
    final byte[] sharedSecret = _ellipticCurveKeyUtils.generateSharedSecret(_serverKeyPair, _clientPublicKey);
    Assert.assertEquals(expectedSharedSecret, Base64.getEncoder().encodeToString(sharedSecret));
  }
}
