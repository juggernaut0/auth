package auth.javalin

import auth.api.v1.*
import io.javalin.Javalin
import io.javalin.http.NotFoundResponse
import multiplatform.javalin.handleApi
import java.util.*

class MockAuthHandler {
    private val mockToken = "mockToken"
    private val mockUserId = UUID.fromString("3e4097ef-3853-4829-9b56-67791b78a798")

    fun registerRoutes(app: Javalin) {
        app
            .before(validate.path.pathString()) { ctx ->
                val token = ctx.header("Authorization")
                if (token != "Bearer $mockToken") {
                    ctx.status(401)
                        .result("Unauthorized")
                        .skipRemainingHandlers()
                }
            }
            .handleApi(validate) { mockUserId }
            .handleApi(lookup) { UserInfo(mockUserId, null) }
            .handleApi(register) { AuthenticatedUser(mockUserId, mockToken) }
            .handleApi(signIn) { AuthenticatedUser(mockUserId, mockToken) }
            .handleApi(getGoogleClientId) { throw NotFoundResponse() }
    }
}
