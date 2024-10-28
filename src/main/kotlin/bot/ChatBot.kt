package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.logging.LogLevel
import com.github.kotlintelegrambot.types.TelegramBotResult
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.example.common.MessageTexts
import org.example.service.ArticleService
import org.example.service.SubscriptionService
import org.example.service.TaskService
import org.example.service.UserService
import org.example.storage.Database
import org.example.storage.models.Article
import org.example.storage.models.Subscription
import org.example.storage.models.Tariff
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException
import kotlin.concurrent.thread

/**
 * Класс, представляющий чат-бота.
 *
 * Обрабатывает команды пользователей и управляет состоянием пользователей.
 */
class ChatBot {

    private var userStates = mutableMapOf<Long, UserState>()
    val articleService = ArticleService()
    val userService = UserService()
    val subscriptionService = SubscriptionService()
    val taskService = TaskService

    companion object {
        val logger = KotlinLogging.logger {}
        private const val TOKEN = "5316952420:AAEE9F4utvw3IwrfNEBsXcCwM6DTb6joOQA"
        private const val TIMEOUT_TIME = 30
    }

    /**
     * Перечисление возможных состояний пользователя.
     */
    enum class UserState {
        NONE,
        ARTICLE_AWAITING_TIME_TO_NOTIFY,
        ARTICLE_AWAITING_LINK,
        NOTIFICATION_AWAITING_TIME_TO_NOTIFY
    }

    /**
     * Создает и настраивает бота.
     *
     * @return Настроенный экземпляр бота.
     */
    fun build(): Bot {
        Database.connectToDatabase()
        return bot {
            token = TOKEN
            logLevel = LogLevel.Error
            timeout = TIMEOUT_TIME
            dispatch {
                handleStartCommand()
                handleChangeNotificationSettings()
                handleSubCommand()
                handleAddSubscription()
                handleRemoveSubscription()
                handleCheckPriceNow()
                handleSupportProject()
                handleCallbackQuery()
            }
        }
    }

    /**
     * Создает основную клавиатуру для взаимодействия с пользователем.
     *
     * @return Разметка клавиатуры.
     */
    private fun createMainKeyboard(): KeyboardReplyMarkup {
        return KeyboardReplyMarkup(
            keyboard = listOf(
                listOf(
                    KeyboardButton("Добавить товар"),
                    KeyboardButton("Удалить товар")
                ),
                listOf(
                    KeyboardButton("Список товаров"),
                    KeyboardButton("Узнать цену сейчас")
                ),
                listOf(
                    KeyboardButton("Поддержать проект"),
                    KeyboardButton("Настроить уведомления")
                )
            ),
            resizeKeyboard = true
        )
    }

    /**
     * Обрабатывает команду "/start" и возвращает пользователя в главное меню.
     */
    private fun Dispatcher.handleStartCommand() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message
            val userName = update.message?.chat?.username ?: return@message

