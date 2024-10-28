package org.example.service

import org.example.storage.models.Subscription
import org.example.storage.repository.SubscriptionRepository
import java.math.BigDecimal

/**
 * Сервис для управления подписками пользователей, включая получение, добавление,
 * обновление и удаление подписок.
 */
class SubscriptionService {

    private val subscriptionRepository = SubscriptionRepository()

    /**
     * Возвращает список подписок для указанного пользователя.
     *
     * @param userId Идентификатор пользователя, для которого требуется получить подписки.
     * @return Список [Subscription], содержащий подписки пользователя.
     */
    fun getUserSubscriptions(userId: Long): List<Subscription> {
        return subscriptionRepository.getSubscriptionsByUserId(userId)
    }

    /**
     * Возвращает подписку по идентификатору.
     *
     * @param subId Идентификатор подписки.
     * @return [Subscription] Объект подписки.
     */
    fun getSubscription(subId: Long): Subscription? {
        return subscriptionRepository.getSubscriptionById(subId)
    }

    /**
     * Добавляет новую подписку и возвращает ее.
     *
     * @param subscription Объект [Subscription], который необходимо добавить.
     * @return Объект [Subscription], который был добавлен.
     */
    fun addSubscription(subscription: Subscription): Subscription {
        return subscriptionRepository.addSubscription(subscription)
    }

    /**
     * Обновляет цену статьи в подписке.
     *
     * @param subscription Объект [Subscription], для которого необходимо обновить цену статьи.
     * @param newPrice Новая цена статьи.
     */
    fun updateSubscriptionArticlePrice(subscription: Subscription, newPrice: BigDecimal) {
        subscription.article.price = newPrice
        subscriptionRepository.updateSubscription(subscription)
    }

    /**
     * Удаляет подписку по указанному идентификатору.
     *
     * @param subscriptionId Идентификатор подписки, которую необходимо удалить.
     */
    fun deleteSubscription(subscriptionId: Long) {
        subscriptionRepository.deleteSubscription(subscriptionId)
    }

    /**
     * Возвращает список всех подписок в системе.
     *
     * @return Список [Subscription], содержащий все подписки.
     */
    fun getAllSubscriptions(): List<Subscription> {
        return subscriptionRepository.getAllSubscriptions()
    }
}
