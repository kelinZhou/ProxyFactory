package com.kelin.proxyfactory.usecase

import io.reactivex.Observable


/**
 * **描述:** 没有请求参数的UseCase。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2020/3/9  9:44 AM
 *
 * **版本:** v 1.0.0
 */
internal class ApiRequestUseCase<DATA>(private val caller: () -> Observable<DATA>) : UseCase<DATA>() {
    override fun buildUseCaseObservable(): Observable<DATA> {
        return caller()
    }
}
