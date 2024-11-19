package org.example.storage.repository

import org.example.storage.dao.FeedbackMessagesDao
import org.example.storage.dao.UserDao
import org.example.storage.models.FeedbackMessage
import org.example.storage.tables.FeedbackMessages
import org.jetbrains.exposed.sql.transactions.transaction


/**
 * Репозиторий для работы с сообщениями с обратной связью.
 *
 * Обеспечивает методы для добавления, получения, обновления и удаления сообщений.
 */
class FeedbackMessageRepository {

    /**
     * Добавляет новое сообщение.
     *
     * @param feedbackMessage Объект сообщения, которое нужно добавить.
     * @return Добавленное сообщение.
     */
    fun addFeedbackMessage(feedbackMessage: FeedbackMessage): FeedbackMessage = transaction {
        val userDao = UserDao.findById(feedbackMessage.userId)
            ?: throw IllegalArgumentException("User not found")

        FeedbackMessagesDao.new {
            user = userDao
            message = feedbackMessage.message
        }.toFeedbackMessage()
    }

    /**
     * Получает сообщение с обратной связью по идентификатору.
     *
     * @param id Идентификатор сообщения.
     * @return Сообщение, если найдено, или null, если не найдено.
     */
    fun getFeedbackMessageById(id: Long): FeedbackMessage? = transaction {
        FeedbackMessagesDao.findById(id)?.toFeedbackMessage()
    }

    /**
     * Получает список сообщений с обратной связью по идентификатору пользователя.
     *
     * @param userId Идентификатор пользователя.
     * @return Список сообщений
     */
    fun getFeedbackMessagesByUserId(userId: Long): List<FeedbackMessage> = transaction {
        FeedbackMessagesDao.find {
            FeedbackMessages.userId eq userId
        }.map {
            it.toFeedbackMessage()
        }
    }

    /**
     * Получает все сообщения с обратной связью.
     *
     * @return Список всех сообщений от всех пользователей.
     */
    fun getAllUsers(): List<FeedbackMessage> = transaction {
        FeedbackMessagesDao.all().map { it.toFeedbackMessage() }
    }
}