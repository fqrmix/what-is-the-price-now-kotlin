package org.example.storage.dao

import org.example.storage.models.Subscription
import org.example.storage.tables.Subscriptions
import org.jetbrains.exposed.dao.LongEntity
import org.jetbrains.exposed.dao.LongEntityClass
import org.jetbrains.exposed.dao.id.EntityID

/**
 * Data Access Object (DAO) для работы с сущностью подписки.
 *
 * @property id Идентификатор подписки.
 */
class SubscriptionDao(id: EntityID<Long>) : LongEntity(id) {
    companion object : LongEntityClass<SubscriptionDao>(Subscriptions)

    var user by UserDao referencedOn Subscriptions.userId
    var article by ArticleDao referencedOn Subscriptions.articleId
    var createdTime by Subscriptions.createdTime
    var nextExecutionTime by Subscriptions.nextExecutionTime

    /**
     * Преобразует текущий объект DAO в объект подписки.
     *
     * @return Экземпляр подписки с данными из DAO.
     */
    fun toSubscription() = Subscription(
        id.value,
        user.id.value,
        article.toArticle(),
        createdTime,
        nextExecutionTime
    )
}