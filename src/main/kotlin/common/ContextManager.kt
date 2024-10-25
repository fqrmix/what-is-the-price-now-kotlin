package org.example.common


// Интерфейс для ContextManager
interface ContextManager {
    fun addContext(context: SubscriptionContext)
    fun getContextsByUser(user: Long): List<SubscriptionContext>
    fun getAllContexts(): List<SubscriptionContext>
    fun removeContext(context: SubscriptionContext): Boolean
    fun clearContexts()
}

// Реализация ContextManager
class ContextManagerImpl : ContextManager {

    // Коллекция для хранения контекстов
    private val contextList: MutableList<SubscriptionContext> = mutableListOf()

    // Добавление контекста
    override fun addContext(context: SubscriptionContext) {
        contextList.add(context)
    }

    // Получение всех контекстов для конкретного пользователя
    override fun getContextsByUser(user: Long): List<SubscriptionContext> {
        return contextList.filter { it.user == user }
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