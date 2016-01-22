import com.ameyakarve.chromepush.ChromePushUtils
import java.security.interfaces.ECPublicKey
import java.security.interfaces.ECPrivateKey

val utils = new ChromePushUtils()
val clientPublicKey = utils.createPublicKey("BAA7hlasczMn_0AIligu4aVayyjYnlwzVuiKyI2QfOiyb7XEc0mgkznvQQ7q4jG0HKIEAGHSO5cUpnyDuAJUwHE=")

val serverKeys = utils.generateServerKeyPair()
val serverPublicKey: ECPublicKey = serverKeys.getPublic().asInstanceOf[ECPublicKey]
val serverPrivateKey: ECPrivateKey = serverKeys.getPrivate().asInstanceOf[ECPrivateKey]

val clientPublic = utils.publicKeyToBytes(clientPublicKey)
val serverPublic = utils.publicKeyToBytes(serverPublicKey)
val serverPrivate = utils.privateKeyToBytes(serverPrivateKey)

val sharedSecret = utils.generateSharedSecret(serverKeys, clientPublicKey)

val gcmParams = utils.getGcmChromePushParams(serverPublic, clientPublic, sharedSecret, "{\"key\":true}")
