package com.kelin.proxyfactory

import android.app.Application
import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.kelin.logger.LogOption
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

    private var mContext: Application? = null

    internal var isDebugMode = false
        private set

    private var mProxyHandler: ProxyEventHandler? = null

    private val requireProxyHandler: ProxyEventHandler
        get() = mProxyHandler ?: throw NullPointerException("You must call the ProxyFactory.init() Method before use the ProxyFactory")

    /**
     * 初始化ProxyFactory。
     * @param context 应用上下文。
     * @param proxyHandler 事件处理器。
     * @param isDebug 是否开启Debug模式。
     * @param vpnCheck 是否开启VPN校验。
     */
    fun init(context: Application, proxyHandler: ProxyEventHandler, isDebug: Boolean = false, vpnCheck: Boolean = true) {
        LogOption.init("ProxyFactory", isDebug)
        mContext = context
        mProxyHandler = proxyHandler
        isDebugMode = isDebug
        NetWorks.init(context, vpnCheck)
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
        return object : IdActionDataProxy<ID, DATA>(requireProxyHandler) {
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
        return object : ActionDataProxy<DATA>(requireProxyHandler) {
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
        return object : IdDataProxy<ID, DATA>(requireProxyHandler) {
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
        return object : DataProxy<DATA>(requireProxyHandler) {
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
        return object : IdActionDataProxy<ID, DATA>(requireProxyHandler) {
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
        return object : ActionDataProxy<DATA>(requireProxyHandler) {
            override fun createUseCase(action: ActionParameter): UseCase<DATA> {
                //这里改用子类集成ActionDataProxy
                return if (action is PageActionParameter && action.pages != null) {
                    PageApiRequestUseCase(action.pages, caller)
                } else throw RuntimeException("Action Parameter Error!")
            }
        }.tryBindLifecycle(owner)
    }
}