package auth.db

import auth.db.jooq.Tables.*
import auth.db.jooq.tables.records.AuthUserRecord
import auth.domain.AuthProvider
import kotlinx.coroutines.future.await
import org.jooq.DSLContext
import org.jooq.Record2
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
                .executeAsync()
                .await()

        dsl.insertInto(USER_PASS)
                .set(USER_PASS.ID, UUID.randomUUID())
                .set(USER_PASS.AUTH_USER_ID, userId)
                .set(USER_PASS.HASHED_PASS, hashedPass)
                .executeAsync()
                .await()

        return dsl.selectFrom(AUTH_USER)
                .where(AUTH_USER.ID.eq(userId))
                .fetchAsync()
                .await()
                .single()
    }

    suspend fun isNameTaken(dsl: DSLContext, name: String): Boolean {
        val inner = DSL.selectDistinct(USER_DISPLAY_NAME.AUTH_USER_ID)
                .from(USER_DISPLAY_NAME)
                .where(DSL.lower(USER_DISPLAY_NAME.DISPLAY_NAME).eq(DSL.lower(name)))
        return dsl.selectFrom(USER_DISPLAY_NAME)
                .where(USER_DISPLAY_NAME.AUTH_USER_ID.`in`(inner))
                .fetchAsync()
                .await()
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
                .executeAsync()
                .await()
    }

    suspend fun getUserPassByEmail(dsl: DSLContext, email: String): Record2<UUID, String>? {
        return dsl.select(AUTH_USER.ID, USER_PASS.HASHED_PASS)
                .from(AUTH_USER.join(USER_PASS).onKey())
                .where(DSL.lower(AUTH_USER.EMAIL).eq(DSL.lower(email.trim())))
                .fetchAsync()
                .await()
                .firstOrNull()
    }
}