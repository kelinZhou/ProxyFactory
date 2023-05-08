package com.kelin.proxyfactory

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import com.kelin.apiexception.ApiException
import com.kelin.proxyfactory.usecase.UseCase

/**
 * **描述:** 有请求参数的网络请求代理。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019/4/1  1:42 PM
 *
 * **版本:** v 1.0.0
 */
abstract class ActionDataProxy<D>(toaster: Toaster) : IdActionDataProxy<Any, D>(toaster), LifecycleProxy {

    private val defaultRequestId = Any()

    override fun setNotToast(): ActionDataProxy<D> {
        super.setNotToast()
        return this
    }

    final override fun createUseCase(id: Any, action: ActionParameter): UseCase<D> {
        return createUseCase(action)
    }

    abstract fun createUseCase(action: ActionParameter): UseCase<D>

    final override fun checkNetworkEnable(id: Any, action: ActionParameter): Boolean {
        return checkNetworkEnable(action)
    }

    protected open fun checkNetworkEnable(action: ActionParameter): Boolean = true

    fun request(action: LoadAction) {
        request(ActionParameter.createInstance(action))
    }

    fun request(action: ActionParameter) {
        super.request(action, defaultRequestId)
    }

    /**
     * 将Proxy于声明周期绑定，由于绑定后将会减少垃圾的产生，所以通常情况下建议绑定。
     * @param owner 声明周期拥有者，通常是Activity或Fragment。
     * @param callBack 异步回调。
     */
    fun bind(owner: LifecycleOwner, callBack: ActionDataCallback<ActionParameter, D>): ActionDataProxy<D> {
        super.bind(owner, callBack)
        return this
    }

    /**
     * 将Proxy于声明周期绑定，由于绑定后将会减少垃圾的产生，所以通常情况下建议绑定。
     * @param owner 声明周期拥有者，通常是Activity或Fragment。
     */
    override fun bind(owner: LifecycleOwner): ActionDataProxy<D> {
        bind(owner, InnerCallback())
        return this
    }

    override fun progress(context: Context, text: String): ActionDataProxy<D> {
        super.progress(context, text)
        return this
    }

    override fun progress(context: Context, text: Int): ActionDataProxy<D> {
        super.progress(context, text)
        return this
    }

    /**
     * 设置成功回调。
     * @param onSuccess 回调函数，当异步任务成功后将会调用该回调函数。
     */
    fun onSuccess(onSuccess: (data: D, action: ActionParameter) -> Unit): ActionDataProxy<D> {
        if (mGlobalCallback != null && mGlobalCallback is InnerCallback) {
            (mGlobalCallback as InnerCallback).success = onSuccess
        } else {
            mGlobalCallback = SingleCallback().apply { success = onSuccess }
        }
        return this
    }

    /**
     * 设置失败回调。
     * @param onFailed 回调函数，当异步任务失败后将会调用该回调函数。
     */
    fun onFailed(onFailed: (e: ApiException, action: ActionParameter) -> Unit): ActionDataProxy<D> {
        if (mGlobalCallback != null && mGlobalCallback is InnerCallback) {
            (mGlobalCallback as InnerCallback).failed = onFailed
        } else {
            mGlobalCallback = SingleCallback().apply { failed = onFailed }
        }
        return this
    }

    private open inner class InnerCallback : IdActionDataCallback<Any, ActionParameter, D> {

        var success: ((data: D, action: ActionParameter) -> Unit)? = null

        var failed: ((e: ApiException, action: ActionParameter) -> Unit)? = null

        override fun onSuccess(id: Any, action: ActionParameter, data: D) {
            success?.invoke(data, action)
        }

        override fun onFailed(id: Any, action: ActionParameter, e: ApiException) {
            if (failed != null) {
                failed?.invoke(e, action)
            } else if (!noToast) {
                toaster.showFailedToast(e)
            }
        }
    }

    private inner class SingleCallback : InnerCallback() {
        override fun onFinished(id: Any, action: ActionParameter) {
            unbind()
        }
    }

    abstract class ActionDataCallback<ACTION : ActionParameter, D> : IdActionDataCallback<Any, ACTION, D> {
        final override fun onSuccess(id: Any, action: ACTION, data: D) {
            onSuccess(action, data)
        }

        abstract fun onSuccess(action: ACTION, data: D)

        final override fun onFailed(id: Any, action: ACTION, e: ApiException) {
            onFailed(action, e)
        }

        abstract fun onFailed(action: ACTION, e: ApiException)
    }
}