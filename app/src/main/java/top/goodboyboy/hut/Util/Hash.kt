package top.goodboyboy.hut.Util

import java.security.MessageDigest

/**
 * 哈希校验Util
 *
 */
class Hash {
    companion object {
        /**
         * 哈希计算
         *
         * @param inputString 需要计算的字符串
         * @param algorithm 哈希算法
         * @return 哈希字符串
         */
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