package org.example.service

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import org.example.common.RunnableTask
import org.example.common.Scheduler
import org.example.storage.models.Subscription
import org.example.storage.models.User
import org.example.storage.repository.SubscriptionRepository
import java.time.LocalDateTime

/**
 * Сервис для управления задачами, связанными с отслеживанием изменений цен на товары
 * и отправкой уведомлений пользователям.
 */
object TaskService {

    private val taskScheduler = Scheduler
    private val subscriptionRepository = SubscriptionRepository()
    private val subscriptionService = SubscriptionService()
    private val scheduledTasks = mutableMapOf<Long, RunnableTask>()
    private val articleService = ArticleService()

    /**
     * Запланировать задачу для сравнения цен на товар по подписке и отправки уведомления пользователю.
     *
     * @param subscription Подписка, для которой необходимо отслеживать изменения цены.
     * @param user Пользователь, который будет получать уведомления.
     * @param bot Объект бота, используемый для отправки сообщений.
     */
    fun scheduleComparePriceTask(subscription: Subscription, user: User, bot: Bot) {
        var nextExecutionTime = user.timeToNotify!!.let {
            LocalDateTime.now()
                .withHour(it.hour)
                .withMinute(it.minute)
                .withSecond(0)
        }

        // Если следующее время выполнения уже прошло, добавляем 24 часа
        if (nextExecutionTime < LocalDateTime.now()) {
            nextExecutionTime = nextExecutionTime.plusHours(24)
        }

        scheduleInternalTask(
            subscription.id,
            RunnableTask {
                try {
                    val (newPrice, isChanged) = articleService.checkPriceChange(subscription.article)
                    var message = ""
                    if (isChanged) {
                        subscriptionService.updateSubscriptionArticlePrice(subscription, newPrice!!)
                        message += "Цена на ${subscription.article.name} изменилась! " +
                                "Старая цена: ${subscription.article.price} " +
                                "Новая цена: ${newPrice}\n"
                    } else {
                        message += "Цена на ${subscription.article.name} не изменилась!\n"
                    }

                    bot.sendMessage(
                        ChatId.fromId(user.id),
                        text = message
                    )
                } catch (e: Exception) {
                    println(e.stackTrace)
                }
                subscription.nextExecutionTime = nextExecutionTime
                subscriptionRepository.updateSubscription(subscription)
                this.scheduleComparePriceTask(subscription, user, bot)
            },
            subscription.nextExecutionTime
        )
    }

    /**
     * Выполняет запланированную задачу немедленно.
     *
     * @param subscription Подписка, для которой необходимо выполнить задачу.
     */
    fun executeTaskByNow(subscription: Subscription) {
        scheduledTasks[subscription.id]?.let { this.taskScheduler.submitTask(it) }
    }

    /**
     * Запускает внутреннюю задачу с указанным идентификатором и временем выполнения.
     *
     * @param taskId Идентификатор задачи.
     * @param task Объект [RunnableTask], представляющий задачу.
     * @param executionTime Время, в которое задача должна быть выполнена.
     */
    private fun scheduleInternalTask(taskId: Long, task: RunnableTask, executionTime: LocalDateTime) {
        val newTask = taskScheduler.createScheduledTask(task, executionTime)
        if (newTask != null) {
            scheduledTasks[taskId] = task
        }
    }
}
