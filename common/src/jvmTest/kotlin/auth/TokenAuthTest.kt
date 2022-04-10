package auth

import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.testing.*
import kotlin.test.Test
import kotlin.test.assertEquals

class TokenAuthTest {
    @Test
    fun happy() {
        val id = "08bb0a38-e0e2-405f-a8e6-488f5927000f"
        val authClient = HttpClient(MockEngine {
            respond(
                status = HttpStatusCode.OK,
                content = "\"$id\""
            )
        })

        testApplication {
            install(Authentication) {
                token(httpClient = authClient)
            }

            routing {
                authenticate {
                    get {
                        val userId = call.principal<ValidatedToken>()!!.userId
                        call.respondText("hello $userId")
                    }
                }
            }

            with(client.get("/") {
                header("Authorization", "Bearer foo.bar.sig")
            }) {
                assertEquals(HttpStatusCode.OK, status)
                assertEquals("hello $id", bodyAsText())
            }

            with(client.get("/")) {
                assertEquals(HttpStatusCode.Unauthorized, status)
            }
        }
    }

    @Test
    fun forbidden() {
        val authClient = HttpClient(MockEngine {
            respond(
                status = HttpStatusCode.BadRequest,
                content = ""
            )
        })

        testApplication {
            install(Authentication) {
                token(httpClient = authClient)
            }

            routing {
                authenticate {
                    get { call.respondText("hello") }
                }
            }

            with(client.get("/") {
                header("Authorization", "Bearer foo.bar.sig")
            }) {
                assertEquals(HttpStatusCode.Unauthorized, status)
            }
        }
    }
}