            if (text == "/start" || text == "Вернуться в меню") {
                userService.addUserIfNotExist(chatId, userName, Tariff.STANDART)
                userStates[chatId] = UserState.NONE
                logSuccessOrError({
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = MessageTexts.MAIN_MENU.text,
                        replyMarkup = createMainKeyboard()
                    )
                })
                update.consume()
            }
        }
    }

    /**
     * Обрабатывает команду "Список товаров" и возвращает список подписок пользователя.
     */
    private fun Dispatcher.handleSubCommand() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Список товаров") {
                userStates[chatId] = UserState.NONE
                val currentSubscriptions = subscriptionService.getUserSubscriptions(chatId)
                val subscriptionText = if (currentSubscriptions.isEmpty()) {
                    MessageTexts.SUBSCRIPTION_NOT_FOUND.text
                } else {
                    currentSubscriptions.joinToString("\n") {
                        "[${it.article.name}](${it.article.url})` | Цена: ${it.article.price}` руб.\n\n"
                    }.let { "Ваши текущие подписки:\n\n$it" }
                }

                logSuccessOrError({
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = subscriptionText,
                        parseMode = ParseMode.MARKDOWN
                    )
                })

                update.consume()
            }
        }
    }

    /**
     * Обрабатывает изменения настроек уведомлений для пользователя.
     */
    private fun Dispatcher.handleChangeNotificationSettings() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Настроить уведомления" && userStates[chatId] == UserState.NONE) {
                userStates[chatId] = UserState.NOTIFICATION_AWAITING_TIME_TO_NOTIFY
                logSuccessOrError({
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = "Пришлите время для уведомления в формате - HH:MM",
                        replyMarkup = createMainKeyboard()
                    )
                })
                update.consume()
            } else if (userStates[chatId] == UserState.NOTIFICATION_AWAITING_TIME_TO_NOTIFY) {
                val user = userService.getUserById(chatId)
                try {
                    val userLocalTime = LocalTime.parse(text)
                    user.timeToNotify = userLocalTime
                    userService.updateUserInfo(user)
                    userStates[chatId] = UserState.NONE
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = "Время для уведомлений было успешно изменено на $text"
                    )
                    update.consume()
                } catch (e: DateTimeParseException) {
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = "Ошибка! Невереный формат времени!",
                        replyMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(
                                listOf(KeyboardButton("Вернуться в меню"))
                            )
                        )
                    )
                    update.consume()
                }
            }
        }
    }

    /**
     * Обрабатывает команду "Добавить товар" и добавляет новый товар в подписки пользователя.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun Dispatcher.handleAddSubscription() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            when (userStates[chatId]) {
                UserState.NONE -> {
                    if (text == "Добавить товар") {
                        val user = userService.getUserById(chatId)
                        val userSubscriptions = subscriptionService.getUserSubscriptions(user.id)

                        if (userSubscriptions.size >= 2) {
                            logSuccessOrError({
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "На текущий момент у вас уже добавлено максимальное количество товаров",
                                )
                            })
                            return@message
                        }

                        val timeToNotify = user.timeToNotify

                        if (timeToNotify != null) {
                            logSuccessOrError({
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = MessageTexts.AWAITING_FOR_LINK.text,
                                    replyMarkup = KeyboardReplyMarkup(
                                        keyboard = listOf(
                                            listOf(KeyboardButton("Вернуться в меню"))
                                        )
                                    )
                                )
                            })
                            userStates[chatId] = UserState.ARTICLE_AWAITING_LINK
                        } else {
                            logSuccessOrError({
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "Пришлите время для уведомления в формате - HH:MM",
                                    replyMarkup = createMainKeyboard()
                                )
                            })
                            userStates[chatId] = UserState.ARTICLE_AWAITING_TIME_TO_NOTIFY
                        }
                    }
                }

                UserState.ARTICLE_AWAITING_TIME_TO_NOTIFY -> {
                    val user = userService.getUserById(chatId)
                    try {
                        val userLocalTime = LocalTime.parse(text)
                        user.timeToNotify = userLocalTime
                        userService.updateUserInfo(user)
                        userStates[chatId] = UserState.ARTICLE_AWAITING_LINK
                        bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.AWAITING_FOR_LINK.text)
                    } catch (e: DateTimeParseException) {
                        bot.sendMessage(ChatId.fromId(chatId), text = "Ошибка! Невереный формат времени!")
                    }
                }

                UserState.ARTICLE_AWAITING_LINK -> {
                    GlobalScope.launch {
                        val url: URL?
                        try {
                            url = URL(text)
                        } catch (e: MalformedURLException) {
                            bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.Error.URL_IS_MALFORMED.text)
                            return@launch
                        }

                        val article: Article?

                        try {
                            bot.sendMessage(
                                ChatId.fromId(chatId),
                                text = "Пытаемся получить данные о товаре...",
                            )

                            article = articleService.parseArticle(url)


                        } catch (e: Exception) {
                            bot.sendMessage(
                                ChatId.fromId(chatId),
                                text = "При получении данных о товаре произошла ошибка. Попробуйте прислать ссылку еще раз.",
                            )
                            return@launch
                        }

                        val user = userService.getUserById(chatId)

                        if (article != null) {
                            var nextExecutionTime = user.timeToNotify?.let {
                                LocalDateTime.now()
                                    .withHour(it.hour)
                                    .withMinute(it.minute)
                                    .withSecond(0)
                            }

                            if (nextExecutionTime != null) {
                                if (nextExecutionTime < LocalDateTime.now()) {
                                    nextExecutionTime = nextExecutionTime.plusHours(24)
                                }

                                val subscription = subscriptionService.addSubscription(
                                    Subscription(
                                        id = 0,
                                        userId = chatId,
                                        article = article,
                                        createdTime = LocalDateTime.now(),
                                        nextExecutionTime = nextExecutionTime!!
                                    )
                                )
                                taskService.scheduleComparePriceTask(subscription, user, bot)
                                logSuccessOrError({
                                    bot.sendMessage(
                                        ChatId.fromId(chatId),
                                        text = MessageTexts.SUBSCRIPTION_ADDED.text,
                                        replyMarkup = createMainKeyboard()
                                    )
                                })

                                // Сбрасываем состояние
                                userStates[chatId] = UserState.NONE
                            }

                        } else {
                            bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.Error.SHOP_NOT_SUPPORTED.text)
                        }
                    }
                }

                else -> {
                    bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.Error.UNKNOWN_COMMAND.text)
                }
            }
        }
    }

    /**
     * Обрабатывает команду "Узнать цену сейчас" и проверяет изменение цен для подписок пользователя.
     */
    @OptIn(DelicateCoroutinesApi::class)
    private fun Dispatcher.handleCheckPriceNow() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Узнать цену сейчас") {
                GlobalScope.launch {
                    userStates[chatId] = UserState.NONE
                    val currentSubscriptions = subscriptionService.getUserSubscriptions(chatId)
                    if (currentSubscriptions.isEmpty()) {
                        logSuccessOrError({
                            bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.SUBSCRIPTION_NOT_FOUND.text)
                        })
                        return@launch
                    }
                    var message = ""
                    currentSubscriptions.forEach {
                        val (newPrice, isChanged) = articleService.checkPriceChange(it.article)
                        if (isChanged) {
                            subscriptionService.updateSubscriptionArticlePrice(it, newPrice!!)
                            message += "Цена на ${it.article.name} изменилась! " +
                                    "Старая цена: ${it.article.price} " +
                                    "Новая цена: ${newPrice}\n"
                        } else {
                            message += "Цена на ${it.article.name} не изменилась!\n"
                        }
                    }

                    logSuccessOrError({
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = message,
                            replyMarkup = createMainKeyboard()
                        )
                    })
                    update.consume()
                }
            }
        }
    }

    /**
     * Обрабатывает команду "Удалить товар" и предлагает пользователю выбрать подписку для удаления.
     */
    private fun Dispatcher.handleRemoveSubscription() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Удалить товар") {
                userStates[chatId] = UserState.NONE
                val currentSubscriptions = subscriptionService.getUserSubscriptions(chatId)
                if (currentSubscriptions.isEmpty()) {
                    logSuccessOrError({
                        bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.SUBSCRIPTION_NOT_FOUND.text)
                    })
                    return@message
                }

                val inlineKeyboard = InlineKeyboardMarkup.create(
                    currentSubscriptions.map { subscription ->
                        listOf(InlineKeyboardButton.CallbackData(
                            text = subscription.article.name,
                            callbackData = "deletesub_" + subscription.id.toString()
                        ))
                    }
                )

                logSuccessOrError({
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = MessageTexts.CHOOSE_SUBSCRIPTION.text,
                        replyMarkup = inlineKeyboard
                    )
                })
            }
        }
    }

    /**
     * Обрабатывает нажатия на кнопки с подписками для удаления.
     */
    private fun Dispatcher.handleCallbackQuery() {
        callbackQuery {
            val chatId = update.callbackQuery?.message?.chat?.id ?: return@callbackQuery
            val subscriptionInfo = update.callbackQuery?.data ?: return@callbackQuery

            val callbackData = subscriptionInfo.split("_")

            when(callbackData[0]) {
                "deletesub" -> {
                    val sub = subscriptionService.getSubscription(callbackData[1].toLong())
                    if (sub != null) {
                        taskService.cancelTask(sub)
                        subscriptionService.deleteSubscription(callbackData[1].toLong())
                        logSuccessOrError({
                            bot.sendMessage(
                                ChatId.fromId(chatId),
                                text = "Подписка на товар ${sub.article.name} удалена!",
                                replyMarkup = createMainKeyboard()
                            )
                        })
                    }
                }
            }
        }
    }

    /**
     * Обрабатывает команду "Поддержать проект".
     */
    private fun Dispatcher.handleSupportProject() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Поддержать проект") {
                userStates[chatId] = UserState.NONE
                logSuccessOrError({
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = "Поддержать проект: \"https://yoomoney.ru/main\""
                    )
                })
                update.consume()
            }
        }
    }

    /**
     * Логирует успешное или ошибочное выполнение операций.
     *
     * @param block Блок кода для выполнения.
     * @return Результат выполнения.
     */
    private fun <T> logSuccessOrError(vararg block: () -> TelegramBotResult<T>): TelegramBotResult<T> {
        lateinit var result: TelegramBotResult<T>

        block.forEach {
            result = it()

            result.fold(
                {
                    it as Message
                    logger.info { "Message was successfully send into chatID ${it.chat.id}. Message body: ${it.text.toString()}" }
                },
                {
                    logger.error { "There was a error while sending a message. Reason: $it" }
                }
            )
        }

        return result
    }
}

