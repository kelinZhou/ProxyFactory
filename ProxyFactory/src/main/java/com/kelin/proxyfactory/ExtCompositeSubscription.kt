package com.kelin.proxyfactory

import com.kelin.proxyfactory.subscriber.Achievable
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
internal class ExtCompositeSubscription(vararg subscriptions: Achievable) {

    private var subscriptions: MutableSet<Achievable> = HashSet()

    @Volatile
    private var unSubscribed: Boolean = false

    init {
        subscriptions.takeIf { it.isNotEmpty() }?.also { this.subscriptions.addAll(it) }
    }

    fun isDisposed(): Boolean {
        return unSubscribed
    }

    /**
     * Adds a new [Achievable] to this `CompositeSubscription` if the
     * `CompositeSubscription` is not yet unSubscribed. If the `CompositeSubscription` *is*
     * unSubscribed, `add` will indicate this by explicitly unsubscribing the new `Subscription` as
     * well.
     *
     * @param s the [Achievable] to add
     */
    fun add(s: Achievable) {
        if (s.isAchieved) {
            return
        }
        if (!unSubscribed) {
            synchronized(this) {
                if (!unSubscribed) {
                    subscriptions.add(s)
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
    fun remove(s: Achievable) {
        if (!unSubscribed) {
            var unsubscribe: Boolean
            synchronized(this) {
                if (unSubscribed || subscriptions.isEmpty()) {
                    return
                }
                unsubscribe = subscriptions.remove(s.also {
                    it.achieved()
                })
            }
            if (unsubscribe) {
                // if we removed successfully we then need to call unSubscribe on it (outside of the lock)
                s.achieved()
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
            var unsubscribe: Collection<Achievable>?
            synchronized(this) {
                if (unSubscribed || subscriptions.isEmpty()) {
                    return
                } else {
                    unsubscribe = subscriptions
                    subscriptions.clear()
                }
            }
            disposeAll(unsubscribe)
        }
    }


    fun disposed() {
        if (!unSubscribed) {
            var unsubscribe: Collection<Achievable>?
            synchronized(this) {
                if (unSubscribed) {
                    return
                }
                unSubscribed = true
                unsubscribe = subscriptions
                subscriptions.clear()
            }
            // we will only get here once
            disposeAll(unsubscribe)
        }
    }

    private fun disposeAll(subscriptions: Collection<Achievable>?) {
        if (subscriptions == null) {
            return
        }
        var es: MutableList<Throwable>? = null
        for (s in subscriptions) {
            try {
                s.achieved()
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

    /**
     * Returns true if this composite is not unSubscribed and contains subscriptions.
     *
     * @return `true` if this composite is not unSubscribed and contains subscriptions.
     * @since 1.0.7
     */
    fun hasSubscriptions(): Boolean {
        if (!unSubscribed) {
            synchronized(this) {
                return !unSubscribed && subscriptions.isNotEmpty()
            }
        }
        return false
    }
}
