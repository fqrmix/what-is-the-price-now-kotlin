package org.example.bot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.*
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.parser.ShopName
import org.example.storage.dao.SubscriptionRepository

class ChatBot {

    private var userStates = mutableMapOf<Long, UserState>()
    private val selectedShops = mutableMapOf<Long, ShopName>()
    private val subscriptionService = SubscriptionRepository()

    companion object {
        val logger = KotlinLogging.logger {}
        private const val TOKEN = "5316952420:AAEE9F4utvw3IwrfNEBsXcCwM6DTb6joOQA"
        private const val TIMEOUT_TIME = 30
    }

    enum class UserState {
        NONE,
        AWAITING_SHOP_SELECTION,
        AWAITING_LINK
    }


    fun build(): Bot {
        return bot {
            token = TOKEN
            timeout = TIMEOUT_TIME
            dispatch {
                command("start") {
                    val chatId = update.message?.chat?.id ?: return@command
                    userStates[chatId] = UserState.NONE
                    bot.sendMessage(ChatId.fromId(chatId), text = "Добро пожаловать!", replyMarkup = createMainKeyboard())
                }
                handleSubCommand()
                handleAddSubscription()
                handleRemoveSubscription()
                handleCallbackQuery()
            }
        }
    }

    private fun createMainKeyboard() : KeyboardReplyMarkup {
        return KeyboardReplyMarkup(
            keyboard = listOf(
                listOf(KeyboardButton("Управление товарами")),
                listOf(KeyboardButton("Мой аккаунт"))
            ),
            resizeKeyboard = true
        )
    }

    private fun Dispatcher.handleSubCommand() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Управление товарами") {
                userStates[chatId] = UserState.NONE
                // 1. Выводим список текущих подписок
                val currentSubscriptions = subscriptionService.getSubscriptions(chatId)
                val subscriptionText = if (currentSubscriptions.isEmpty()) {
                    "У вас пока нет подписок."
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
                val keyboard = KeyboardReplyMarkup(
                    keyboard = listOf(
                        listOf(KeyboardButton("Добавить"), KeyboardButton("Удалить"))
                    ),
                    resizeKeyboard = true
                )

                bot.sendMessage(ChatId.fromId(chatId), text = subscriptionText, replyMarkup = keyboard)
            }
        }
    }

    private fun Dispatcher.handleAddSubscription() {
        message { ->
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            when (userStates[chatId]) {
                UserState.NONE -> {
                    if (text == "Добавить") {
                        // Переходим к выбору магазина
                        val shopKeyboard = KeyboardReplyMarkup(
                            keyboard = listOf(
                                listOf(
                                    KeyboardButton(ShopName.VINYLBOX.toString()),
                                    KeyboardButton(ShopName.OZON.toString()),
                                    KeyboardButton(ShopName.YANDEXMARKET.toString())
                                )
                            ),
                            resizeKeyboard = true
                        )

                        bot.sendMessage(ChatId.fromId(chatId), text = "Выберите магазин:", replyMarkup = shopKeyboard)
                        userStates[chatId] = UserState.AWAITING_SHOP_SELECTION
                    }
                }

                UserState.AWAITING_SHOP_SELECTION -> {
                    if (text in listOf(ShopName.YANDEXMARKET.toString(), ShopName.OZON.toString(), ShopName.VINYLBOX.toString())) {
                        // Сохраняем выбранный магазин и запрашиваем ссылку
                        selectedShops[chatId] = ShopName.valueOf(text)
                        bot.sendMessage(ChatId.fromId(chatId), text = "Пожалуйста, отправьте ссылку на товар:")
                        userStates[chatId] = UserState.AWAITING_LINK
                    } else {
                        bot.sendMessage(ChatId.fromId(chatId), text = "Пожалуйста, выберите один из предложенных магазинов.")
                    }
                }

                UserState.AWAITING_LINK -> {
                    // Получаем ссылку и завершаем добавление подписки
                    val selectedShop = selectedShops[chatId]
                    if (selectedShop != null) {
                        subscriptionService.addSubscription(chatId, shopName = selectedShop, url = text)
                        bot.sendMessage(ChatId.fromId(chatId), text = "Подписка добавлена!", replyMarkup = createMainKeyboard())

                        // Сбрасываем состояние
                        userStates[chatId] = UserState.NONE
                        selectedShops.remove(chatId)
                    } else {
                        bot.sendMessage(ChatId.fromId(chatId), text = "Ошибка: не выбран магазин.")
                    }
                }

                else -> {
                    bot.sendMessage(ChatId.fromId(chatId), text = "Неизвестная команда.")
                }
            }
        }
    }

    // Функция для обработки удаления подписки
    private fun Dispatcher.handleRemoveSubscription() {
        message {
            val chatId = update.message?.chat?.id ?: return@message
            val text = update.message?.text ?: return@message

            if (text == "Удалить") {
                val currentSubscriptions = subscriptionService.getSubscriptions(chatId)
                if (currentSubscriptions.isEmpty()) {
                    bot.sendMessage(ChatId.fromId(chatId), text = "У вас нет подписок для удаления.")
                    return@message
                }

                // 3. Клавиатура с текущими подписками для удаления
                val inlineKeyboard = InlineKeyboardMarkup.create(
                    currentSubscriptions.map { subscription ->
                        listOf(InlineKeyboardButton.CallbackData(
                            text = subscription.article.name,
                            callbackData = subscription.id.toString()
                        ))
                    }
                )

                bot.sendMessage(ChatId.fromId(chatId), text = "Выберите подписку для удаления:", replyMarkup = inlineKeyboard)
            }
        }
    }

    // Обработчик нажатий на кнопки с подписками для удаления
    private fun Dispatcher.handleCallbackQuery() {
        callbackQuery {
            val chatId = update.callbackQuery?.message?.chat?.id ?: return@callbackQuery
            val selectedSubscription = update.callbackQuery?.data ?: return@callbackQuery

            subscriptionService.removeSubscription(selectedSubscription.toLong())
            bot.sendMessage(ChatId.fromId(chatId), text = "Подписка '$selectedSubscription' удалена!", replyMarkup = createMainKeyboard())
        }
    }
}
