package com.kelin.proxyfactory.usecase

import com.kelin.proxyfactory.IdOwner
import io.reactivex.Observable


/**
 * **描述:** 通用的UseCase。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2020/3/9  9:44 AM
 *
 * **版本:** v 1.0.0
 */
internal class ApiIdRequestUseCase<ID, DATA>(override var id: ID, private val caller: (id: ID) -> Observable<DATA>) : UseCase<DATA>(), IdOwner<ID> {
    override fun buildUseCaseObservable(): Observable<DATA> {
        return caller(id)
    }
}
