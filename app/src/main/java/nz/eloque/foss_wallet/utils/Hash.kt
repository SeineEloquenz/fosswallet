package nz.eloque.foss_wallet.utils

import java.security.MessageDigest

object Hash {

    private val messageDigest = MessageDigest.getInstance("SHA-256")

    fun sha256(value: String): String {
        val hash = messageDigest.digest(value.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}