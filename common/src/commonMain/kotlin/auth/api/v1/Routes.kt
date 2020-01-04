package auth.api.v1

import juggernaut0.multiplatform.UUIDSerializer
import juggernaut0.multiplatform.api.ApiRoute
import juggernaut0.multiplatform.api.Method.GET
import juggernaut0.multiplatform.api.Method.POST
import juggernaut0.multiplatform.api.pathOf
import kotlinx.serialization.PolymorphicSerializer
import kotlinx.serialization.internal.UnitSerializer
import kotlinx.serialization.internal.nullable

private const val PREFIX = "/auth/api/v1"
val register = ApiRoute(POST, pathOf(UnitSerializer, "$PREFIX/account"), AuthenticatedUser.serializer(), PolymorphicSerializer(RegistrationRequest::class))
val lookup = ApiRoute(GET, pathOf(LookupParams.serializer(), "$PREFIX/account?id={id}&name={name}"), UserInfo.serializer().nullable)
val signIn = ApiRoute(POST, pathOf(UnitSerializer, "$PREFIX/account/signin"), AuthenticatedUser.serializer(), PolymorphicSerializer(SignInRequest::class))
val validate = ApiRoute(GET, pathOf(UnitSerializer, "$PREFIX/validate"), UUIDSerializer)