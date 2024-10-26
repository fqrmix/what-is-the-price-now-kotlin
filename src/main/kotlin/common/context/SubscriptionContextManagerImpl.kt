package org.example.common.context

object SubscriptionContextManagerImpl : ContextManager <SubscriptionContext> {

    // Коллекция для хранения контекстов
    private val contextList: MutableList<SubscriptionContext> = mutableListOf()

    // Добавление контекста
    override fun addContext(context: SubscriptionContext) {
        contextList.add(context)
    }

    // Получение всех контекстов для конкретного пользователя
    override fun getContextsByUserId(userId: Long): List<SubscriptionContext> {
        return contextList.filter { it.user?.id == userId }
    }

    // Получение всех контекстов
    override fun getAllContexts(): List<SubscriptionContext> {
        return contextList.toList()
    }

    // Удаление конкретного контекста
    override fun removeContext(context: SubscriptionContext): Boolean {
        return contextList.remove(context)
    }

    // Очистка всех контекстов
    override fun clearContexts() {
        contextList.clear()
    }
}