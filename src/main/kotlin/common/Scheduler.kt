package org.example.common

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

object Scheduler {

    private lateinit var scheduler : ScheduledExecutorService
    private val logger = KotlinLogging.logger {}

    fun init() {
        scheduler = Executors.newScheduledThreadPool(1)
    }

    fun createScheduledTask(task: RunnableTask, executionTime: LocalDateTime) : ScheduledFuture<*>? {
        if (!Scheduler::scheduler.isInitialized) {
            throw RuntimeException("MessageScheduler object is not initialized. " +
                    "Call init() function once before interaction with scheduler")
        }

        val currentTime = LocalDateTime.now()

        if (currentTime > executionTime) {
            throw RuntimeException("$currentTime is more than $executionTime. Skipping task")
        }

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
}

