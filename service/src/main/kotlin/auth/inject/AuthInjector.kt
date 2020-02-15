package auth.inject

import auth.AuthApp
import dagger.Component
import javax.inject.Singleton

@Component(modules = [AuthModule::class])
@Singleton
interface AuthInjector {
    fun app(): AuthApp
}