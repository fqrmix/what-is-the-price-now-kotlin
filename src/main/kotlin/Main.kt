package org.example

import org.example.bot.ChatBot
import org.example.service.TaskService


fun main() {
    val chatBotEnvironment = ChatBot()
    with(chatBotEnvironment) {
        val bot = this.build()
        bot.startPolling()
        this.subscriptionService.getAllSubscriptions().forEach {
            val user = chatBotEnvironment.userService.getUserById(it.userId)
            TaskService.scheduleComparePriceTask(it, user, bot)
        }
    }
}
