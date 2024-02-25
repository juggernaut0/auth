package auth

import auth.inject.AuthModule
import auth.inject.DaggerAuthInjector
import com.typesafe.config.ConfigFactory
import dev.twarner.auth.db.DbMigration
import io.github.config4k.extract

fun main() {
    val config: AuthConfig = ConfigFactory.load().extract()
    DbMigration.runMigrations(config.data.jdbcUrl, config.data.user, config.data.password)
    DaggerAuthInjector
        .builder()
        .authModule(AuthModule(config))
        .build()
        .app()
        .start()
}
