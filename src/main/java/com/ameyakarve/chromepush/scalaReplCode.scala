import com.ameyakarve.chromepush.ChromePushUtils
import java.security.interfaces.ECPublicKey
import java.security.interfaces.ECPrivateKey
import java.util.Base64

val utils = new ChromePushUtils()
val clientAuth = Base64.getUrlDecoder().decode("7XeH1mK-zbL3YXiMb8UB1Q==");
val clientPublicKey = utils.createPublicKey("BGQGOin_m3zmA9XWixIbiA_vP9OCYZ3oJ_VjL0Q34socI_SsLFrd9n9FFfmTDuGxSCvG-PDAAAC02E5B4TwkIY8=")
// val salt = utils.generateSalt()
val salt = Base64.getUrlDecoder().decode("4ySkAeiZp33fmMxRwbtFdQ==")

// val serverKeys = utils.generateServerKeyPair()
// val serverPublicKey: ECPublicKey = serverKeys.getPublic().asInstanceOf[ECPublicKey]
// val serverPrivateKey: ECPrivateKey = serverKeys.getPrivate().asInstanceOf[ECPrivateKey]

val clientPublic = utils.publicKeyToBytes(clientPublicKey)
// val serverPublic = utils.publicKeyToBytes(serverPublicKey)
// val serverPrivate = utils.privateKeyToBytes(serverPrivateKey)
val serverPublic = Base64.getUrlDecoder().decode("BMYr62Nn4eqwBcOC3fchiZAdzps67dj0AzP8FqEcdNi3Ka52b_O8h5W4qhY_u1wmVSozVzlDNnJw4Hk0kei1-as=")
val serverPrivate = Base64.getUrlDecoder().decode("K-XqdjfgATqwaT28dJxS2Xh0hK0iRp-6kH783DGBWcg=")

// val sharedSecret = utils.generateSharedSecret(serverKeys, clientPublicKey)
val sharedSecret = Base64.getUrlDecoder().decode("zfCSWv1LWQC9xqRXqh29Oe9ap62XPjbShMm3_rpz22c=")

val gcmParams = utils.getGcmChromePushParams(serverPublic, clientPublic, sharedSecret, clientAuth, "{\"key\":true}", salt)
