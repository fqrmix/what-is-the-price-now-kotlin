package org.example.common

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.properties.Delegates

class RunnableTask(val task: () -> Unit) : Runnable {

    private val taskName = task.javaClass
    private var status: Int
            by Delegates.observable(1) {
                    _,
                    oldValue,
                    newValue -> onChange(oldValue, newValue)
            }

    private val logger = KotlinLogging.logger {}

    private fun onChange(oldValue: Int, newValue: Int) {
        logger.info { "Status changed from $oldValue to $newValue. Current status: ${getStatus()}" }
    }

    override fun run() {
        try {
            task()
            status = 3
        } catch (e: Exception) {
            logger.atWarn {
                message = "Task $this was canceled because of exception"
                cause = e
            }
            status = 4
        }
    }

    fun setStatus(taskStatus: TaskStatus) {
        when (taskStatus) {
            TaskStatus.CREATED -> this.status = 1
            TaskStatus.WAITING_FOR_EXECUTION -> this.status = 2
            TaskStatus.DONE -> this.status = 3
            TaskStatus.CANCELED -> this.status = 4
        }
    }

    fun getStatus(): TaskStatus {
        return when (status) {
            1 -> TaskStatus.CREATED
            2 -> TaskStatus.WAITING_FOR_EXECUTION
            3 -> TaskStatus.DONE
            4 -> TaskStatus.CANCELED
            else -> {
                throw RuntimeException("Task status is incorrect. Only 1..4 integers allowed!")
            }
        }
    }

    fun getContext(): String {
        return taskName.toString()
    }

    enum class TaskStatus {
        CREATED,
        WAITING_FOR_EXECUTION,
        DONE,
        CANCELED
    }
}