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

    fun handError(e: Throwable): Boolean

    /**
     * 显示调用接口失败时的提示。
     */
    fun showFailedToast(e: ApiException)

    /**
     * 显示加载中的样式。
     */
    fun showProgress(context: Context, progressTip: String?)

    fun hideProgress(context: Context)
}