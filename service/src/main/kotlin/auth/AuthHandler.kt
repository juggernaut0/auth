package auth

import auth.api.v1.*
import auth.db.AuthDao
import auth.db.Database
import auth.domain.AuthProvider
import auth.domain.GoogleTokenVerifier
import auth.domain.PasswordHasher
import auth.domain.TokenGenerator
import io.ktor.auth.*
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
    authenticate(optional = true) {
        handleApi(lookup) {
            handler.lookup(params, auth)
        }
    }
    authenticate {
        handleApi(validate) { handler.extractSubject(auth) ?: error("Invalid principal") }
    }
}

class AuthHandler @Inject constructor(
        private val dao: AuthDao,
        private val database: Database,
        private val googleTokenVerifier: GoogleTokenVerifier,
        private val passwordHasher: PasswordHasher,
        private val tokenGenerator: TokenGenerator
) {
    fun extractSubject(principal: Principal?): UUID? {
        return (principal ?: return null).let { it as JWTPrincipal }.payload.subject.let { UUID.fromString(it) }
    }

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
        }
    }

    suspend fun signIn(signInRequest: SignInRequest): AuthenticatedUser {
        return when (signInRequest) {
            is PasswordSignInRequest -> passwordSignIn(signInRequest)
            is GoogleSignInRequest -> googleSignIn(signInRequest)
        } ?: throw BadRequestException("Sign in failed")
    }

    private suspend fun passwordSignIn(signInRequest: PasswordSignInRequest): AuthenticatedUser? {
        if (signInRequest.password.length !in PASSWORD_LENGTH_RANGE) {
            return null
        }

        return database.transaction { dsl ->
            val user = dao.getUserByEmail(dsl, signInRequest.email) ?: return@transaction null
            if (!AuthProvider.containsProvider(user.provider, AuthProvider.PASSWORD)) return@transaction null
            val hashedPass = dao.getUserPassByUserId(dsl, user.id) ?: return@transaction null
            if (passwordHasher.verify(signInRequest.password, hashedPass)) {
                val userId = user.id
                AuthenticatedUser(userId, tokenGenerator.generate(userId))
            } else {
                null
            }
        }
    }

    private suspend fun googleSignIn(signInRequest: GoogleSignInRequest): AuthenticatedUser? {
        val verifiedToken = googleTokenVerifier.verify(signInRequest.googleToken) ?: return null

        return database.transaction { dsl ->
            val user = dao.getUserByEmail(dsl, verifiedToken.email)

            if (user == null) {
                val newUser = dao.createGoogleUser(dsl, verifiedToken.email, verifiedToken.googleId)
                val userId = newUser.id
                AuthenticatedUser(userId, tokenGenerator.generate(userId))
            } else if (AuthProvider.containsProvider(user.provider, AuthProvider.GOOGLE)) {
                val userId = user.id
                AuthenticatedUser(userId, tokenGenerator.generate(userId))
            } else {
                null
            }
        }
    }

    suspend fun lookup(params: LookupParams, principal: Principal?): UserInfo? {
        return database.transaction { dsl ->
            if (params.id == null && params.name == null) {
                dao.lookupUserInfo(dsl, id = extractSubject(principal))
            } else {
                dao.lookupUserInfo(dsl, id = params.id, name = params.name)
            }?.let { (id, name) -> UserInfo(id, name) }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(AuthHandler::class.java)

        private val PASSWORD_LENGTH_RANGE = 8..64
    }
}