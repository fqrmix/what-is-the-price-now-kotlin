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
import org.example.command.ComparePriceCommand
import org.example.common.MessageTexts
import org.example.service.ArticleService
import org.example.service.TaskService
import org.example.storage.Database
import org.example.storage.models.User
import org.example.storage.models.Subscription
import org.example.storage.models.Tariff
import org.example.storage.repository.SubscriptionRepository
import org.example.storage.repository.UserRepository
import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeParseException

class ChatBot {

    private var userStates = mutableMapOf<Long, UserState>()
    private val subscriptionRepository = SubscriptionRepository()
    private val userRepository = UserRepository()
    private val articleService = ArticleService()
    private val taskService = TaskService

    companion object {
        val logger = KotlinLogging.logger {}
        private const val TOKEN = "5316952420:AAEE9F4utvw3IwrfNEBsXcCwM6DTb6joOQA"
        private const val TIMEOUT_TIME = 30
    }

    enum class UserState {
        NONE,
        ARTICLE_AWAITING_TIME_TO_NOTIFY,
        ARTICLE_AWAITING_LINK,
        NOTIFICATION_AWAITING_TIME_TO_NOTIFY
    }


    fun build(): Bot {
        Database.connectToDatabase()
        subscriptionRepository.getAllSubscriptions().forEach {
            TaskService.scheduleComparePriceTask(it, userRepository.getUserById(it.userId)!!)
        }
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

    private fun createMainKeyboard() : KeyboardReplyMarkup {
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
                ),
            ),
            resizeKeyboard = true
        )
    }

    private fun Dispatcher.handleStartCommand() {
        message  {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message
            val userName = update.message?.chat?.username ?: return@message

            if (text == "/start" || text == "Вернуться в меню") {

                if (userRepository.getUserById(chatId) == null) {
                    userRepository.addUser(
                        User(
                            id = chatId,
                            name = userName,
                            tariff = Tariff.STANDART
                        )
                    )
                }

                userStates[chatId] = UserState.NONE
                logSuccessOrError({
                    bot.sendMessage(
                        ChatId.fromId(chatId),
                        text = MessageTexts.MAIN_MENU.text,
                        replyMarkup = createMainKeyboard()
                    )
                })
            }
        }
    }

    private fun Dispatcher.handleSubCommand() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Список товаров") {
                userStates[chatId] = UserState.NONE
                val currentSubscriptions = subscriptionRepository.getSubscriptionsByUserId(chatId)
                val subscriptionText = if (currentSubscriptions.isEmpty()) {
                    MessageTexts.SUBSCRIPTION_NOT_FOUND.text
                } else {
                    var subString = ""
                    currentSubscriptions.forEach {
                        subString += it.article.name
                        subString += " : Цена = "
                        subString += it.article.price
                        subString += "\n"
                    }
                    "Ваши текущие подписки:\n${subString}"
                }

                // 2. Клавиатура с кнопками Добавить и Удалить
//                val keyboard = KeyboardReplyMarkup(
//                    keyboard = listOf(
//                        listOf(KeyboardButton("Добавить"), KeyboardButton("Удалить")),
//                        listOf(KeyboardButton("Вернуться в меню"))
//                    ),
//                    resizeKeyboard = true
//                )
                logSuccessOrError({
                    bot.sendMessage(ChatId.fromId(chatId), text = subscriptionText)
                })
            }
        }
    }

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
                val user = userRepository.getUserById(chatId)
                try {
                    val userLocalTime = LocalTime.parse(text)
                    if (user != null) {
                        user.timeToNotify = userLocalTime
                        userRepository.updateUser(
                            user
                        )
                        userStates[chatId] = UserState.NONE
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = "Время для уведомлений было успешно изменено на $text"
                        )
                        update.consume()
                    }
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

    private fun Dispatcher.handleAddSubscription() {
        message { ->
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            when (userStates[chatId]) {
                UserState.NONE -> {
                    if (text == "Добавить товар") {
                        val user = userRepository.getUserById(chatId)

                        if (user?.id?.let { subscriptionRepository.getSubscriptionsByUserId(it).size }!! >= 2) {
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
                    val user = userRepository.getUserById(chatId)
                    try {
                        val userLocalTime = LocalTime.parse(text)
                        if (user != null) {
                            user.timeToNotify = userLocalTime
                            userRepository.updateUser(
                                user
                            )
                            userStates[chatId] = UserState.ARTICLE_AWAITING_LINK
                            bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.AWAITING_FOR_LINK.text)
                        }
                    } catch (e: DateTimeParseException) {
                        bot.sendMessage(ChatId.fromId(chatId), text = "Ошибка! Невереный формат времени!")
                    }
                }

                UserState.ARTICLE_AWAITING_LINK -> {
                    val url: URL?
                    try {
                        url = URL(text)
                    } catch (e: MalformedURLException) {
                        bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.Error.URL_IS_MALFORMED.text)
                        return@message
                    }

                    val article = articleService.parseArticle(url)
                    val user = userRepository.getUserById(chatId)

                    if (article != null) {
                        if (user != null) {
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

                                val subscription = subscriptionRepository.addSubscription(
                                    Subscription(
                                        id = 0,
                                        userId = chatId,
                                        article = article,
                                        createdTime = LocalDateTime.now(),
                                        nextExecutionTime = nextExecutionTime!!
                                    )
                                )

                                taskService.scheduleComparePriceTask(subscription, user)

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
                        }

                    } else {
                        bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.Error.SHOP_NOT_SUPPORTED.text)
                    }
                }

                else -> {
                    bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.Error.UNKNOWN_COMMAND.text)
                }
            }
        }
    }

    private fun Dispatcher.handleCheckPriceNow() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Узнать цену сейчас") {
                userStates[chatId] = UserState.NONE
                val currentSubscriptions = subscriptionRepository.getSubscriptionsByUserId(chatId)
                if (currentSubscriptions.isEmpty()) {
                    logSuccessOrError({
                        bot.sendMessage(ChatId.fromId(chatId), text = MessageTexts.SUBSCRIPTION_NOT_FOUND.text)
                    })
                    return@message
                }
                var message = ""
                currentSubscriptions.forEach {
                    val oldPrice = it.article.price
                    val isPriceChanged = ComparePriceCommand().execute(it)
                    message += if (isPriceChanged) {
                        "Цена на ${it.article.name} изменилась! " +
                                "Старая цена: $oldPrice " +
                                "Новая цена: ${it.article.price}\n"

                    } else {
                        "Цена на ${it.article.name} не изменилась!\n"
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

    private fun Dispatcher.handleRemoveSubscription() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Удалить товар") {
                userStates[chatId] = UserState.NONE
                val currentSubscriptions = subscriptionRepository.getAllSubscriptions()
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

    // Обработчик нажатий на кнопки с подписками для удаления
    private fun Dispatcher.handleCallbackQuery() {
        callbackQuery {
            val chatId = update.callbackQuery?.message?.chat?.id ?: return@callbackQuery
            val subscriptionInfo = update.callbackQuery?.data ?: return@callbackQuery

            val callbackData = subscriptionInfo.split("_")

            when(callbackData[0]) {
                "deletesub" -> {
                    subscriptionRepository.deleteSubscription(callbackData[1].toLong())
                    logSuccessOrError({
                        bot.sendMessage(
                            ChatId.fromId(chatId),
                            text = "Подписка '${callbackData[1]}' удалена!",
                            replyMarkup = createMainKeyboard()
                        )
                    })
                }
            }
        }
    }

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
