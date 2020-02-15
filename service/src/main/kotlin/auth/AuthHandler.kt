package auth

import auth.api.v1.*
import auth.db.AuthDao
import auth.db.Database
import auth.domain.PasswordHasher
import auth.domain.TokenGenerator
import io.ktor.auth.authenticate
import io.ktor.auth.jwt.JWTPrincipal
import io.ktor.routing.Route
import multiplatform.ktor.BadRequestException
import multiplatform.ktor.handleApi
import org.jooq.exception.DataAccessException
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Inject

fun Route.registerRoutes(handler: AuthHandler) {
    handleApi(register) { handler.register(it) }
    handleApi(signIn) { handler.signIn(it) }
    //handleApi(lookup) { handler.lookup(params) } // TODO
    authenticate {
        handleApi(validate) { UUID.fromString((auth as JWTPrincipal).payload.subject) }
    }
}

class AuthHandler @Inject constructor(
        private val dao: AuthDao,
        private val database: Database,
        private val passwordHasher: PasswordHasher,
        private val tokenGenerator: TokenGenerator
) {
    suspend fun register(registrationRequest: RegistrationRequest): AuthenticatedUser {
        return when (registrationRequest) {
            is PasswordRegistrationRequest -> {
                if (registrationRequest.password.length !in PASSWORD_LENGTH_RANGE) {
                    throw BadRequestException("Password must be between ${PASSWORD_LENGTH_RANGE.first} and ${PASSWORD_LENGTH_RANGE.last} characters")
                }
                val hashedPass = passwordHasher.hash(registrationRequest.password)
                database.transaction { dsl ->
                    val userId = try {
                        dao.createPasswordUser(dsl, registrationRequest.email, hashedPass).id
                    } catch (e: DataAccessException) {
                        if (e.sqlState() == "23505") { // unique_violation
                            throw BadRequestException("Failed to register")
                        } else {
                            throw e
                        }
                    }
                    registrationRequest.displayName?.takeIf { it.isNotBlank() }?.let { name ->
                        if (dao.isNameTaken(dsl, name)) {
                            throw BadRequestException("Display name is taken")
                        }
                        dao.setDisplayName(dsl, userId, name)
                    }
                    AuthenticatedUser(userId, tokenGenerator.generate(userId))
                }
            }
            else -> throw BadRequestException("Unsupported registration type: ${registrationRequest::class}")
        }
    }

    suspend fun signIn(signInRequest: SignInRequest): AuthenticatedUser {
        return when (signInRequest) {
            is PasswordSignInRequest -> passwordSignIn(signInRequest)
            else -> throw BadRequestException("Unsupported signIn type: ${signInRequest::class}")
        } ?: throw BadRequestException("Sign in failed")
    }

    private suspend fun passwordSignIn(signInRequest: PasswordSignInRequest): AuthenticatedUser? {
        if (signInRequest.password.length !in PASSWORD_LENGTH_RANGE) {
            return null
        }

        return database.transaction { dsl ->
            val (userId, hashedPass) = dao.getUserPassByEmail(dsl, signInRequest.email) ?: return@transaction null
            if (passwordHasher.verify(signInRequest.password, hashedPass)) {
                AuthenticatedUser(userId, tokenGenerator.generate(userId))
            } else {
                null
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AuthHandler::class.java)

        private val PASSWORD_LENGTH_RANGE = 8..64
    }
}