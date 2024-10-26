package org.example.service

import org.example.command.ComparePriceCommand
import org.example.common.RunnableTask
import org.example.common.Scheduler
import org.example.common.context.SubscriptionContext
import org.example.storage.models.Subscription
import org.example.storage.models.User
import org.example.storage.repository.SubscriptionRepository
import java.time.LocalDateTime

object TaskService {

    private val taskScheduler = Scheduler
    private val subscriptionRepository = SubscriptionRepository()
    private var scheduledTasks = mutableMapOf<Long, RunnableTask>()

    fun scheduleComparePriceTask(subscription: Subscription, user: User) {
        var nextExecutionTime = user.timeToNotify!!.let {
            LocalDateTime.now()
                .withHour(it.hour)
                .withMinute(it.minute)
                .withSecond(0)
        }

        if (nextExecutionTime < LocalDateTime.now()) {
            nextExecutionTime = nextExecutionTime.plusHours(24)
        }

        scheduleInternalTask(
            subscription.id,
            RunnableTask {
                try {
                    ComparePriceCommand().execute(subscription)
                } catch (e: Exception) {
                    println(e.stackTrace)
                }
                subscription.nextExecutionTime = nextExecutionTime
                subscriptionRepository.updateSubscription(subscription)
                this.scheduleComparePriceTask(subscription, user)
            },
            subscription.nextExecutionTime
        )
    }

    fun executeTaskByNow(subscription: Subscription) {
        scheduledTasks[subscription.id]?.let { this.taskScheduler.submitTask(it) }
    }

    private fun scheduleInternalTask(taskId: Long, task: RunnableTask, executionTime: LocalDateTime) {
        val newTask = taskScheduler.createScheduledTask(task, executionTime)
        if (newTask != null) {
            scheduledTasks[taskId] = task
        }
    }
}