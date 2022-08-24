package com.kelin.proxyfactory.exception

import androidx.annotation.StringRes
import com.kelin.apiexception.ApiException
import com.kelin.proxyfactory.ProxyFactory
import com.kelin.proxyfactory.R

/**
 * **描述:** ProxyFactroy中定义的业务逻辑错误。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2022/8/24 4:57 PM
 *
 * **版本:** v 1.0.0
 */
enum class ProxyLogicError(override val code: Int, override val msg: String) : ApiException.ApiError {
    FAIL_TOO_MUCH(9999, getString(R.string.failed_too_much)),
    NETWORK_UNAVAILABLE(8001, getString(R.string.network_unavailable));

}

private fun getString(@StringRes text: Int): String {
    return ProxyFactory.getContext().getString(text)
}