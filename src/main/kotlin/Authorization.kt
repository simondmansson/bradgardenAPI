import mu.KLogging
import org.apache.commons.dbutils.DbUtils
import java.util.*
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * Created by kalk on 7/2/17.
 */
class Authorization {
    companion object : KLogging()
     private val HMAC_SHA1 = "HmacSHA1"
  private  val kg = KeyGenerator.getInstance(HMAC_SHA1)
  private  val mac = Mac.getInstance(HMAC_SHA1)
  private  val users = HashMap<String, String>()
    init {
        mac.init(kg.generateKey())
        val connection = DBConnection.instance.open()
        try {
            val stmt = connection.prepareStatement("select name, secret from api_user")
            val rs = stmt.executeQuery()
            while (rs.next()) {
                users.put(rs.getString("name"), rs.getString("secret"))
            }
        } catch (e: Exception) {
            logger.error { e.printStackTrace() }
        } finally {
            DbUtils.close(connection)
        }
    }

    fun authorize(requestKey: String, message: String, user: String): Boolean {
        val token = users[user] ?: throw APIException("User not found")
        val dbKey = calculateHMAC(message,token)
        return (requestKey == dbKey)
    }

    private fun calculateHMAC(data: String, key: String): String {
        val secretKeySpec = SecretKeySpec(key.toByteArray(), HMAC_SHA1)
        val mac = Mac.getInstance(HMAC_SHA1)
        mac.init(secretKeySpec)
        return toHexString(mac.doFinal(data.toByteArray()))
    }

    private fun toHexString(bytes: ByteArray): String {
        val formatter = Formatter()
        for (b in bytes) {
            formatter.format("%02x", b)
        }
        return formatter.toString()
    }
}