package com.kelin.proxyfactory

import android.content.Context
import android.net.ConnectivityManager
import android.util.LruCache
import android.util.SparseArray
import androidx.annotation.CallSuper
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.kelin.apiexception.ApiException
import com.kelin.logger.Logger
import com.kelin.proxyfactory.exception.ProxyLogicError
import com.kelin.proxyfactory.subscriber.ErrorHandlerSubscriber
import com.kelin.proxyfactory.usecase.UseCase
import io.reactivex.observers.DisposableObserver
import java.lang.Exception

/**
 * **描述:** 有请求参数的网络请求代理。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019/4/1  1:42 PM
 *
 * **版本:** v 1.0.0
 */
abstract class IdActionDataProxy<ID, D>(protected val toaster: Toaster) : LifecycleObserver {

    companion object {
        private const val CONSTANT_CACHE_KEY = 1
        private val mUseCaseMap = LruCache<Int, UseCase<*>>(5)

        internal fun clearUseCase() {
            mUseCaseMap.evictAll()
        }
    }

    private var context: Context? = null
    private var progressText: String? = null

    private var uncheckNetWork = false

    protected var noToast = false

    private val defaultRequestId = Any()

    var isWorking: Boolean = false
        private set

    val isNotWorking: Boolean
        get() = !isWorking

    private val isNetWorkConnected: Boolean
        get() = ContextCompat.getSystemService(context ?: ProxyFactory.getContext(), ConnectivityManager::class.java)?.activeNetworkInfo?.isConnected == true

    protected var mGlobalCallback: IdActionDataCallback<ID, ActionParameter, D>? = null
    private var mSubscriptions: ExtCompositeSubscription? = null
    private val mErrorCount = SparseArray<ErrorWrapper>()

    val isBound: Boolean
        get() = mGlobalCallback != null


    open fun setNotToast(): IdActionDataProxy<ID, D> {
        noToast = true
        return this
    }

    protected abstract fun createUseCase(id: ID, action: ActionParameter): UseCase<D>

    protected open fun checkNetworkEnable(id: ID, action: ActionParameter): Boolean = true

    /**
     * 执行请求，调用该方法使代理去做他该做的事情。
     * @param action 动作，用来表示当前请求是如何触发的。
     * @param id 请求参数。
     */
    fun request(action: ActionParameter, id: ID) {
        isWorking = true
        context?.also { toaster.showProgress(it, progressText) }
        var e = checkPreCondition(id, action)
        val observer = createCallback(id, action)
        if (e != null) {
            observer.onError(e)
            observer.onComplete()
            observer.dispose()
        }

        if (isExceedMaxErrorCount(id, action)) {
            e = ApiException(ProxyLogicError.FAIL_TOO_MUCH)
            observer.onError(e)
            observer.onComplete()
            observer.dispose()
        }

        @Suppress("unchecked_cast")
        var useCase = mUseCaseMap.get(getCacheKey(id, action)) as? UseCase<D>
        if (useCase == null) {
            useCase = createUseCase(id, action)
            mUseCaseMap.put(getCacheKey(id, action), useCase)
        }

        useCase.execute(observer)

        mSubscriptions?.add(observer)
    }

    private fun isExceedMaxErrorCount(id: ID, action: ActionParameter): Boolean {
        val key = getCacheKey(id, action)
        val errorWrapper = mErrorCount.get(key) ?: return false

        if (errorWrapper.count.toInt() > ErrorWrapper.MAX_ERROR_COUNT) {
            val current = System.currentTimeMillis()

            if (current - errorWrapper.lastTime < ErrorWrapper.MAX_ERROR_PERIOD) {
                return true
            } else {
                errorWrapper.count.set(0)
            }
        }
        return false
    }

    private fun getCacheKey(id: ID, action: ActionParameter): Int {
        var result = id?.hashCode() ?: CONSTANT_CACHE_KEY
        result = 31 * result + action.hashCode()
        return result
    }

    private fun createCallback(id: ID, action: ActionParameter): DisposableObserver<D> {
        return IdActionCaseSubscriber(id, action)
    }

    private fun checkPreCondition(id: ID, action: ActionParameter): ApiException? {
        return if (uncheckNetWork || !checkNetworkEnable(id, action)) {
            null
        } else {
            if (!isNetWorkConnected) {
                ApiException(ProxyLogicError.NETWORK_UNAVAILABLE)
            } else {
                null
            }
        }
    }

