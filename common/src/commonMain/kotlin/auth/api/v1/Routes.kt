package auth.api.v1

import multiplatform.UUIDSerializer
import multiplatform.api.ApiRoute
import multiplatform.api.Method.GET
import multiplatform.api.Method.POST
import multiplatform.api.pathOf
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer

private const val PREFIX = "/auth/api/v1"
val register = ApiRoute(POST, pathOf(Unit.serializer(), "$PREFIX/account"), AuthenticatedUser.serializer(), PolymorphicSerializer(RegistrationRequest::class))
val lookup = ApiRoute(GET, pathOf(LookupParams.serializer(), "$PREFIX/account?id={id}&name={name}"), UserInfo.serializer().nullable)
val signIn = ApiRoute(POST, pathOf(Unit.serializer(), "$PREFIX/account/signin"), AuthenticatedUser.serializer(), PolymorphicSerializer(SignInRequest::class))
val validate = ApiRoute(GET, pathOf(Unit.serializer(), "$PREFIX/validate"), UUIDSerializer)