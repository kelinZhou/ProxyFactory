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
open class ActionParameter internal constructor(pAction: LoadAction) {

    var action: LoadAction
        private set

    init {
        action = pAction
    }

    fun updateAction(action: LoadAction): ActionParameter {
        this.action = action
        return this
    }

    override fun toString(): String {
        return "LoadAction{ action=$action }"
    }

    companion object {
        /**
         * 获取加载实例。
         */
        fun createInstance(): ActionParameter {
            return ActionParameter(LoadAction.LOAD)
        }
    }
}