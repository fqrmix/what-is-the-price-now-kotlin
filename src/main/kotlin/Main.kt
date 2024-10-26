package org.example

import org.example.bot.ChatBot
import org.example.service.TaskService


fun main() {
    val chatBotEnvironment = ChatBot()
    val bot = chatBotEnvironment.build()
    bot.startPolling()

    chatBotEnvironment.subscriptionService.getAllSubscriptions().forEach {
        val user = chatBotEnvironment.userService.getUserById(it.userId)
        TaskService.scheduleComparePriceTask(it, user, bot)
    }
}
