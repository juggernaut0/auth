package auth

class AuthConfig(
    val jwtSecretKey: String,
    val data: DataConfig,
    val app: AppConfig,
    val google: GoogleConfig,
)

class DataConfig(
    val user: String,
    val password: String,
    val jdbcUrl: String,
    val r2dbcUrl: String,
)

class AppConfig(
    val port: Int,
)

class GoogleConfig(
    val clientId: String,
    val mock: Mock,
) {
    class Mock(val enabled: Boolean)
}
