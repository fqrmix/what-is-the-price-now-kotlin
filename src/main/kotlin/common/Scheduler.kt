package org.example.common

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object Scheduler {

    private var scheduler = Executors.newScheduledThreadPool(10)
    private val logger = KotlinLogging.logger {}

    fun createScheduledTask(task: RunnableTask, executionTime: LocalDateTime) : ScheduledFuture<*>? {

        val currentTime = LocalDateTime.now()

        try {
            val scheduledTask = scheduler.schedule(
                task,
                Duration.between(
                    currentTime,
                    executionTime
                ).toMillis(),
                TimeUnit.MILLISECONDS
            )
            
            logger.info { "Got a new task: $scheduledTask. Execution Time: $executionTime. Context: ${task.getContext()}" }
            task.setStatus(RunnableTask.TaskStatus.WAITING_FOR_EXECUTION)
            return scheduledTask
        } catch (e: Exception){
            logger.atWarn {
                message = "Failed to create scheduled task"
                cause = e
            }
            return null
        }
    }

    fun submitTask(task: RunnableTask) {
        this.scheduler.submit(task)
    }
}

