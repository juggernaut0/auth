package auth.domain

import at.favre.lib.crypto.bcrypt.BCrypt
import javax.inject.Inject

class PasswordHasher @Inject constructor() {
    private val hasher: BCrypt.Hasher = BCrypt.withDefaults()
    private val verifier: BCrypt.Verifyer = BCrypt.verifyer()

    fun hash(raw: String): String {
        return hasher.hashToString(12, raw.toCharArray())
    }

    fun verify(password: String, storedHash: String): Boolean {
        return verifier.verify(password.toCharArray(), storedHash.toCharArray()).verified
    }
}