package com.kelin.proxyfactory

/**
 * **描述:** 定义数据仓库接口的标识。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2020/3/9  9:44 AM
 *
 * **版本:** v 1.0.0
 */
interface Repo {
    enum class Type(val code: Int) {
        AUTH(0x0f0),
        LOGIC(0x0f1),
        COMMON(0x0f2),
        UPDATE(0x0f3);
    }
}
