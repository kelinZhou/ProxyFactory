package com.kelin.proxyfactory.usecase

import com.kelin.proxyfactory.PageActionParameter
import io.reactivex.Observable

/**
 * **描述:** 有分页功能的UseCase。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2020/3/9  9:44 AM
 *
 * **版本:** v 1.0.0
 */
class PageApiRequestUseCase<DATA>(private val pages: PageActionParameter.Pages, private val caller: (pages: PageActionParameter.Pages) -> Observable<DATA>) : UseCase<DATA>() {
    override fun buildUseCaseObservable(): Observable<DATA> {
        return caller(pages)
    }
}