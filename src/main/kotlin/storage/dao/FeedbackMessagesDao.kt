package org.example.storage.dao

import org.example.storage.models.FeedbackMessage
import org.example.storage.tables.FeedbackMessages
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID


/**
 * Data Access Object (DAO) для работы с сущностью сообщения с обратной связью.
 *
 * @property id Идентификатор сообщения.
 */
class FeedbackMessagesDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<FeedbackMessagesDao>(FeedbackMessages)

    var user by UserDao referencedOn FeedbackMessages.userId
    var message by FeedbackMessages.message

    /**
     * Преобразует текущий объект DAO в объект сообщения с обратной связью.
     *
     * @return Экземпляр сообщения с обратной связью с данными из DAO.
     */
    fun toFeedbackMessage() = FeedbackMessage(
        id.value,
        user.id.value,
        message
    )
}