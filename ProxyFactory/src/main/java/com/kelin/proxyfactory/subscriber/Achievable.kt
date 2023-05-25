package com.kelin.proxyfactory.subscriber

/**
 * **描述:** 可以被完成的。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2023/5/25 4:02 PM
 *
 * **版本:** v 1.0.0
 */
interface Achievable {

    val isAchieved: Boolean

    /**
     * 当其失去作用被完成时调用。
     */
    fun achieved()
}