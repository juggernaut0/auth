package auth.api.v1

import multiplatform.UUIDSerializer
import multiplatform.api.ApiRoute
import multiplatform.api.Method.GET
import multiplatform.api.Method.POST
import multiplatform.api.pathOf
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.builtins.nullable
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import multiplatform.api.safeJson

val authJson = Json(safeJson) { serializersModule = authModule }

private const val PREFIX = "/auth/api/v1"
val register = ApiRoute(POST, pathOf(Unit.serializer(), "$PREFIX/account"), AuthenticatedUser.serializer(), PolymorphicSerializer(RegistrationRequest::class), authJson)
val lookup = ApiRoute(GET, pathOf(LookupParams.serializer(), "$PREFIX/account?id={id}&name={name}"), UserInfo.serializer().nullable)
val signIn = ApiRoute(POST, pathOf(Unit.serializer(), "$PREFIX/account/signin"), AuthenticatedUser.serializer(), PolymorphicSerializer(SignInRequest::class), authJson)
val validate = ApiRoute(GET, pathOf(Unit.serializer(), "$PREFIX/validate"), UUIDSerializer)
val getGoogleClientId = ApiRoute(GET, pathOf(Unit.serializer(), "$PREFIX/googleClientId"), String.serializer())
