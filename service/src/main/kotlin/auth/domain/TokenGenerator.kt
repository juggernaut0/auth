package auth.domain

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.*
import javax.inject.Inject

class TokenGenerator @Inject constructor(private val algorithm: Algorithm) {
    fun generate(userId: UUID): String {
        return JWT.create()
                .withSubject(userId.toString())
                .withIssuedAt(Date.from(Instant.now()))
                .sign(algorithm)
    }
}