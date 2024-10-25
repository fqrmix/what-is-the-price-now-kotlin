package org.example.common

import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.types.TelegramBotResult
import org.example.bot.ChatBot.Companion.logger

fun <T> logSuccessOrError(vararg block: () -> TelegramBotResult<T>): TelegramBotResult<T> {
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