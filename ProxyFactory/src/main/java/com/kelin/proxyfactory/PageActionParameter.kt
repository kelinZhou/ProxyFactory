package com.kelin.proxyfactory


/**
 * **描述:** 带有分页请求时的动作参数。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2020/3/9  9:44 PM
 *
 * **版本:** v 1.0.0
 */
class PageActionParameter private constructor(action: LoadAction, val pages: Pages?) : ActionParameter(action) {

    fun updateAction(action: LoadAction, pageNumber: Int, pageSize: Int = DEFAULT_PAGE_SIZE): ActionParameter {
        if (pages != null) {
            pages.page = pageNumber
            pages.size = pageSize
        }
        return super.updateAction(action)
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is PageActionParameter && pages == other.pages
    }

    fun resetPages() {
        pages?.apply {
            page = FIRST_PAGE_NUMBER
            size = DEFAULT_PAGE_SIZE
        }
    }

    override fun hashCode(): Int {
        return pages?.hashCode() ?: action.hashCode()
    }

    companion object {
        const val DEFAULT_PAGE_SIZE = 20
        const val FIRST_PAGE_NUMBER = 1

        /**
         * 获取加载实例。
         */
        fun createInstance(isEnablePage: Boolean, pageSize: Int = DEFAULT_PAGE_SIZE): PageActionParameter {
            return PageActionParameter(LoadAction.LOAD, if (isEnablePage) Pages(FIRST_PAGE_NUMBER, pageSize) else null)
        }
    }

    data class Pages(var page: Int, var size: Int) {
        override fun equals(other: Any?): Boolean {
            return other != null && other is Pages && page == other.page && size == other.size
        }

        override fun hashCode(): Int {
            var result = page
            result = 31 * result + size
            return result
        }
    }
}