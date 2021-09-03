package auth

import auth.api.v1.*
import io.ktor.client.HttpClient
import io.ktor.client.engine.apache.Apache
import io.ktor.client.features.defaultRequest
import io.ktor.http.HttpHeaders
import io.ktor.http.headersOf
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import multiplatform.ktor.*
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull

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
                install(JsonSerializationClientFeature) {
                    json = Json { serializersModule = authModule }
                }
            }.let { KtorApiClient(it) }
        }
    }
}