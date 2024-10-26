package org.example.storage.repository

import org.example.storage.dao.UserDao
import org.example.storage.models.User

import org.jetbrains.exposed.sql.transactions.transaction

class UserRepository {

    // Добавление пользователя
    fun addUser(user: User): User = transaction {
        UserDao.new(user.id) {
            name = user.name
            tariff = user.tariff
            timeToNotify = user.timeToNotify
        }.toUser()
    }

    fun getUserById(id: Long): User? = transaction {
        UserDao.findById(id)?.toUser()
    }

    fun updateUser(user: User): Boolean = transaction {
        val userDao = UserDao.findById(user.id) ?: return@transaction false
        userDao.name = user.name
        userDao.tariff = user.tariff
        userDao.timeToNotify = user.timeToNotify
        true
    }

    fun deleteUser(id: Long): Boolean = transaction {
        val userDao = UserDao.findById(id) ?: return@transaction false
        userDao.delete()
        true
    }

    fun getAllUsers(): List<User> = transaction {
        UserDao.all().map { it.toUser() }
    }
}

