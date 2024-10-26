package org.example

import org.example.bot.ChatBot
import org.example.common.context.SubscriptionContextManagerImpl
import org.example.common.context.SubscriptionContext
import org.example.service.TaskService
import org.example.storage.Database
import org.example.storage.models.Tariff
import org.example.storage.models.User
import org.example.storage.repository.SubscriptionRepository
import org.example.storage.repository.UserRepository
import org.example.storage.tables.Subscriptions

fun main() {
//    val contextManager = SubscriptionContextManagerImpl
//
//    val subs = SubscriptionRepository().getAllSubscriptions()
//    val userRepository = UserRepository()
//
//    subs.forEach {
//        contextManager.addContext(
//            SubscriptionContext(userRepository.getUserById(it.userId), it, it.article)
//        )
//    }
//
//    contextManager.getAllContexts().forEach { context ->
//        context.subscription.nextExecutionTime?.let {
//            TaskService.scheduleComparePriceTask(context, it)
//        }
//    }
//
//    contextManager.getContextsByUser(1).forEach { context ->
//        TaskService.executeTaskByNow(context)
//    }


    val bot = ChatBot().build()
    bot.startPolling()

//    val userRepository = UserRepository()
//    val subscriptionRepository = SubscriptionRepository()
//    println(userRepository.getAllUsers())
//    println(subscriptionRepository.getAllSubscriptions())
//
//    subscriptionRepository.getAllSubscriptions().forEach {
//        subscriptionRepository.deleteSubscription(it.id)
//    }
//
//    userRepository.getAllUsers().forEach {
//        userRepository.deleteUser(it.id)
//    }
}
