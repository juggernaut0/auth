package auth.inject

import auth.AuthConfig
import auth.api.v1.authModule
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.apache.v2.ApacheHttpTransport
import com.google.api.client.json.gson.GsonFactory
import dagger.Module
import dagger.Provides
import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import kotlinx.serialization.json.Json
import javax.inject.Singleton

@Module
class AuthModule(private val config: AuthConfig) {
    @Provides
    fun connectionFactory(): ConnectionFactory {
        val options = ConnectionFactoryOptions.parse(config.data.r2dbcUrl)
            .mutate()
            .option(ConnectionFactoryOptions.USER, config.data.user)
            .option(ConnectionFactoryOptions.PASSWORD, config.data.password)
            .build()
        return ConnectionFactories.get(options)
    }

    @Provides
    @Singleton
    fun json(): Json {
        return Json { serializersModule = authModule }
    }

    @Provides
    @Singleton
    fun jwtAlgorithm(): Algorithm {
        return Algorithm.HMAC256(config.jwtSecretKey)
    }

    @Provides
    fun jwtVerifier(algorithm: Algorithm): JWTVerifier {
        return JWT.require(algorithm).build()
    }

    @Provides
    @Singleton
    fun googleIdTokenVerifier(): GoogleIdTokenVerifier {
        return GoogleIdTokenVerifier.Builder(ApacheHttpTransport(), GsonFactory())
            .setAudience(listOf(config.google.clientId))
            .build()
    }

    @Provides
    fun config(): AuthConfig {
        return config
    }
}