package org.example.service

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.ParseMode
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.example.common.RunnableTask
import org.example.common.Scheduler
import org.example.storage.models.Subscription
import org.example.storage.models.User
import org.example.storage.repository.SubscriptionRepository
import java.math.BigDecimal
import java.time.LocalDateTime
import java.util.concurrent.ScheduledFuture

/**
 * Сервис для управления задачами, связанными с отслеживанием изменений цен на товары
 * и отправкой уведомлений пользователям.
 */
object TaskService {

    private val taskScheduler = Scheduler
    private val subscriptionRepository = SubscriptionRepository()
    private val subscriptionService = SubscriptionService()
    private val scheduledTasks = mutableMapOf<Long, Pair<ScheduledFuture<*>, RunnableTask>>()
    private val articleService = ArticleService()

    /**
     * Запланировать задачу для сравнения цен на товар по подписке и отправки уведомления пользователю.
     *
     * @param subscription Подписка, для которой необходимо отслеживать изменения цены.
     * @param user Пользователь, который будет получать уведомления.
     * @param bot Объект бота, используемый для отправки сообщений.
     */
    @OptIn(DelicateCoroutinesApi::class)
    suspend fun scheduleComparePriceTask(subscription: Subscription, user: User, bot: Bot) {
        var nextExecutionTime = user.timeToNotify!!.let {
            LocalDateTime.now()
                .withHour(it.hour)
                .withMinute(it.minute)
                .withSecond(0)
                .withNano(0)
        }

        // Если следующее время выполнения уже прошло, добавляем 24 часа
        if (nextExecutionTime < LocalDateTime.now()) {
            nextExecutionTime = nextExecutionTime.plusHours(24)
        }

        scheduleInternalTask(
            subscription.id,
            RunnableTask {
                GlobalScope.launch {
                    try {
                        lateinit var result: Pair<BigDecimal?, Boolean>
                        try {
                            result = articleService.checkPriceChange(subscription.article)
                        } catch (e: Exception) {
                                bot.sendMessage(
                                    ChatId.fromId(user.id),
                                    text = "Не удалось получить " +
                                            "актуальную цену на товар: " +
                                            "[${subscription.article.name}](${subscription.article.url})\n" +
                                            "Возможно, товара нет в наличии.",
                                    parseMode = ParseMode.MARKDOWN,
                                )
                        }

                        var message = ""
                        if (result.second) {
                            subscriptionService.updateSubscriptionArticlePrice(subscription, result.first!!)
                            message += "Цена на [${subscription.article.name}](${subscription.article.url}) " +
                                    "Старая цена: `${subscription.article.price}`\n" +
                                    "Новая цена: `${result.first}`\n"
                        } else {
                            message += "Цена на [${subscription.article.name}](${subscription.article.url}) " +
                                    "не изменилась! Стоимость: `${subscription.article.price} руб.`\n"
                        }

                        bot.sendMessage(
                            ChatId.fromId(user.id),
                            text = message,
                            parseMode = ParseMode.MARKDOWN
                        )
                    } catch (e: Exception) {
                        println(e.stackTrace)
                    }
                    subscription.nextExecutionTime = nextExecutionTime
                    subscriptionRepository.updateSubscription(subscription)
                    scheduleComparePriceTask(subscription, user, bot)
                }
            },
            nextExecutionTime
        )
    }

    /**
     * Выполняет запланированную задачу немедленно.
     *
     * @param subscription Подписка, для которой необходимо выполнить задачу.
     */
    fun executeTaskByNow(subscription: Subscription) {
        scheduledTasks[subscription.id]?.let { this.taskScheduler.submitTask(it.second) }
    }

    /**
     * Отменяет запланированную задачу
     *
     * @param subscription Подписка, задачу для которой нужно удалить.
     */
    fun cancelTask(subscription: Subscription) {
        scheduledTasks[subscription.id]?.first?.cancel(true)
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
            scheduledTasks[taskId] = Pair(newTask, task)
        }
    }
}
