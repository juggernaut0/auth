package auth

import auth.api.v1.getGoogleClientId
import com.auth0.jwt.JWTVerifier
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.jetty.*
import io.ktor.server.plugins.callloging.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.routing.*
import kotlinx.serialization.json.Json
import multiplatform.ktor.JsonSerializationPlugin
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
            install(JsonSerializationPlugin) {
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
