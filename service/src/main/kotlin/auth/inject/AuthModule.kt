package auth.inject

import auth.AppConfig
import auth.AuthConfig
import auth.api.v1.authModule
import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import dagger.Module
import dagger.Provides
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import javax.sql.DataSource

@Module
class AuthModule(private val config: AuthConfig) {
    @Provides
    @Singleton
    fun dataSource(): DataSource {
        val config = HikariConfig().apply {
            dataSourceClassName = config.data.dataSourceClassName

            addDataSourceProperty("user", config.data.user)
            addDataSourceProperty("password", config.data.password)
            addDataSourceProperty("url", config.data.jdbcUrl)
        }
        return HikariDataSource(config)
    }

    @Provides
    @Singleton
    fun json(): Json {
        return Json(context = authModule)
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