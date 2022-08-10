package com.kelin.proxyfactory.executors

import com.kelin.proxyfactory.ProxyFactory.isDebugMode
import java.util.concurrent.*

/**
 * Decorated [ThreadPoolExecutor]
 */
//@Singleton
class JobExecutor : ThreadExecutor {
    private val workQueue: BlockingQueue<Runnable>
    private var threadPoolExecutor: ThreadPoolExecutor
    private val threadFactory: ThreadFactory

    private fun reset() {
        threadPoolExecutor = ThreadPoolExecutor(
            0, Int.MAX_VALUE, 30, KEEP_ALIVE_TIME_UNIT,
            workQueue, threadFactory
        )
    }

    override fun execute(runnable: Runnable) {
        if (threadPoolExecutor.isShutdown) {
            reset()
        }
        if (!threadPoolExecutor.isShutdown) {
            threadPoolExecutor.execute(runnable)
            println("JobThreadFactory getTaskCount: " + threadPoolExecutor.taskCount + " getCompletedTaskCount: " + threadPoolExecutor.completedTaskCount + " getPoolSize: " + threadPoolExecutor.poolSize)
        } else {
            System.err.println("JobThreadFactory(isShutdown) getTaskCount: " + threadPoolExecutor.taskCount + " getCompletedTaskCount: " + threadPoolExecutor.completedTaskCount + " getPoolSize: " + threadPoolExecutor.poolSize)
        }
    }

    fun shutdown() {
        threadPoolExecutor.shutdownNow()
    }

    private class JobThreadFactory : ThreadFactory {
        private var counter = 0
        override fun newThread(runnable: Runnable): Thread {
            val thread: Thread = object : Thread(runnable, THREAD_NAME + counter++) {
                override fun run() {
                    try {
                        super.run()
                    } catch (e: Throwable) {
                        if (isDebugMode) {
                            throw e
                        }
                    }
                }
            }
            thread.isDaemon = true
            println("JobThreadFactory newThread: " + thread.name)
            return thread
        }

        companion object {
            private const val THREAD_NAME = "android_"
        }
    }

    companion object {
        private const val INITIAL_POOL_SIZE = 5
        private const val MAX_POOL_SIZE = 9

        // Sets the amount of time an idle thread waits before terminating
        private const val KEEP_ALIVE_TIME = 10

        // Sets the Time Unit to seconds
        private val KEEP_ALIVE_TIME_UNIT = TimeUnit.SECONDS
    }

    //@Inject
    init {
        workQueue = SynchronousQueue() //new LinkedBlockingQueue<>();
        threadFactory = JobThreadFactory()
        threadPoolExecutor = ThreadPoolExecutor(
            0, Int.MAX_VALUE, 30, KEEP_ALIVE_TIME_UNIT,
            workQueue, threadFactory
        )
    }
}