    /**
     * 将Proxy于声明周期绑定，由于绑定后将会减少垃圾的产生，所以通常情况下建议绑定。
     * @param owner 声明周期拥有者，通常是Activity或Fragment。
     * @param callBack 异步回调。
     */
    @CallSuper
    fun bind(
        owner: LifecycleOwner,
        callBack: IdActionDataCallback<ID, ActionParameter, D>
    ): IdActionDataProxy<ID, D> {
        mSubscriptions = ExtCompositeSubscription()
        if (mGlobalCallback != null) {
            if (mGlobalCallback != callBack) {
                unbind()
                mGlobalCallback = callBack
            }
        } else {
            mGlobalCallback = callBack
        }
        owner.lifecycle.addObserver(this)
        return this
    }

    /**
     * 显示加载进度弹窗(loading弹窗)。
     * @param context 可以显示Dialog的Context。
     */
    open fun progress(context: Context, text: String): IdActionDataProxy<ID, D> {
        this.context = context
        this.progressText = text
        return this
    }

    /**
     * 显示加载进度弹窗(loading弹窗)。
     * @param context 可以显示Dialog的Context。
     */
    open fun progress(context: Context, @StringRes text: Int = R.string.please_wait): IdActionDataProxy<ID, D> {
        return progress(context, context.resources.getString(text))
    }

    private fun onHideProgress() {
        context?.also { toaster.hideProgress(it) }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    fun destroy() {
        context = null
        unbind()
    }


    fun unbind() {
        this.mGlobalCallback = null
        if (mSubscriptions?.isDisposed() == false) {
            mSubscriptions?.disposed()
        }
    }

    /**
     * 用户行为观察者
     */
    internal inner class IdActionCaseSubscriber(private val id: ID, private val action: ActionParameter) : ErrorHandlerSubscriber<D>(toaster) {

        override fun onFinished(successful: Boolean) {
            super.onFinished(successful)
            onHideProgress()
            isWorking = false
            if (mGlobalCallback != null) {
                mGlobalCallback!!.onFinished(id, action)
            } else {
                //页面如果已经销毁了，回调丢失是正常的，否则就是不正常。
                Logger.system("===>DataProxy")?.e("The Proxy callback is Null!")
            }
        }

        override fun onError(ex: ApiException) {
            try {
                if (mGlobalCallback != null) {
                    mGlobalCallback?.onFailed(id, action, ex)
                } else {
                    //页面如果已经销毁了，回调丢失是正常的，否则就是不正常。
                    Logger.system("===>DataProxy")?.e("The Proxy callback is Null!")
                }
            } catch (e: Exception) {
                onError(e)
            }
        }

        override fun onSuccess(t: D) {
            try {
                if (mGlobalCallback != null) {
                    mGlobalCallback!!.onSuccess(id, action, t)
                } else {
                    //页面如果已经销毁了，回调丢失是正常的，否则就是不正常。
                    Logger.system("===>DataProxy")?.e("The Proxy callback is Null!")
                }
            } catch (e: Exception) {
                onError(e)
            }
        }
    }

    interface IdActionDataCallback<ID, ACTION : ActionParameter, D> {

        fun onSuccess(id: ID, action: ACTION, data: D)

        fun onFailed(id: ID, action: ACTION, e: ApiException)

        fun onFinished(id: ID, action: ACTION) {}
    }

    interface DefaultIdDataCallback<ID, ACTION : ActionParameter, D> : IdActionDataCallback<ID, ACTION, D> {

        fun onLoadSuccess(id: ID, data: D)

        fun onLoadFailed(id: ID, e: ApiException)

        fun onRetrySuccess(id: ID, data: D)

        fun onRetryFailed(id: ID, e: ApiException)

        fun onRefreshSuccess(id: ID, data: D)

        fun onRefreshFailed(id: ID, e: ApiException)
    }

    interface PageIdDataCallback<ID, ACTION : ActionParameter, D> : DefaultIdDataCallback<ID, ACTION, D> {

        fun onLoadMoreFailed(id: ID, e: ApiException)

        fun onLoadMoreSuccess(id: ID, data: D)
    }
}