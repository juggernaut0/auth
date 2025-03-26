package auth.javalin

import auth.api.v1.*
import io.javalin.Javalin
import io.javalin.http.NotFoundResponse
import multiplatform.javalin.handleApi
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
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

class ProxyAuthHandler(private val baseUrl: String) {
    private val client = HttpClient.newHttpClient()

    fun registerRoutes(app: Javalin) {
        app.before("/auth/*") { ctx ->
            val pathWithQuery = ctx.path() + ctx.queryString()?.let { "?$it" }.orEmpty()
            val req = HttpRequest.newBuilder(URI("http://$baseUrl$pathWithQuery"))
            val body = if (ctx.contentLength() > 0) {
                HttpRequest.BodyPublishers.ofByteArray(ctx.bodyAsBytes())
            } else {
                HttpRequest.BodyPublishers.noBody()
            }
            req.method(ctx.method().name, body)
            val authHeader = ctx.header("Authorization")
            if (authHeader != null) {
                req.header("Authorization", authHeader)
            }

            val resp = client.send(req.build(), HttpResponse.BodyHandlers.ofInputStream())

            ctx.status(resp.statusCode()).result(resp.body()).skipRemainingHandlers()
        }
    }
}
