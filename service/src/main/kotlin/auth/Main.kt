package auth

import auth.inject.AuthModule
import auth.inject.DaggerAuthInjector
import com.typesafe.config.ConfigFactory
import io.github.config4k.extract

fun main() {
    DaggerAuthInjector
            .builder()
            .authModule(AuthModule(ConfigFactory.load().extract()))
            .build()
            .app()
            .start()
}
