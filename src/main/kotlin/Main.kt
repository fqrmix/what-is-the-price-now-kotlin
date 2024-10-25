package org.example

import org.example.bot.ChatBot
import org.example.command.CheckPriceCommand
import org.example.common.ContextManagerImpl
import org.example.common.RunnableTask
import org.example.common.Scheduler
import org.example.common.SubscriptionContext
import org.example.storage.dao.SubscriptionRepository
import java.time.LocalDateTime

fun main() {
//    val scheduler = Scheduler

//    scheduler.init()
//    val contextManager = ContextManagerImpl()
//
//    val subs = SubscriptionRepository().getAllSubscriptions()
//    subs.forEach {
//        contextManager.addContext(SubscriptionContext(it.article, it.userId))
//    }
//
//    contextManager.getAllContexts().forEach { context ->
//        scheduler.createScheduledTask(
//            RunnableTask { CheckPriceCommand()
//                .execute(context) },
//            executionTime = LocalDateTime.now().plusSeconds(5)
//        )
//    }
//
//    contextManager.getAllContexts().forEach { context ->
//        scheduler.createScheduledTask(
//            RunnableTask { CheckPriceCommand()
//                .execute(context) },
//            executionTime = LocalDateTime.now().plusSeconds(30)
//        )
//    }

    val bot = ChatBot().build()
    bot.startPolling()
}
