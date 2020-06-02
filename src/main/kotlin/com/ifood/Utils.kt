package com.ifood

import com.newrelic.api.agent.NewRelic
import com.newrelic.api.agent.Trace
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.mpierce.kotlin.coroutines.newrelic.NewRelicTransaction

@Trace(dispatcher = true)
suspend fun trace(body: suspend () -> Unit) = body()

@Trace(dispatcher = true)
fun runBlockingWithSegment(body: suspend () -> Unit) {
    runBlocking { withContext(NewRelicTransaction(NewRelic.getAgent().transaction)) {
        val t = coroutineContext[NewRelicTransaction.Key]
        val s = t?.txn?.startSegment("dispatchBlocking")
        try {
            body()
        } finally {
            s?.end()
        }
    }
    }
}

@Trace(dispatcher = true)
suspend fun asyncWithSegment(body: suspend () -> Unit) {
    return withContext(NewRelicTransaction(NewRelic.getAgent().transaction)) {
        val t = coroutineContext[NewRelicTransaction.Key]
        val s = t?.txn?.startSegment("dispatchAsync")
        try {
            body()
        } finally {
            s?.end()
        }
    }
}

@Trace(dispatcher = true)
suspend fun blockingWithMultipleAsyncsWithSegment(times: Int, body: suspend () -> Unit) {
    return withContext(NewRelicTransaction(NewRelic.getAgent().transaction)) {
        val t = coroutineContext[NewRelicTransaction.Key]
        val s = t?.txn?.startSegment("dispatchAsync")
        try {
            val asyncs = mutableListOf<Job>()
            repeat(times) {
                asyncs.add(async { body() })
            }
            asyncs.forEach { it.join() }
        } finally {
            s?.end()
        }
    }
}