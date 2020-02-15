@file:UseSerializers(UUIDSerializer::class)

package auth.api.v1

import multiplatform.UUID
import multiplatform.UUIDSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.UseSerializers
import kotlinx.serialization.modules.SerializersModule

val authModule = SerializersModule {
    polymorphic<SignInRequest> {
        PasswordSignInRequest::class with PasswordSignInRequest.serializer()
    }
    polymorphic<RegistrationRequest> {
        PasswordRegistrationRequest::class with PasswordRegistrationRequest.serializer()
    }
}

interface SignInRequest {
    val email: String
}

@Serializable
class PasswordSignInRequest(
        override val email: String,
        val password: String
) : SignInRequest

interface RegistrationRequest {
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
