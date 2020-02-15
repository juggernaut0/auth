package auth

import io.ktor.application.call
import io.ktor.auth.*
import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.http.headersOf
import io.ktor.response.respond
import multiplatform.ktor.callApi
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.net.ConnectException
import java.util.*

class ValidatedToken(val userId: UUID): Principal

fun Authentication.Configuration.token(name: String? = null, httpClient: HttpClient) {
    val provider = TokenAuthProvider.Configuration(name, httpClient).build()
    provider.pipeline.intercept(AuthenticationPipeline.RequestAuthentication) { context ->
        val header = call.request.parseAuthorizationHeader()?.takeIf { it.authScheme == "Bearer" } as? HttpAuthHeader.Single
        val principal = header?.let { provider.validate(it.blob) }

        val cause = when {
            header == null -> AuthenticationFailedCause.NoCredentials
            principal == null -> AuthenticationFailedCause.InvalidCredentials
            else -> null
        }

        if (cause != null) {
            context.challenge("TokenAuth", cause) {
                call.respond(HttpStatusCode.Unauthorized, "Missing or invalid authorization header")
                it.complete()
            }
        } else {
            context.principal(principal!!)
        }
    }

    register(provider)
}

class TokenAuthProvider(private val configuration: Configuration) : AuthenticationProvider(configuration) {
    suspend fun validate(token: String): ValidatedToken? {
        val id = try {
            configuration.httpClient.callApi(auth.api.v1.validate, Unit, headers = headersOf(HttpHeaders.Authorization, "Bearer $token"))
        } catch (e: Exception) {
            when (e) {
                is ClientRequestException, is ConnectException -> log.warn("Failed to validate token", e)
                else -> log.error("Failed to validate token", e)
            }
            return null
        }
        return ValidatedToken(id)
    }

    class Configuration(name: String?, internal val httpClient: HttpClient) : AuthenticationProvider.Configuration(name) {
        internal fun build(): TokenAuthProvider {
            return TokenAuthProvider(this)
        }
    }
}

private val log = LoggerFactory.getLogger(TokenAuthProvider::class.java)
