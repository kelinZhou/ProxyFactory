package com.kelin.proxyfactory

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.kelin.apiexception.ApiException
import com.kelin.logger.LogOption
import com.kelin.logger.Logger
import com.kelin.proxyfactory.usecase.*
import io.reactivex.Observable
import java.lang.RuntimeException

/**
 * **描述:** Proxy的工厂类。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2020/3/9  9:44 PM
 *
 * **版本:** v 1.0.0
 */
object ProxyFactory {

    private var mContext: Context? = null

    internal var isDebugMode = false
        private set

    private var mToaster: Toaster? = null

    private val requireToaster: Toaster
        get() = mToaster ?: throw NullPointerException("You must call the ProxyFactory.init() Method before use the ProxyFactory")

    fun init(context: Context, toaster: Toaster, isDebug: Boolean = false) {
        LogOption.init("ProxyFactory", isDebug)
        mContext = context.applicationContext
        mToaster = toaster
        isDebugMode = isDebug
    }

    fun recycle() {
        IdActionDataProxy.clearUseCase()
    }

    internal fun getContext(): Context {
        return mContext ?: throw NullPointerException("You must call the MapKit.init() Method before use the MapKit")
    }

    private fun <P : LifecycleProxy> P.tryBindLifecycle(owner: LifecycleOwner?): P {
        owner?.also { bind(owner) }
        return this
    }

    /**
     * 创建一个请求代理（不支持分页）。
     *
     * @param caller api调用器，具体要调用那个api由调用器决定。
     */
    fun <ID, DATA> createIdActionProxy(caller: (id: ID) -> Observable<DATA>): IdActionDataProxy<ID, DATA> {
        return object : IdActionDataProxy<ID, DATA>(requireToaster) {
            override fun createUseCase(id: ID, action: ActionParameter): UseCase<DATA> {
                return ApiIdRequestUseCase(id, caller)
            }
        }
    }

    /**
     * 创建一个请求代理（不支持分页）。
     *
     * @param caller api调用器，具体要调用那个api由调用器决定。
     */
    fun <DATA> createActionProxy(owner: LifecycleOwner? = null, caller: () -> Observable<DATA>): ActionDataProxy<DATA> {
        return object : ActionDataProxy<DATA>(requireToaster) {
            override fun createUseCase(action: ActionParameter): UseCase<DATA> {
                return ApiRequestUseCase(caller)
            }
        }.tryBindLifecycle(owner)
    }

    /**
     * 创建一个请求代理（不支持分页）。
     *
     * @param caller api调用器，具体要调用那个api由调用器决定。
     */
    fun <ID, DATA> createIdProxy(owner: LifecycleOwner? = null, caller: (id: ID) -> Observable<DATA>): IdDataProxy<ID, DATA> {
        return object : IdDataProxy<ID, DATA>(requireToaster) {
            override fun createUseCase(id: ID): UseCase<DATA> {
                return ApiIdRequestUseCase(id, caller)
            }
        }.tryBindLifecycle(owner)
    }

    /**
     * 创建一个请求代理（不支持分页）。
     *
     * @param caller api调用器，具体要调用那个api由调用器决定。
     */
    fun <DATA> createProxy(owner: LifecycleOwner? = null, caller: () -> Observable<DATA>): DataProxy<DATA> {
        return object : DataProxy<DATA>(requireToaster) {
            override fun createUseCase(): UseCase<DATA> {
                return ApiRequestUseCase(caller)
            }
        }.tryBindLifecycle(owner)
    }

    /**
     * 创建一个请求代理（支持分页）。
     *
     * @param caller api调用器，具体要调用那个api由调用器决定。
     */
    fun <ID, DATA> createPageIdActionProxy(caller: (id: ID, pages: PageActionParameter.Pages) -> Observable<DATA>): IdActionDataProxy<ID, DATA> {
        return object : IdActionDataProxy<ID, DATA>(requireToaster) {
            override fun createUseCase(id: ID, action: ActionParameter): UseCase<DATA> {
                //这里改用子类集成IdActionDataProxy
                return if (action is PageActionParameter && action.pages != null) {
                    PageApiIdRequestUseCase(id, action.pages, caller)
                } else throw RuntimeException("Action Parameter Error!")
            }
        }
    }

    /**
     * 创建一个请求代理（支持分页）。
     *
     * @param caller api调用器，具体要调用那个api由调用器决定。
     */
    fun <DATA> createPageActionProxy(owner: LifecycleOwner? = null, caller: (pages: PageActionParameter.Pages) -> Observable<DATA>): ActionDataProxy<DATA> {
        return object : ActionDataProxy<DATA>(requireToaster) {
            override fun createUseCase(action: ActionParameter): UseCase<DATA> {
                //这里改用子类集成ActionDataProxy
                return if (action is PageActionParameter && action.pages != null) {
                    PageApiRequestUseCase(action.pages, caller)
                } else throw RuntimeException("Action Parameter Error!")
            }
        }.tryBindLifecycle(owner)
    }
}