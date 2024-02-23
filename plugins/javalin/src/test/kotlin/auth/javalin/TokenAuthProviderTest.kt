package auth.javalin

import auth.api.v1.validate
import io.javalin.Javalin
import io.javalin.testtools.JavalinTest
import io.mockk.every
import io.mockk.mockk
import multiplatform.UUID
import multiplatform.api.BlockingApiClient
import multiplatform.javalin.AuthenticationPlugin
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenAuthProviderTest {
    @Test
    fun happy() {
        val testId = UUID.randomUUID()
        val apiClient = mockk<BlockingApiClient> {
            every { callApi(validate, Unit, any()) } answers { throw RuntimeException("invalid") }
            every { callApi(validate, Unit, match { ("Authorization" to "Bearer foo") in it.toSet() }) } returns testId
        }

        val app = Javalin
            .create {
                it.registerPlugin(AuthenticationPlugin {
                    register(TokenAuthProvider(apiClient))
                })
            }
            .get("/test", {
                val p = it.with(AuthenticationPlugin::class) as ValidatedToken
                it.result(p.userId.toString())
            }, AuthenticatedRole)
            .get("/anon") {
                it.result("ok")
            }

        JavalinTest.test(app) { _, client ->
            with(client.get("/test") { it.header("Authorization", "Bearer foo") }) {
                assertEquals(200, code)
                assertEquals(testId.toString(), body!!.string())
            }
            with(client.get("/anon")) {
                assertEquals(200, code)
                assertEquals("ok", body!!.string())
            }
            with(client.get("/test")) {
                assertEquals(401, code)
            }
        }
    }
}
