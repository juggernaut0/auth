package auth

class AuthConfig(
        val jwtSecretKey: String,
        val data: DataConfig,
        val app: AppConfig
)

class DataConfig(
        val dataSourceClassName: String,
        val user: String,
        val password: String,
        val jdbcUrl: String
)

class AppConfig(
        val port: Int
)
