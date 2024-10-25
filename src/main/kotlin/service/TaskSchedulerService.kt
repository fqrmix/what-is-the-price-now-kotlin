package org.example.service

import org.example.command.CheckPriceCommand
import org.example.common.RunnableTask
import org.example.common.Scheduler
import org.example.common.SubscriptionContext
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture

object TaskSchedulerService {

    private val taskScheduler = Scheduler
    private var scheduledTasks = mutableMapOf<Long, ScheduledFuture<*>>()

    fun scheduleNewTask(context: SubscriptionContext, executionTime: LocalDateTime) {
        scheduleInternalTask(
            context.subscription.id,
            RunnableTask { CheckPriceCommand().execute(context) },
            executionTime
        )
    }

    private fun scheduleInternalTask(taskId: Long, task: RunnableTask, executionTime: LocalDateTime) {
        val newTask = taskScheduler.createScheduledTask(task, executionTime)
        if (newTask != null) {
            scheduledTasks[taskId] = newTask
        }
    }

}