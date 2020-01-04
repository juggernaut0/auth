import org.flywaydb.core.Flyway
import java.net.URI

fun runMigrations(url: String, user: String, pass: String) {
    Flyway.configure()
        .dataSource(url, user, pass)
        .load()
        .migrate()
}

fun main(args: Array<String>) {
    //expects format: postgres://user:pass@host:port/path
    val env = System.getenv("DATABASE_URL") ?: args.getOrNull(0) ?: throw RuntimeException("Must provide database URL")
    val uri = URI(env)
    val (user, pass) = uri.userInfo.split(":")
    val url = URI("jdbc:postgresql", null, uri.host, uri.port, uri.path, uri.rawQuery, uri.rawFragment).toString()
    runMigrations(url, user, pass)
}