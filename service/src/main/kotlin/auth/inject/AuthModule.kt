package auth.inject

import auth.AppConfig
import auth.AuthConfig
import auth.api.v1.authModule
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
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
    fun appConfig(): AppConfig {
        return config.app
    }
}