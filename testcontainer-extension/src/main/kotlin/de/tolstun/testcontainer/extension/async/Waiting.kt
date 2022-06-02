package de.tolstun.testcontainer.extension.async

import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit


class Waiting {


    private val releaseLatch = CountDownLatch(1)


    fun waitProcess(timeout: Long = 5000,
                    unit: TimeUnit = TimeUnit.MILLISECONDS,
                    ready: () -> Boolean) {

        val timeoutInMilliseconds = TimeUnit.MILLISECONDS.convert(timeout, unit)
        val oneCycleTimeoutInMilliseconds = 200L
        var numberOfCycles = timeoutInMilliseconds / oneCycleTimeoutInMilliseconds

        do {
            awaitCompletion(oneCycleTimeoutInMilliseconds, TimeUnit.MILLISECONDS)
            numberOfCycles --
        } while (!ready() && numberOfCycles > 0)

        completeNow()
    }


    fun <T> waitOfResult(timeout: Long = 5000,
                         unit: TimeUnit = TimeUnit.MILLISECONDS,
                         process: () -> T?): T? {

        val timeoutInMilliseconds = TimeUnit.MILLISECONDS.convert(timeout, unit)
        val oneCycleTimeoutInMilliseconds = 200L
        var numberOfCycles = timeoutInMilliseconds / oneCycleTimeoutInMilliseconds
        var result: T?

        do {
            awaitCompletion(oneCycleTimeoutInMilliseconds, TimeUnit.MILLISECONDS)
            numberOfCycles --
            result = process()
        } while (result == null && numberOfCycles > 0)

        completeNow()

        return result
    }


    @Synchronized
    fun completeNow() = releaseLatch.countDown()


    @Throws(InterruptedException::class)
    fun awaitCompletion(timeout: Long, unit: TimeUnit? = TimeUnit.MILLISECONDS): Boolean {

        return releaseLatch.await(timeout, unit ?: TimeUnit.MILLISECONDS)
    }


}