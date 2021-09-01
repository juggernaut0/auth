package auth

class AuthConfig(
        val jwtSecretKey: String,
        val data: DataConfig,
        val app: AppConfig
)

class DataConfig(
        val user: String,
        val password: String,
        val jdbcUrl: String,
        val r2dbcUrl: String,
)

class AppConfig(
        val port: Int
)
