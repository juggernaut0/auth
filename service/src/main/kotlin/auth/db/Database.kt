package auth.db

import io.r2dbc.spi.ConnectionFactory
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.reactive.asFlow
import kotlinx.coroutines.reactive.awaitSingle
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.reactivestreams.Publisher
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Database @Inject constructor(private val connectionFactory: ConnectionFactory) {
    suspend fun <T> transaction(block: suspend CoroutineScope.(DSLContext) -> T): T {
        val conn = connectionFactory.create().awaitSingle()
        try {
            conn.setAutoCommit(false).await()
            conn.beginTransaction().await()
            try {
                val dsl = DSL.using(conn, SQLDialect.POSTGRES)
                val res = coroutineScope { block(dsl) }
                conn.commitTransaction().await()
                return res
            } catch (e: Exception) {
                conn.rollbackTransaction().await()
                throw e
            }
        } finally {
            conn.close().await()
        }
    }
}

private suspend fun Publisher<Void>.await() {
    asFlow().collect {  }
}
