package org.example.common.context


// Интерфейс для ContextManager
interface ContextManager <T> {
    fun addContext(context: T)
    fun getContextsByUserId(userId: Long): List<T>
    fun getAllContexts(): List<T>
    fun removeContext(context: T): Boolean
    fun clearContexts()
}