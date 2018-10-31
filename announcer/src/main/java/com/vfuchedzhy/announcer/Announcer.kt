package com.vfuchedzhy.announcer

interface Announcer<T> {

    fun notifyAll(value: T)
    fun notifyFiltered(value: T, priorityRule: PriorityRule)

    fun addSubscription(priority: AnnouncerPriority, subscription: Observer<T>)
    fun removeSubsription(subscription: Observer<T>)
    fun clear()
}

typealias Observer<T> = (T) -> Unit

typealias PriorityRule = (AnnouncerPriority) -> Boolean

typealias AnnouncerPriority = Int
