package com.kelin.proxyfactory

import java.util.concurrent.atomic.AtomicInteger

/**
 * **描述:** 对错误的封装。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019/4/2  2:03 PM
 *
 * **版本:** v 1.0.0
 */
class ErrorWrapper {
    var count = AtomicInteger(0)
    var lastTime: Long = 0

    companion object {
        const val MAX_ERROR_COUNT = 8
        const val MAX_ERROR_PERIOD = 120000
    }
}