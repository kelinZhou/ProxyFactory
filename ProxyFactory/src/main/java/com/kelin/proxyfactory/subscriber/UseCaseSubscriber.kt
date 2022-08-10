package com.kelin.proxyfactory.subscriber

import io.reactivex.observers.DisposableObserver

/**
 * **描述:** 默认的观察者。
 *
 * **创建人:** kelin
 *
 * **创建时间:** 2019/3/9  11:01 AM
 *
 * **版本:** v 1.0.0
 */
abstract class UseCaseSubscriber<T> : DisposableObserver<T>() {

    override fun onComplete() {
        // no-op by default.
    }

    override fun onError(e: Throwable) {
        // no-op by default.
    }

    override fun onNext(t: T) {
        // no-op by default.
    }
}
