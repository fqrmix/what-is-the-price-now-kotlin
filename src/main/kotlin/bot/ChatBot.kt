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
import org.example.org.example.bot.ChatContext
import org.example.service.*
import org.example.storage.Database
import org.example.storage.models.Article
import org.example.storage.models.FeedbackMessage
import org.example.storage.models.Subscription
import org.example.storage.models.Tariff
import java.math.BigDecimal
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

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
    val feedbackService = FeedbackService()

    companion object {
        val logger = KotlinLogging.logger {}
        private val TOKEN = System.getenv("TELEGRAM_TOKEN")
        private const val TIMEOUT_TIME = 30
    }

    /**
     * Перечисление возможных состояний пользователя.
     */
    enum class UserState {
        NONE,
        ARTICLE_AWAITING_TIME_TO_NOTIFY,
        ARTICLE_AWAITING_LINK,
        NOTIFICATION_AWAITING_TIME_TO_NOTIFY,
        FEEDBACK_AWAITING_MESSAGE
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
                handleFeedback()
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
                    KeyboardButton("Настроить уведомления")
                ),
                listOf(
                    KeyboardButton("Список товаров"),
                    KeyboardButton("Узнать цену сейчас")
                ),
                listOf(
                    KeyboardButton("Поддержать проект"),
                    KeyboardButton("Обратная связь")
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
                        replyMarkup = createMainKeyboard(),
                        parseMode = ParseMode.MARKDOWN
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
                if (currentSubscriptions.isEmpty()) {
                    logSuccessOrError({
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = MessageTexts.SUBSCRIPTION_NOT_FOUND.text,
                            parseMode = ParseMode.MARKDOWN
                        )
                    })

                } else {
                    logSuccessOrError({
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = "Ваши текущие подписки:",
                            parseMode = ParseMode.MARKDOWN
                        )
                    })

                    currentSubscriptions.forEach {

                        val deleteButton = InlineKeyboardMarkup.create(
                            listOf(InlineKeyboardButton.CallbackData(
                                text = "Удалить",
                                callbackData = "deletesub_" + it.id.toString()
                            ))
                        )

                        logSuccessOrError({
                            bot.sendMessage(
                                ChatId.fromId(chatId),
                                text =  "[${it.article.name}](${it.article.url})` | Цена: ${it.article.price} руб.`\n\n",
                                parseMode = ParseMode.MARKDOWN,
                                replyMarkup = deleteButton
                            )
                        })
                    }
                }
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
                    val subs = subscriptionService.getUserSubscriptions(user.id)
                    subs.forEach {
                        it.nextExecutionTime = it.nextExecutionTime.withHour(userLocalTime.hour)
                            .withMinute(userLocalTime.minute)
                        subscriptionService.updateSubscription(it)
                        taskService.cancelTask(it)
                        taskService.scheduleComparePriceTask(it, user, bot)
                    }

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

    private fun handleAddArticle(chatContext: ChatContext) {
        if (chatContext.text == "Добавить товар") {
            val user = userService.getUserById(chatContext.chatId)
            val userSubscriptions = subscriptionService.getUserSubscriptions(user.id)

            if (userSubscriptions.size >= 4) {
                logSuccessOrError({
                    chatContext.bot.sendMessage(
                        ChatId.fromId(chatContext.chatId),
                        text = "На текущий момент у вас уже добавлено максимальное количество товаров",
                    )
                })
                return
            }

            val timeToNotify = user.timeToNotify

            if (timeToNotify != null) {
                logSuccessOrError({
                    chatContext.bot.sendMessage(
                        ChatId.fromId(chatContext.chatId),
                        text = MessageTexts.AWAITING_FOR_LINK.text,
                        replyMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(
                                listOf(KeyboardButton("Вернуться в меню"))
                            )
                        )
                    )
                })
                userStates[chatContext.chatId] = UserState.ARTICLE_AWAITING_LINK
            } else {
                logSuccessOrError({
                    chatContext.bot.sendMessage(
                        ChatId.fromId(chatContext.chatId),
                        text = "Пришлите время для уведомления в формате - HH:MM",
                        replyMarkup = createMainKeyboard()
                    )
                })
                userStates[chatContext.chatId] = UserState.ARTICLE_AWAITING_TIME_TO_NOTIFY
            }
        }
    }

    private fun handleTimeToNotify(chatContext: ChatContext) {
        val user = userService.getUserById(chatContext.chatId)
        try {
            val userLocalTime = LocalTime.parse(chatContext.text)
            user.timeToNotify = userLocalTime
            userService.updateUserInfo(user)
            userStates[chatContext.chatId] = UserState.ARTICLE_AWAITING_LINK
            chatContext.bot.sendMessage(ChatId.fromId(chatContext.chatId), text = MessageTexts.AWAITING_FOR_LINK.text)
        } catch (e: DateTimeParseException) {
            chatContext.bot.sendMessage(ChatId.fromId(chatContext.chatId), text = "Ошибка! Невереный формат времени!")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun handleArticleLink(chatContext: ChatContext) {
        GlobalScope.launch {
            val url: URL?
            try {
                url = URL(chatContext.text)
            } catch (e: MalformedURLException) {
                chatContext.bot.sendMessage(ChatId.fromId(chatContext.chatId), text = MessageTexts.Error.URL_IS_MALFORMED.text)
                return@launch
            }

            val article: Article?

            try {
                chatContext.bot.sendMessage(
                    ChatId.fromId(chatContext.chatId),
                    text = "Пытаемся получить данные о товаре...",
                )
                article = articleService.parseArticle(url)
            } catch (e: Exception) {
                chatContext.bot.sendMessage(
                    ChatId.fromId(chatContext.chatId),
                    text = "При получении данных о товаре произошла ошибка. Попробуйте прислать ссылку еще раз.",
                )
                return@launch
            }

            val user = userService.getUserById(chatContext.chatId)

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
                            userId = chatContext.chatId,
                            article = article,
                            createdTime = LocalDateTime.now(),
                            nextExecutionTime = nextExecutionTime!!
                        )
                    )

                    taskService.scheduleComparePriceTask(subscription, user, chatContext.bot)
                    logSuccessOrError({
                        chatContext.bot.sendMessage(
                            ChatId.fromId(chatContext.chatId),
                            text = MessageTexts.SUBSCRIPTION_ADDED.text,
                            replyMarkup = createMainKeyboard()
                        )
                    })

                    // Сбрасываем состояние
                    userStates[chatContext.chatId] = UserState.NONE
                }

            } else {
                chatContext.bot.sendMessage(ChatId.fromId(chatContext.chatId), text = MessageTexts.Error.SHOP_NOT_SUPPORTED.text)
            }
        }
    }

    /**
     * Обрабатывает команду "Добавить товар" и добавляет новый товар в подписки пользователя.
     */
    private fun Dispatcher.handleAddSubscription() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            val chatContext = ChatContext(chatId, text, bot)

            when (userStates[chatId]) {
                UserState.NONE -> {
                    handleAddArticle(chatContext)
                }

                UserState.ARTICLE_AWAITING_TIME_TO_NOTIFY -> {
                    handleTimeToNotify(chatContext)
                }

                UserState.ARTICLE_AWAITING_LINK -> {
                    handleArticleLink(chatContext)
                }

                else -> {
//                    bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.Error.UNKNOWN_COMMAND.text)
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
                userStates[chatId] = UserState.NONE
                val currentSubscriptions = subscriptionService.getUserSubscriptions(chatId)
                if (currentSubscriptions.isEmpty()) {
                    logSuccessOrError({
                        bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.SUBSCRIPTION_NOT_FOUND.text)
                    })
                    return@message
                }
                currentSubscriptions.forEach {
                    GlobalScope.launch {
                        lateinit var result: Pair<BigDecimal?, Boolean>
                        try {
                            result = articleService.checkPriceChange(it.article)
                        } catch (e: Exception) {
                            logSuccessOrError({
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "Не удалось получить " +
                                            "актуальную цену на товар: [${it.article.name}](${it.article.url})\n" +
                                            "Возможно, товара нет в наличии.",
                                    parseMode = ParseMode.MARKDOWN,
                                    replyMarkup = createMainKeyboard()
                                )
                            })
                        }

                        if (result.second) {
                            subscriptionService.updateSubscriptionArticlePrice(it, result.first!!)
                            logSuccessOrError({
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "Цена на [${it.article.name}](${it.article.url}) изменилась! " +
                                            "Старая цена: `${it.article.price}`\n" +
                                            "Новая цена: `${result.first}`\n",
                                    parseMode = ParseMode.MARKDOWN,
                                    replyMarkup = createMainKeyboard()
                                )
                            })
                        } else {
                            logSuccessOrError({
                                bot.sendMessage(
                                    ChatId.fromId(chatId),
                                    text = "Цена на [${it.article.name}](${it.article.url}) не изменилась! " +
                                            "Стоимость: `${it.article.price} руб.`\n",
                                    parseMode = ParseMode.MARKDOWN,
                                    replyMarkup = createMainKeyboard()
                                )
                            })
                        }
                    }
                }
                update.consume()
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

                handleSubCommand()
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
                        text = MessageTexts.SUPPORT_PROJECT.text,
                        parseMode = ParseMode.MARKDOWN
                    )
                })
                update.consume()
            }
        }
    }

    /**
     * Обрабатывает команду "Обратная связь".
     */
    private fun Dispatcher.handleFeedback() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Обратная связь" && userStates[chatId] == UserState.NONE) {
                userStates[chatId] = UserState.FEEDBACK_AWAITING_MESSAGE
                logSuccessOrError({
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = MessageTexts.FEEDBACK.text,
                        parseMode = ParseMode.MARKDOWN,
                        replyMarkup = KeyboardReplyMarkup(
                            keyboard = listOf(
                                listOf(KeyboardButton("Вернуться в меню"))
                            )
                        )

                    )
                })
                update.consume()
            } else if (userStates[chatId] == UserState.FEEDBACK_AWAITING_MESSAGE) {
                feedbackService.addFeedbackMessage(
                    FeedbackMessage(id = 0, userId = chatId, message = text)
                )
                userStates[chatId] = UserState.NONE
                update.consume()
                logSuccessOrError({
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = MessageTexts.FEEDBACK_SUCCESS.text,
                        parseMode = ParseMode.MARKDOWN,
                        replyMarkup = createMainKeyboard()
                    )
                })
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

