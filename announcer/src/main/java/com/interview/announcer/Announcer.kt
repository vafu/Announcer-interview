package com.interview.announcer

import java.io.Closeable

/**
 * Subject that allows to add subscriptions with priority.
 */
interface Announcer<T> {

    /**
     * Notifies all subscriptions in natural order by priority.
     */
    fun notify(value: T)

    /**
     * Takes [predicate] that indicates if subscription with given priority should be invoked at this time.
     * Similarly to [notify] subscriptions are invoked in natural order by priority.
     */
    fun notifyFiltered(value: T, predicate: (Int) -> Boolean)

    /**
     * Subscribes to this announcer with [priority] which will be respected upon calling [notify] or [notifyFiltered].
     * @return [Closeable] which cancels this subscription. After calling [Closeable.close] this subscription won't be
     * invoked upon calling [notify].
     */
    fun subscribe(priority: Int, subscription: (T) -> Unit): Closeable
}