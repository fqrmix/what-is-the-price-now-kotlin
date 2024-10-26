package org.example.common

import io.github.oshai.kotlinlogging.KotlinLogging
import kotlin.properties.Delegates


/**
 * Представляет задачу, которую можно выполнить в виде `Runnable`.
 *
 * @property task Задача, которая будет выполнена при запуске метода [run].
 */
class RunnableTask(val task: () -> Unit) : Runnable {

    /**
     * Имя класса задачи для использования в логировании и контексте задачи.
     */
    private val taskName = task.javaClass

    /**
     * Статус задачи. При изменении статуса вызывает [onChange] для логирования изменений.
     */
    private var status: Int
            by Delegates.observable(1) { _, oldValue, newValue -> onChange(oldValue, newValue) }

    /**
     * Логгер для вывода информации о выполнении задачи и изменении её статуса.
     */
    private val logger = KotlinLogging.logger {}

    /**
     * Логирует изменения статуса задачи.
     *
     * @param oldValue Предыдущее значение статуса.
     * @param newValue Новое значение статуса.
     */
    private fun onChange(oldValue: Int, newValue: Int) {
        logger.info { "Status changed from $oldValue to $newValue. Current status: ${getStatus()}" }
    }

    /**
     * Выполняет задачу [task]. При успешном выполнении изменяет статус на [TaskStatus.DONE].
     * Если во время выполнения возникает исключение, логирует его и устанавливает статус [TaskStatus.CANCELED].
     */
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

    /**
     * Устанавливает статус задачи в зависимости от переданного значения [taskStatus].
     *
     * @param taskStatus Новый статус задачи в виде [TaskStatus].
     */
    fun setStatus(taskStatus: TaskStatus) {
        when (taskStatus) {
            TaskStatus.CREATED -> this.status = 1
            TaskStatus.WAITING_FOR_EXECUTION -> this.status = 2
            TaskStatus.DONE -> this.status = 3
            TaskStatus.CANCELED -> this.status = 4
        }
    }

    /**
     * Возвращает текущий статус задачи в виде [TaskStatus].
     *
     * @return Текущий статус задачи.
     * @throws RuntimeException если статус задачи имеет недопустимое значение.
     */
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

    /**
     * Возвращает имя класса задачи, что может быть полезно для контекстного логирования.
     *
     * @return Строковое представление имени класса задачи.
     */
    fun getContext(): String {
        return taskName.toString()
    }

    /**
     * Перечисление возможных статусов задачи.
     */
    enum class TaskStatus {
        /** Задача создана и ожидает выполнения. */
        CREATED,

        /** Задача ожидает выполнения. */
        WAITING_FOR_EXECUTION,

        /** Задача успешно выполнена. */
        DONE,

        /** Задача отменена. */
        CANCELED
    }
}
