package com.kelin.proxyfactory

/**
 * **描述:** 请求数据的动作参数。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019/4/3  1:51 PM
 *
 * **版本:** v 1.0.0
 */
open class ActionParameter internal constructor(private var pAction: LoadAction) {

    val action: LoadAction
        get() = pAction


    fun updateAction(action: LoadAction): ActionParameter {
        this.pAction = action
        return this
    }

    override fun toString(): String {
        return "LoadAction{ action=$action }"
    }

    companion object {
        /**
         * 获取加载实例。
         */
        fun createInstance(action: LoadAction = LoadAction.LOAD): ActionParameter {
            return ActionParameter(action)
        }
    }
}