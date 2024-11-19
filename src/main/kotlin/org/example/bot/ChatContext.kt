package org.example.org.example.bot

import com.github.kotlintelegrambot.Bot

data class ChatContext(
    val chatId: Long,
    val text: String,
    val bot: Bot
)
