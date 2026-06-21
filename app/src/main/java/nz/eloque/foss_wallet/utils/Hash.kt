package nz.eloque.foss_wallet.utils

import java.security.MessageDigest

object Hash {
    fun sha256(value: String): String {
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val hash = messageDigest.digest(value.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
