package com.kelin.proxyfactory.executors

/**
 * **描述:** 代理的执行器。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2022/8/10 12:56 PM
 *
 * **版本:** v 1.0.0
 */
object ProxyExecutors {
    /**
     * 用作线程池。
     */
    val jobExecutor: JobExecutor = JobExecutor()

    /**
     * UI线程。
     */
    val uiThread: UIThread = UIThread()
}