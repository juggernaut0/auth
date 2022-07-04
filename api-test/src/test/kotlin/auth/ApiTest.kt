package auth

import auth.api.v1.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.plugins.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import multiplatform.api.Headers
import multiplatform.ktor.*
import kotlin.random.Random
import kotlin.test.*

class ApiTest {
    @Test
    fun passwordRegisterTest() {
        val client = buildClient()
        runBlocking {
            val resp = client.callApi(register, Unit, PasswordRegistrationRequest(
                    email = genEmail(),
                    password = "password1"
            ))
            val userId = resp.id
            val token = resp.token
            val validatedId = client.callApi(validate, Unit, headers = headersOf(HttpHeaders.Authorization, "Bearer $token").toMultiplatformHeaders())
            assertEquals(userId, validatedId)
        }
    }

    @Test
    fun registerDuplicate() {
        val client = buildClient()
        val email = genEmail()
        runBlocking {
            val resp = client.callApi(register, Unit, PasswordRegistrationRequest(
                    email = email,
                    password = "password1"
            ))

            // Same email should fail
            assertFails {
                client.callApi(register, Unit, PasswordRegistrationRequest(
                        email = email,
                        password = "password2"
                ))
            }

            // First token should still be valid
            val userId = resp.id
            val token = resp.token
            val validatedId = client.callApi(validate, Unit, headers = headersOf(HttpHeaders.Authorization, "Bearer $token").toMultiplatformHeaders())
            assertEquals(userId, validatedId)
        }
    }

    @Test
    fun registerDuplicateWithWhitespace() {
        val client = buildClient()
        val email = genEmail()
        runBlocking {
            val resp = client.callApi(register, Unit, PasswordRegistrationRequest(
                    email = email,
                    password = "password1"
            ))

            // Same email with whitespace should fail
            assertFails {
                client.callApi(register, Unit, PasswordRegistrationRequest(
                        email = "\t$email  \n",
                        password = "password2"
                ))
            }

            // First token should still be valid
            val userId = resp.id
            val token = resp.token
            val validatedId = client.callApi(validate, Unit, headers = headersOf(HttpHeaders.Authorization, "Bearer $token").toMultiplatformHeaders())
            assertEquals(userId, validatedId)
        }
    }

    @Test
    fun passwordSignInTest() {
        val client = buildClient()
        runBlocking {
            val email = genEmail()
            val password = "password1"
            val userId = client.callApi(register, Unit, PasswordRegistrationRequest(
                    email = email,
                    password = password
            )).id

            val signInResp = client.callApi(signIn, Unit, PasswordSignInRequest(
                    email = email,
                    password = password
            ))
            assertEquals(userId, signInResp.id)

            val token = signInResp.token
            val validatedId = client.callApi(validate, Unit, headers = headersOf(HttpHeaders.Authorization, "Bearer $token").toMultiplatformHeaders())
            assertEquals(userId, validatedId)
        }
    }

    @Test
    fun passwordSignInWithWhitespace() {
        val client = buildClient()
        runBlocking {
            val email = genEmail()
            val password = "password1"
            val userId = client.callApi(register, Unit, PasswordRegistrationRequest(
                    email = email,
                    password = password
            )).id

            val signInResp = client.callApi(signIn, Unit, PasswordSignInRequest(
                    email = "\t$email  \n",
                    password = password
            ))
            assertEquals(userId, signInResp.id)

            val token = signInResp.token
            val validatedId = client.callApi(validate, Unit, headers = headersOf(HttpHeaders.Authorization, "Bearer $token").toMultiplatformHeaders())
            assertEquals(userId, validatedId)
        }
    }

    @Test
    fun signInWrongEmail() {
        val client = buildClient()
        runBlocking {
            val email = genEmail()
            val password = "password1"
            client.callApi(register, Unit, PasswordRegistrationRequest(
                    email = email,
                    password = password
            )).id

            assertFails {
                client.callApi(signIn, Unit, PasswordSignInRequest(
                        email = "wrong_email@gmail.com",
                        password = password
                ))
            }
        }
    }

    @Test
    fun signInWrongPassword() {
        val client = buildClient()
        runBlocking {
            val email = genEmail()
            val password = "password1"
            client.callApi(register, Unit, PasswordRegistrationRequest(
                    email = email,
                    password = password
            )).id

            assertFails {
                client.callApi(signIn, Unit, PasswordSignInRequest(
                        email = email,
                        password = "wrong_password"
                ))
            }
        }
    }

