package com.kelin.proxyfactory

import io.reactivex.disposables.Disposable
import io.reactivex.exceptions.CompositeException

import java.util.*

/**
 * **描述:**
 *
 *
 * **创建人:** kelin
 *
 *
 * **创建时间:** 2019/4/1  3:10 PM
 *
 *
 * **版本:** v 1.0.0
 */
internal class ExtCompositeSubscription {

    private var subscriptions: MutableSet<Disposable>? = null
    @Volatile
    private var unSubscribed: Boolean = false

    constructor() {}

    constructor(vararg subscriptions: Disposable) {
        this.subscriptions = HashSet(subscriptions.toList())
    }

    fun isDisposed(): Boolean {
        return unSubscribed
    }

    /**
     * Adds a new [Subscription] to this `CompositeSubscription` if the
     * `CompositeSubscription` is not yet unSubscribed. If the `CompositeSubscription` *is*
     * unSubscribed, `add` will indicate this by explicitly unsubscribing the new `Subscription` as
     * well.
     *
     * @param s the [Subscription] to add
     */
    fun add(s: Disposable) {
        if (s.isDisposed) {
            return
        }
        if (!unSubscribed) {
            synchronized(this) {
                if (!unSubscribed) {
                    if (subscriptions == null) {
                        subscriptions = HashSet(4)
                    }
                    subscriptions!!.add(s)
                    return
                }
            }
        }
        // call after leaving the synchronized block so we're not holding a lock while executing this
//        s.dispose()
    }

    /**
     * Removes a [Subscription] from this `CompositeSubscription`, and unsubscribes the
     * [Subscription].
     *
     * @param s the [Subscription] to remove
     */
    fun remove(s: Disposable) {
        if (!unSubscribed) {
            var unsubscribe: Boolean
            synchronized(this) {
                if (unSubscribed || subscriptions == null) {
                    return
                }
                unsubscribe = subscriptions!!.remove(s)
            }
            if (unsubscribe) {
                // if we removed successfully we then need to call unSubscribe on it (outside of the lock)
                s.dispose()
            }
        }
    }

    /**
     * Unsubscribes any subscriptions that are currently part of this `CompositeSubscription` and remove
     * them from the `CompositeSubscription` so that the `CompositeSubscription` is empty and
     * able to manage new subscriptions.
     */
    fun clear() {
        if (!unSubscribed) {
            var unsubscribe: Collection<Disposable>?
            synchronized(this) {
                if (unSubscribed || subscriptions == null) {
                    return
                } else {
                    unsubscribe = subscriptions
                    subscriptions = null
                }
            }
            disposeAll(unsubscribe)
        }
    }


    fun disposed() {
        if (!unSubscribed) {
            var unsubscribe: Collection<Disposable>?
            synchronized(this) {
                if (unSubscribed) {
                    return
                }
                unSubscribed = true
                unsubscribe = subscriptions
                subscriptions = null
            }
            // we will only get here once
            disposeAll(unsubscribe)
        }
    }

    private fun disposeAll(subscriptions: Collection<Disposable>?) {
        if (subscriptions == null) {
            return
        }
        var es: MutableList<Throwable>? = null
        for (s in subscriptions) {
            try {
                s.dispose()
            } catch (e: Throwable) {
                if (es == null) {
                    es = ArrayList()
                }
                es.add(e)
            }

        }
        if (!es.isNullOrEmpty()) {
            throw CompositeException(es)
        }
    }

    private fun unsubscribe(subscription: Disposable?) {
        if (subscription == null) {
            return
        }
        var es: MutableList<Throwable>? = null
        try {
            subscription.dispose()
        } catch (e: Throwable) {
            if (es == null) {
                es = ArrayList()
            }
            es.add(e)
        }

        if (!es.isNullOrEmpty()) {
            throw CompositeException(es)
        }
    }

    /**
     * Returns true if this composite is not unSubscribed and contains subscriptions.
     *
     * @return `true` if this composite is not unSubscribed and contains subscriptions.
     * @since 1.0.7
     */
    fun hasSubscriptions(): Boolean {
        if (!unSubscribed) {
            synchronized(this) {
                return !unSubscribed && subscriptions != null && !subscriptions!!.isEmpty()
            }
        }
        return false
    }
}
