package com.kelin.proxyfactory

/**
 * **描述:** 请求参数拥有者。。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2024/4/30 09:49
 *
 * **版本:** v 1.0.0
 */
internal interface IdOwner<ID> {
    var id: ID
}