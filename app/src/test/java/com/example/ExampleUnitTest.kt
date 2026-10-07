package com.example

import com.example.security.SecurityUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testPasswordHashingAndVerification() {
        val salt = SecurityUtils.generateSalt()
        val password = "SecurePassword123!"
        val hash = SecurityUtils.hashPassword(password, salt)

        assertTrue(SecurityUtils.verifyPassword(password, salt, hash))
        assertFalse(SecurityUtils.verifyPassword("WrongPassword", salt, hash))
    }

    @Test
    fun testSaltUniqueness() {
        val salt1 = SecurityUtils.generateSalt()
        val salt2 = SecurityUtils.generateSalt()
        assertNotEquals(salt1, salt2)
    }

    @Test
    fun testPairingCodeFormat() {
        val code = SecurityUtils.generatePairingCode()
        assertTrue(code.startsWith("FL-"))
        assertEquals(9, code.length) // "FL-" (3) + 2 + 4 = 9
    }

    @Test
    fun testPasswordStrengthEvaluation() {
        val weak = SecurityUtils.evaluatePasswordStrength("123", isParent = true)
        assertFalse(weak.isAcceptable)

        val strong = SecurityUtils.evaluatePasswordStrength("FocusParent#123", isParent = true)
        assertTrue(strong.isAcceptable)
        assertEquals("Strong", strong.label)
    }
}
