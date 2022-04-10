package auth

import io.ktor.client.*
import io.ktor.client.plugins.*
import io.ktor.http.*
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.*
import io.ktor.server.auth.*
import io.ktor.server.response.respond
import multiplatform.ktor.KtorApiClient
import multiplatform.ktor.toMultiplatformHeaders
import org.slf4j.LoggerFactory
import java.net.ConnectException
import java.util.*

class ValidatedToken(val userId: UUID): Principal

fun AuthenticationConfig.token(name: String? = null, httpClient: HttpClient) {
    val provider = TokenAuthProvider.Configuration(name, httpClient).build()
    register(provider)
}

class TokenAuthProvider(configuration: Configuration) : AuthenticationProvider(configuration) {
    private val client = KtorApiClient(configuration.httpClient)

    override suspend fun onAuthenticate(context: AuthenticationContext) {
        val header = context.call.request.parseAuthorizationHeader()?.takeIf { it.authScheme == "Bearer" } as? HttpAuthHeader.Single
        val principal = header?.let { validate(it.blob) }

        val cause = when {
            header == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> {
                context.principal(principal)
                return
            }
        }

        context.challenge("TokenAuth", cause) { challenge, call ->
            call.respond(HttpStatusCode.Unauthorized, "Missing or invalid authorization header")
            challenge.complete()
        }
    }

    suspend fun validate(token: String): ValidatedToken? {
        val id = try {
            val headers = headersOf(HttpHeaders.Authorization, "Bearer $token").toMultiplatformHeaders()
            client.callApi(auth.api.v1.validate, Unit, headers = headers)
        } catch (e: Exception) {
            when (e) {
                is ClientRequestException, is ConnectException -> log.warn("Failed to validate token", e)
                else -> log.error("Failed to validate token", e)
            }
            return null
        }
        return ValidatedToken(id)
    }

    class Configuration(name: String?, internal val httpClient: HttpClient) : Config(name) {
        internal fun build(): TokenAuthProvider {
            return TokenAuthProvider(this)
        }
    }
}

private val log = LoggerFactory.getLogger(TokenAuthProvider::class.java)
