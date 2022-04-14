package com.interview.announcer

import org.junit.Test
import org.mockito.kotlin.*
import kotlin.random.Random

private typealias Observer<T> = (T) -> Unit

class AnnouncerTest {

    private val subject: Announcer<String> = Announcers.create()

    @Test
    fun `subscription adds and is invoking successfully`() {
        val subscription: Observer<String> = mock {
            on { invoke(any()) } doReturn println("test")
        }
        subject.subscribe(0, subscription)

        subject.notify("test")

        verify(subscription, only()).invoke(any())
    }

    @Test
    fun `filter is working correctly`() {
        val subscriptions = Array(5) { createObserver() }

        subscriptions.forEachIndexed { index, observer -> subject.subscribe(index, observer) }

        val priorityRule: (Int) -> Boolean = { it % 2 == 0 }

        subject.notifyFiltered("test", priorityRule)

        subscriptions.forEachIndexed { index, observer ->
            val resolution = if (priorityRule(index)) only() else never()
            verify(observer, resolution).invoke(any())
        }
    }

    @Test
    fun `for all subscriptions invocation happens on notify`() {
        val subscriptions = Array(5) {
            mock<Observer<String>> {
                on { invoke(any()) } doReturn println("test")
            }
        }

        subscriptions.forEachIndexed { index, observer -> subject.subscribe(index, observer) }

        subject.notify("test")

        subscriptions.forEach { verify(it, only()).invoke(any()) }
    }

    @Test
    fun `subscriptions are invoked depending on priority`() {
        val subscriptions = Array(5) {
            createObserver() to Random.nextInt(100)
        }

        subscriptions.forEach { (observer, priority) ->
            subject.subscribe(priority, observer)
        }

        subject.notify("test")

        val sortedByPriority = subscriptions
                .sortedBy { (_, priority) -> priority }
                .map { (observer, _) -> observer }
                .toTypedArray()

        val inOrder = inOrder(*sortedByPriority)

        sortedByPriority.forEach { inOrder.verify(it).invoke(any()) }
    }

    @Test
    fun `subscriptions are processing correct value`() {
        val listSize = 5
        val testMessage = "Test message, hello!"
        val resultList = mutableListOf<String>()

        val subscriptions = Array<Observer<String>>(listSize) { { message: String -> resultList.add(message) } }

        subscriptions.forEachIndexed { index, observer -> subject.subscribe(index, observer) }

        subject.notify(testMessage)

        assert(resultList.size == listSize)
        assert(resultList.all { it == testMessage })
    }

    @Test
    fun `items with same priority are processed in order`() {
        val subscriptions = Array(5) { createObserver() }

        subscriptions.forEach { observer -> subject.subscribe(0, observer) }

        subject.notify("test")

        subscriptions.forEach { verify(it, only()).invoke(any()) }
    }

    @Test
    fun `removing works correctly`() {
        val subscriptions = Array(5) { createObserver() }

        val closeables = subscriptions.mapIndexed { index, observer -> subject.subscribe(index, observer) }

        val removedIndex = 3
        closeables[removedIndex].close()
        subject.notify("test")

        subscriptions.forEachIndexed { index, observer ->
            val resolution = if (index == removedIndex) never() else only()
            verify(observer, resolution).invoke(any())
        }
    }

    private fun createObserver() = mock<Observer<String>> {
        on { invoke(any()) } doReturn println("test")
    }
}
