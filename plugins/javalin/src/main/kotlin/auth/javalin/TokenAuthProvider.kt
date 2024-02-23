package auth.javalin

import io.javalin.http.Context
import io.javalin.http.Header
import io.javalin.security.RouteRole
import multiplatform.api.BlockingApiClient
import multiplatform.api.Headers
import multiplatform.javalin.AuthenticationPlugin
import org.slf4j.LoggerFactory
import java.util.*

class ValidatedToken(val userId: UUID): AuthenticationPlugin.Principal

object AuthenticatedRole : RouteRole

class TokenAuthProvider(
    private val apiClient: BlockingApiClient,
) : AuthenticationPlugin.Provider {
    private val log = LoggerFactory.getLogger(TokenAuthProvider::class.java)

    override fun authenticate(context: Context): AuthenticationPlugin.Result {
        if (AuthenticatedRole !in context.routeRoles()) return AuthenticationPlugin.Result.Anonymous

        val header = context.header(Header.AUTHORIZATION) ?: return AuthenticationPlugin.Result.MissingCredentials
        if (!header.startsWith("Bearer ")) return AuthenticationPlugin.Result.InvalidCredentials
        val token = header.removePrefix("Bearer ")
        val validated = validate(token) ?: return AuthenticationPlugin.Result.InvalidCredentials
        return AuthenticationPlugin.Result.Authenticated(validated)
    }

    private fun validate(token: String): ValidatedToken? {
        val id = try {
            apiClient.callApi(auth.api.v1.validate, Unit, Headers.of(Header.AUTHORIZATION to "Bearer $token"))
        } catch (e: Exception) {
            log.error("Failed to validate token", e)
            return null
        }
        return ValidatedToken(id)
    }
}
