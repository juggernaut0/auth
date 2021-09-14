@file:UseSerializers(UUIDSerializer::class)

package auth.api.v1

import multiplatform.UUID
import multiplatform.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

val authModule = SerializersModule {
    polymorphic(SignInRequest::class) {
        subclass(PasswordSignInRequest::class, PasswordSignInRequest.serializer())
        subclass(GoogleSignInRequest::class, GoogleSignInRequest.serializer())
    }
    polymorphic(RegistrationRequest::class) {
        subclass(PasswordRegistrationRequest::class, PasswordRegistrationRequest.serializer())
    }
}

sealed interface SignInRequest

@Serializable
class PasswordSignInRequest(
        val email: String,
        val password: String
) : SignInRequest

@Serializable
class GoogleSignInRequest(
    val googleToken: String,
) : SignInRequest

sealed interface RegistrationRequest {
    val email: String
    val displayName: String?
}

@Serializable
class PasswordRegistrationRequest(
        override val email: String,
        override val displayName: String? = null,
        val password: String
) : RegistrationRequest

@Serializable
class AuthenticatedUser(
        val id: UUID,
        val token: String
)

@Serializable
class LookupParams(
        val id: UUID? = null,
        val name: String? = null
)

@Serializable
class UserInfo(
        val id: UUID,
        val displayName: String?
)
