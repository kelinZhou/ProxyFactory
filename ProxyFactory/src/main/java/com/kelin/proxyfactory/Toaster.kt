package com.kelin.proxyfactory

import android.content.Context
import com.kelin.apiexception.ApiException

/**
 * **描述:** Toast提示器。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2022/8/10 1:14 PM
 *
 * **版本:** v 1.0.0
 */
interface Toaster {

    /**
     * 处理异步任务中捕获的异常，如果你希望自己处理改异常则需要返回null，返回null之后Proxy的onFailed方法将不会被回调，否则会将你返回的ApiException回调给Proxy的onFailed方法。
     */
    fun handError(e: Throwable): ApiException?

    /**
     * 显示调用异步任务失败时的提示。
     */
    fun showFailedToast(e: ApiException)

    /**
     * 显示加载中的样式。
     */
    fun showProgress(context: Context, progressTip: String? = null)

    /**
     * 隐藏加载中的样式。
     */
    fun hideProgress(context: Context)
}