    @Test
    fun invalidToken() {
        val client = buildClient()
        runBlocking {
            assertFails {
                val token = "ThisIsNotARealToken.NoReallyItIsnt.NotEvenSignedProperly"
                client.callApi(validate, Unit, headers = headersOf(HttpHeaders.Authorization, "Bearer $token").toMultiplatformHeaders())
            }
        }
    }

    @Test
    fun lookup() {
        val client = buildClient()
        runBlocking {
            val email = genEmail()
            val password = "password1"
            val displayName = "test${Random.nextInt()}"
            val user = client.callApi(register, Unit, PasswordRegistrationRequest(
                email = email,
                password = password,
                displayName = displayName,
            ))

            run {
                val headers = headersOf(HttpHeaders.Authorization, "Bearer ${user.token}").toMultiplatformHeaders()
                val userInfo = client.callApi(lookup, LookupParams(), headers = headers)
                assertNotNull(userInfo)
                assertEquals(user.id, userInfo.id)
                assertEquals(displayName, userInfo.displayName)
            }

            run {
                val userInfo = client.callApi(lookup, LookupParams(id = user.id))
                assertNotNull(userInfo)
                assertEquals(user.id, userInfo.id)
                assertEquals(displayName, userInfo.displayName)
            }

            run {
                val userInfo = client.callApi(lookup, LookupParams(name = displayName))
                assertNotNull(userInfo)
                assertEquals(user.id, userInfo.id)
                assertEquals(displayName, userInfo.displayName)
            }
        }
    }

    @Test
    fun lookupAnother() {
        val client = buildClient()
        runBlocking {
            val displayName = "test${Random.nextInt()}"
            val user = client.callApi(register, Unit, PasswordRegistrationRequest(
                email = genEmail(),
                password = "password1",
                displayName = displayName,
            ))

            val token = client.callApi(register, Unit, PasswordRegistrationRequest(
                email = genEmail(),
                password = "password1",
            )).token

            run {
                val headers = headersOf(HttpHeaders.Authorization, "Bearer $token").toMultiplatformHeaders()
                val userInfo = client.callApi(lookup, LookupParams(id = user.id), headers = headers)
                assertNotNull(userInfo)
                assertEquals(user.id, userInfo.id)
                assertEquals(displayName, userInfo.displayName)
            }

            run {
                val headers = headersOf(HttpHeaders.Authorization, "Bearer $token").toMultiplatformHeaders()
                val userInfo = client.callApi(lookup, LookupParams(name = displayName), headers = headers)
                assertNotNull(userInfo)
                assertEquals(user.id, userInfo.id)
                assertEquals(displayName, userInfo.displayName)
            }
        }
    }

    @Test
    fun googleSignInBasic() {
        val client = buildClient()
        runBlocking {
            val id1 = run {
                val user = client.callApi(signIn, Unit, GoogleSignInRequest("test.token"))
                val userId = user.id
                val token = user.token
                val validatedId = client.callApi(validate, Unit, headers = authorizationHeaders(token))
                assertEquals(userId, validatedId)
                userId
            }

            val id2 = run {
                val user = client.callApi(signIn, Unit, GoogleSignInRequest("test.token"))
                val userId = user.id
                val token = user.token
                val validatedId = client.callApi(validate, Unit, headers = authorizationHeaders(token))
                assertEquals(userId, validatedId)
                userId
            }

            assertEquals(id1, id2)
        }
    }

    @Test
    fun googleSignInExisting() {
        val client = buildClient()
        runBlocking {
            val email = genEmail()
            client.callApi(register, Unit, PasswordRegistrationRequest(
                email = email,
                password = "password1"
            ))

            val e = assertFailsWith<ClientRequestException> {
                client.callApi(signIn, Unit, GoogleSignInRequest(email)) // verifier mock uses whole token as email
            }
            assertEquals(HttpStatusCode.BadRequest, e.response.status)
        }
    }

    companion object {
        fun genEmail(): String {
            val id = Random.nextInt()
            return "test+$id@example.com"
        }

        fun buildClient(): KtorApiClient {
            return HttpClient(Apache) {
                defaultRequest {
                    url {
                        host = "localhost"
                        port = 9001
                    }
                }
                install(JsonSerializationClientPlugin) {
                    json = Json { serializersModule = authModule }
                }
                expectSuccess = true
            }.let { KtorApiClient(it) }
        }

        private fun authorizationHeaders(token: String): Headers {
            return headersOf(HttpHeaders.Authorization, "Bearer $token").toMultiplatformHeaders()
        }
    }
}