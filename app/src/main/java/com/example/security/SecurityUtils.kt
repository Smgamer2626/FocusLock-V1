package com.example.security

import java.security.MessageDigest
import java.security.SecureRandom

object SecurityUtils {
    private val secureRandom = SecureRandom()
    private const val CODE_ALPHABET = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ" // unambiguous characters

    fun generateSalt(): String {
        val bytes = ByteArray(16)
        secureRandom.nextBytes(bytes)
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun hashPassword(password: String, salt: String): String {
        val md = MessageDigest.getInstance("SHA-256")
        val input = "$salt:$password"
        val bytes = md.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    fun verifyPassword(password: String, salt: String, expectedHash: String): Boolean {
        val actualHash = hashPassword(password, salt)
        return MessageDigest.isEqual(
            actualHash.toByteArray(Charsets.UTF_8),
            expectedHash.toByteArray(Charsets.UTF_8)
        )
    }

    fun generatePairingCode(): String {
        val part1 = (0 until 2).map { CODE_ALPHABET[secureRandom.nextInt(CODE_ALPHABET.length)] }.joinToString("")
        val part2 = (0 until 4).map { CODE_ALPHABET[secureRandom.nextInt(CODE_ALPHABET.length)] }.joinToString("")
        return "FL-$part1$part2"
    }

    data class PasswordStrength(
        val isAcceptable: Boolean,
        val score: Float, // 0.0 to 1.0
        val label: String,
        val feedback: String
    )

    fun evaluatePasswordStrength(password: String, isParent: Boolean): PasswordStrength {
        if (password.isEmpty()) {
            return PasswordStrength(false, 0f, "Empty", "Enter a password")
        }
        var score = 0
        if (password.length >= 6) score++
        if (password.length >= 8) score++
        if (password.any { it.isUpperCase() }) score++
        if (password.any { it.isDigit() }) score++
        if (password.any { !it.isLetterOrDigit() }) score++

        val normalized = (score / 5f).coerceIn(0.1f, 1.0f)

        return when {
            score >= 4 -> PasswordStrength(
                isAcceptable = true,
                score = normalized,
                label = "Strong",
                feedback = "Great secure password"
            )
            score >= 2 -> {
                val acceptable = !isParent || password.length >= 8
                PasswordStrength(
                    isAcceptable = acceptable,
                    score = normalized,
                    label = if (score >= 3) "Good" else "Fair",
                    feedback = if (isParent && !acceptable) "Parents need at least 8 characters with numbers/letters" else "Sufficient"
                )
            }
            else -> PasswordStrength(
                isAcceptable = false,
                score = normalized,
                label = "Weak",
                feedback = "Must be at least 6 characters with letters and numbers"
            )
        }
    }
}
