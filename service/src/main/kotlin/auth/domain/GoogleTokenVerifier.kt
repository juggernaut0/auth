package auth.domain

import auth.AuthConfig
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import javax.inject.Inject

class GoogleTokenVerifier @Inject constructor(
    private val config: AuthConfig,
    private val tokenVerifier: GoogleIdTokenVerifier,
) {
    suspend fun verify(token: String): VerifiedToken? {
        if (config.google.mock.enabled) {
            return VerifiedToken("", token)
        }
        val idToken = verifyToken(token) ?: run {
            log.warn("Failed to verify google token")
            return null
        }
        val payload = idToken.payload
        return VerifiedToken(payload.subject, payload.email)
    }

    private suspend fun verifyToken(token: String): GoogleIdToken? {
        return withContext(Dispatchers.IO) {
            @Suppress("BlockingMethodInNonBlockingContext")
            tokenVerifier.verify(token)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(GoogleTokenVerifier::class.java)
    }
}

data class VerifiedToken(val googleId: String, val email: String)
