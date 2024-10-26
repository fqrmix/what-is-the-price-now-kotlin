package org.example.storage.repository

import org.example.storage.dao.UserDao
import org.example.storage.models.User
import org.jetbrains.exposed.sql.transactions.transaction


/**
 * Репозиторий для работы с пользователями.
 *
 * Обеспечивает методы для добавления, получения, обновления и удаления пользователей.
 */
class UserRepository {

    /**
     * Добавляет нового пользователя.
     *
     * @param user Объект пользователя, который нужно добавить.
     * @return Добавленный пользователь.
     */
    fun addUser(user: User): User = transaction {
        UserDao.new(user.id) {
            name = user.name
            tariff = user.tariff
            timeToNotify = user.timeToNotify
        }.toUser()
    }

    /**
     * Получает пользователя по идентификатору.
     *
     * @param id Идентификатор пользователя.
     * @return Пользователь, если найден, или null, если не найден.
     */
    fun getUserById(id: Long): User? = transaction {
        UserDao.findById(id)?.toUser()
    }

    /**
     * Обновляет информацию о существующем пользователе.
     *
     * @param user Объект пользователя с обновленными данными.
     * @return true, если обновление прошло успешно, иначе false.
     */
    fun updateUser(user: User): Boolean = transaction {
        val userDao = UserDao.findById(user.id) ?: return@transaction false
        userDao.name = user.name
        userDao.tariff = user.tariff
        userDao.timeToNotify = user.timeToNotify
        true
    }

    /**
     * Удаляет пользователя по идентификатору.
     *
     * @param id Идентификатор пользователя, которого нужно удалить.
     * @return true, если удаление прошло успешно, иначе false.
     */
    fun deleteUser(id: Long): Boolean = transaction {
        val userDao = UserDao.findById(id) ?: return@transaction false
        userDao.delete()
        true
    }

    /**
     * Получает всех пользователей.
     *
     * @return Список всех пользователей.
     */
    fun getAllUsers(): List<User> = transaction {
        UserDao.all().map { it.toUser() }
    }
}


