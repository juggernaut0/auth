package auth.db

import auth.db.jooq.Tables.*
import auth.db.jooq.tables.records.AuthUserRecord
import auth.domain.AuthProvider
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.single
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.reactive.asFlow
import org.jooq.DSLContext
import org.jooq.impl.DSL
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject

class AuthDao @Inject constructor() {
    suspend fun createPasswordUser(dsl: DSLContext, email: String, hashedPass: String): AuthUserRecord {
        val userId = UUID.randomUUID()
        dsl.insertInto(AUTH_USER)
            .set(AUTH_USER.ID, userId)
            .set(AUTH_USER.EMAIL, email.trim())
            .set(AUTH_USER.PROVIDER, AuthProvider.PASSWORD)
            .asFlow()
            .single()

        dsl.insertInto(USER_PASS)
            .set(USER_PASS.ID, UUID.randomUUID())
            .set(USER_PASS.AUTH_USER_ID, userId)
            .set(USER_PASS.HASHED_PASS, hashedPass)
            .asFlow()
            .single()

        return dsl.selectFrom(AUTH_USER)
            .where(AUTH_USER.ID.eq(userId))
            .asFlow()
            .single()
    }

    suspend fun createGoogleUser(dsl: DSLContext, email: String, googleId: String): AuthUserRecord {
        val userId = UUID.randomUUID()
        dsl.insertInto(AUTH_USER)
            .set(AUTH_USER.ID, userId)
            .set(AUTH_USER.EMAIL, email)
            .set(AUTH_USER.PROVIDER, AuthProvider.GOOGLE)
            .asFlow()
            .single()

        dsl.insertInto(GOOGLE_SIGNIN_DETAILS)
            .set(GOOGLE_SIGNIN_DETAILS.ID, UUID.randomUUID())
            .set(GOOGLE_SIGNIN_DETAILS.AUTH_USER_ID, userId)
            .set(GOOGLE_SIGNIN_DETAILS.GOOGLE_ID, googleId)
            .asFlow()
            .single()

        return dsl.selectFrom(AUTH_USER)
            .where(AUTH_USER.ID.eq(userId))
            .asFlow()
            .single()
    }

    suspend fun isNameTaken(dsl: DSLContext, name: String): Boolean {
        val inner = DSL.selectDistinct(USER_DISPLAY_NAME.AUTH_USER_ID)
            .from(USER_DISPLAY_NAME)
            .where(DSL.lower(USER_DISPLAY_NAME.DISPLAY_NAME).eq(DSL.lower(name)))
        return dsl.selectFrom(USER_DISPLAY_NAME)
            .where(USER_DISPLAY_NAME.AUTH_USER_ID.`in`(inner))
            .asFlow()
            .toList()
            .groupBy { it.authUserId }
            .values
            .any { names -> names.maxByOrNull { it.effectiveDt }!!.displayName.equals(name, ignoreCase = true) }
    }

    suspend fun setDisplayName(dsl: DSLContext, userId: UUID, newName: String?) {
        dsl.insertInto(USER_DISPLAY_NAME)
            .set(USER_DISPLAY_NAME.ID, UUID.randomUUID())
            .set(USER_DISPLAY_NAME.AUTH_USER_ID, userId)
            .set(USER_DISPLAY_NAME.DISPLAY_NAME, newName)
            .set(USER_DISPLAY_NAME.EFFECTIVE_DT, LocalDateTime.now())
            .asFlow()
            .single()
    }

    suspend fun getUserByEmail(dsl: DSLContext, email: String): AuthUserRecord? {
        return dsl.selectFrom(AUTH_USER)
            .where(DSL.lower(AUTH_USER.EMAIL).eq(DSL.lower(email.trim())))
            .asFlow()
            .firstOrNull()
    }

    suspend fun getUserPassByUserId(dsl: DSLContext, userId: UUID): String? {
        return dsl.select(USER_PASS.HASHED_PASS)
            .from(USER_PASS)
            .where(USER_PASS.AUTH_USER_ID.eq(userId))
            .asFlow()
            .firstOrNull()
            ?.value1()
    }

    suspend fun lookupUserInfo(dsl: DSLContext, id: UUID? = null, name: String? = null): Pair<UUID, String?>? {
        if (id == null && name == null) return null

        val lookupId = when {
            id != null -> {
                val userExists =
                    dsl.selectCount()
                        .from(AUTH_USER)
                        .where(AUTH_USER.ID.eq(id))
                        .asFlow()
                        .single()
                        .value1() > 0

                if (!userExists) return null
                id
            }
            name != null -> lookupIdByName(dsl, name)
            else -> null
        } ?: return null

        val lookupName = name ?: lookupNameById(dsl, lookupId)

        return (lookupId to lookupName)
            .takeIf { name == null || name == lookupName }
    }

    private suspend fun lookupNameById(dsl: DSLContext, id: UUID): String? {
        return dsl.select(USER_DISPLAY_NAME.DISPLAY_NAME)
            .from(USER_DISPLAY_NAME)
            .where(USER_DISPLAY_NAME.AUTH_USER_ID.eq(id))
            .orderBy(USER_DISPLAY_NAME.EFFECTIVE_DT.desc())
            .asFlow()
            .firstOrNull()
            ?.value1()
    }

    private suspend fun lookupIdByName(dsl: DSLContext, name: String): UUID? {
        return dsl.select(USER_DISPLAY_NAME.AUTH_USER_ID)
            .from(USER_DISPLAY_NAME)
            .where(USER_DISPLAY_NAME.DISPLAY_NAME.eq(name))
            .orderBy(USER_DISPLAY_NAME.EFFECTIVE_DT.desc())
            .asFlow()
            .firstOrNull()
            ?.value1()
    }
}