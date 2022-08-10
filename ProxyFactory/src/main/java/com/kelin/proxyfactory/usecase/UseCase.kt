package com.kelin.proxyfactory.usecase

import com.kelin.proxyfactory.executors.ProxyExecutors
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.schedulers.Schedulers

/**
 * **描述:** 处理数据获取的场景。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2020/3/9  9:44 AM
 *
 * **版本:** v 1.0.0
 */
abstract class UseCase<DATA> {

    private val subscription: Observable<DATA>
        get() = this.buildUseCaseObservable()


    protected abstract fun buildUseCaseObservable(): Observable<DATA>

    fun execute(observer: Observer<DATA>) {
        subscription.subscribeOn(Schedulers.from(ProxyExecutors.jobExecutor))
            .observeOn(ProxyExecutors.uiThread.scheduler)
            .subscribe(observer)
    }
}
