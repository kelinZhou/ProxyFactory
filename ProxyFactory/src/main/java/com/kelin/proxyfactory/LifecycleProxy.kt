package com.kelin.proxyfactory

import androidx.lifecycle.LifecycleOwner

/**
 * **描述:** 可以与生命周期绑定的Proxy。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2022/8/24 3:28 PM
 *
 * **版本:** v 1.0.0
 */
interface LifecycleProxy {
    fun bind(owner: LifecycleOwner): LifecycleProxy
}