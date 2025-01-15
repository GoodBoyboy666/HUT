package top.goodboyboy.hut.Util

import java.security.MessageDigest

class Hash {
    companion object {
        fun hash(inputString: String, algorithm: String = "SHA-256"): String {
            val bytes = inputString.toByteArray()
            val md = MessageDigest.getInstance(algorithm)
            val digest = md.digest(bytes)
            val hexString = StringBuffer()
            for (byte in digest) {
                hexString.append(String.format("%02x", byte))
            }
            return hexString.toString()
        }
    }
}