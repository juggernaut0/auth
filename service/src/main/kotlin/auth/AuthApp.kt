package auth

import auth.api.v1.getGoogleClientId
import com.auth0.jwt.JWTVerifier
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.auth.jwt.jwt
import io.ktor.features.CallLogging
import io.ktor.features.StatusPages
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.jetty.Jetty
import kotlinx.serialization.json.Json
import multiplatform.ktor.JsonSerializationFeature
import multiplatform.ktor.handleApi
import multiplatform.ktor.installWebApplicationExceptionHandler
import org.slf4j.event.Level
import javax.inject.Inject

class AuthApp @Inject constructor(
        private val appJson: Json,
        private val config: AuthConfig,
        private val handler: AuthHandler,
        private val jwtVerifier: JWTVerifier
) {
    fun start() {
        embeddedServer(Jetty, config.app.port) {
            install(CallLogging) {
                level = Level.INFO
            }
            install(StatusPages) {
                installWebApplicationExceptionHandler()
            }
            install(JsonSerializationFeature) {
                json = appJson
            }
            install(Authentication) {
                jwt {
                    verifier(jwtVerifier)
                    validate { JWTPrincipal(it.payload) }
                }
            }
            routing {
                registerRoutes(handler)
                handleApi(getGoogleClientId) { config.google.clientId }
            }
        }.start(wait = true)
    }
}
