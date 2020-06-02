package com.ifood

import com.newrelic.api.agent.Trace
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking

class Processor {

    companion object {
        @Trace
        suspend fun process(delayTime: Long = 1000L) {
            println("Processing...")
            doDelay(delayTime)
            println("Finished Processing...")
        }

        @Trace
        suspend fun doDelay(delayTime: Long): Int {
            println("I will delay $delayTime ms")
            delay(delayTime)
            return 1
        }
    }

}

fun main() {
    while (true) {
        runBlocking { trace { Processor.process() } }
        runBlockingWithSegment { Processor.process(1000L) }
        val job = GlobalScope.async { asyncWithSegment { Processor.process(3000L) }}
        runBlocking { job.await() }
        val job2 = GlobalScope.async { blockingWithMultipleAsyncsWithSegment(2) {Processor.process(4000L) }}
        runBlocking { job2.await() }
    }
}