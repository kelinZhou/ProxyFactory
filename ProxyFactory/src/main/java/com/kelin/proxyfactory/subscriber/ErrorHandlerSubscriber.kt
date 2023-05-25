package com.kelin.proxyfactory.subscriber

import androidx.annotation.CallSuper
import com.kelin.apiexception.ApiException
import com.kelin.proxyfactory.ProxyFactory
import com.kelin.proxyfactory.Toaster

/**
 * **描述:** 错误处理的的观察者。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019/3/9  11:01 AM
 *
 * **版本:** v 1.0.0
 */
abstract class ErrorHandlerSubscriber<T>(private val toaster: Toaster) : UseCaseSubscriber<T>(), Achievable {//api错误处理的观察者

    override val isAchieved: Boolean
        get() = isDisposed

    override fun achieved() {
        if (!isDisposed) {
            dispose()
        }
        onFinished(null)
    }

    /**
     * 当结束时被调用。
     * @param successful 是否是成功的，true表示成功，false表示失败，null表示用户手动中断(例如用户取消订阅)。
     */
    @CallSuper
    open fun onFinished(successful: Boolean?) {
        if (isDisposed) {
            dispose()
        }
    }

    protected abstract fun onError(ex: ApiException)

    protected abstract fun onSuccess(t: T)


    final override fun onError(e: Throwable) {
        doOnError(e)
        onFinished(false)
    }

    private fun doOnError(e: Throwable) {
        try {
            printError(e, "ErrorHandlerSubscribe")
            dealError(e)
        } catch (e: Exception) {
            if (ProxyFactory.isDebugMode) {
                e.printStackTrace()
            }
        }
    }

    protected open fun dealError(e: Throwable) {
        toaster.handError(e)?.also { onError(it) }
    }

    protected fun printError(e: Throwable, tip: String) {
        if (ProxyFactory.isDebugMode) {
            e.printStackTrace()
            print("ErrorHandlerSubscriber===============$tip===============")
            print("ErrorHandlerSubscriber ${e.localizedMessage}")
        }
    }

    override fun onNext(t: T) {
        try {
            onSuccess(t)
        } catch (e: Exception) {
            printError(e, "onNext error")
            doOnError(e)
        }
    }

    override fun onComplete() {
        try {
            onFinished(true)
        } catch (e: Exception) {
            printError(e, "onFinished error")
            doOnError(e)
        }
    }
}