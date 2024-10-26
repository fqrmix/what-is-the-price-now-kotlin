package org.example.common

import io.github.oshai.kotlinlogging.KotlinLogging
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Планировщик задач, позволяющий создавать и запускать отложенные задачи.
 */
object Scheduler {

    /**
     * Пул потоков, использующийся для выполнения задач по расписанию.
     */
    private var scheduler = Executors.newScheduledThreadPool(10)

    /**
     * Логгер для регистрации информации о задачах и ошибках.
     */
    private val logger = KotlinLogging.logger {}

    /**
     * Создает отложенную задачу, которая будет выполнена в указанное время [executionTime].
     *
     * @param task Задача [RunnableTask], которая должна быть выполнена.
     * @param executionTime Время выполнения задачи в формате [LocalDateTime].
     * @return [ScheduledFuture] представляющее запланированную задачу, если она успешно создана, или `null` в случае ошибки.
     */
    fun createScheduledTask(task: RunnableTask, executionTime: LocalDateTime) : ScheduledFuture<*>? {
        val currentTime = LocalDateTime.now()

        try {
            val scheduledTask = scheduler.schedule(
                task,
                Duration.between(currentTime, executionTime).toMillis(),
                TimeUnit.MILLISECONDS
            )

            logger.info { "Got a new task: $scheduledTask. Execution Time: $executionTime. Context: ${task.getContext()}" }
            task.setStatus(RunnableTask.TaskStatus.WAITING_FOR_EXECUTION)
            return scheduledTask
        } catch (e: Exception) {
            logger.atWarn {
                message = "Failed to create scheduled task"
                cause = e
            }
            return null
        }
    }

    /**
     * Отправляет задачу [task] на немедленное выполнение.
     *
     * @param task Задача [RunnableTask], которая должна быть выполнена.
     */
    fun submitTask(task: RunnableTask) {
        this.scheduler.submit(task)
    }
}

