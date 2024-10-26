package org.example.storage.repository

import org.example.storage.dao.ArticleDao
import org.example.storage.dao.SubscriptionDao
import org.example.storage.dao.UserDao
import org.example.storage.models.Subscription
import org.example.storage.tables.Subscriptions
import org.jetbrains.exposed.sql.transactions.transaction


/**
 * Репозиторий для работы с подписками.
 *
 * Обеспечивает методы для добавления, получения, обновления и удаления подписок.
 */
class SubscriptionRepository {

    /**
     * Добавляет новую подписку.
     *
     * @param subscription Объект подписки, который нужно добавить.
     * @return Добавленная подписка.
     * @throws IllegalArgumentException Если пользователь не найден.
     */
    fun addSubscription(subscription: Subscription): Subscription = transaction {
        val userDao = UserDao.findById(subscription.userId)
            ?: throw IllegalArgumentException("User not found")
        val articleDao = ArticleDao.new {
            price = subscription.article.price
            name = subscription.article.name
            shopName = subscription.article.shopName.toString()
            url = subscription.article.url
        }
        val subscriptionDao = SubscriptionDao.new {
            user = userDao
            article = articleDao
            createdTime = subscription.createdTime
            nextExecutionTime = subscription.nextExecutionTime
        }
        subscriptionDao.toSubscription()
    }

    /**
     * Получает подписку по идентификатору.
     *
     * @param id Идентификатор подписки.
     * @return Подписка, если найдена, или null, если не найдена.
     */
    fun getSubscriptionById(id: Long): Subscription? = transaction {
        SubscriptionDao.findById(id)?.toSubscription()
    }

    /**
     * Получает все подписки для указанного пользователя.
     *
     * @param userId Идентификатор пользователя.
     * @return Список подписок для указанного пользователя.
     */
    fun getSubscriptionsByUserId(userId: Long): List<Subscription> = transaction {
        SubscriptionDao.find {
            Subscriptions.userId eq userId
        }.map {
            it.toSubscription()
        }
    }

    /**
     * Обновляет существующую подписку.
     *
     * @param subscription Объект подписки с обновленными данными.
     * @return true, если обновление прошло успешно, иначе false.
     */
    fun updateSubscription(subscription: Subscription): Boolean = transaction {
        val subscriptionDao = SubscriptionDao.findById(subscription.id) ?: return@transaction false
        subscriptionDao.article.price = subscription.article.price
        subscriptionDao.nextExecutionTime = subscription.nextExecutionTime
        true
    }

    /**
     * Удаляет подписку по идентификатору.
     *
     * @param id Идентификатор подписки, которую нужно удалить.
     * @return true, если удаление прошло успешно, иначе false.
     */
    fun deleteSubscription(id: Long): Boolean = transaction {
        val subscriptionDao = SubscriptionDao.findById(id) ?: return@transaction false
        subscriptionDao.delete()
        true
    }

    /**
     * Получает все подписки.
     *
     * @return Список всех подписок.
     */
    fun getAllSubscriptions(): List<Subscription> = transaction {
        SubscriptionDao.all().map { it.toSubscription() }
    }
}

