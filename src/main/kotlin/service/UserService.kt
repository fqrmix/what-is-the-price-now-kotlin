package org.example.service

import org.example.storage.models.Tariff
import org.example.storage.models.User
import org.example.storage.repository.UserRepository

/**
 * Сервис для управления пользователями.
 * Позволяет добавлять пользователей, получать информацию о них и обновлять данные.
 */
class UserService {

    private val userRepository: UserRepository = UserRepository()

    /**
     * Добавляет пользователя, если он еще не существует.
     *
     * @param userId Идентификатор пользователя.
     * @param username Имя пользователя.
     * @param tariff Тариф, связанный с пользователем.
     */
    fun addUserIfNotExist(userId: Long, username: String, tariff: Tariff) {
        if (userRepository.getUserById(userId) == null) {
            userRepository.addUser(
                User(
                    id = userId,
                    name = username,
                    tariff = tariff
                )
            )
        }
    }

    /**
     * Получает пользователя по его идентификатору.
     *
     * @param userId Идентификатор пользователя.
     * @return Пользователь с указанным идентификатором.
     * @throws RuntimeException Если пользователь не найден.
     */
    fun getUserById(userId: Long): User {
        val user = userRepository.getUserById(userId)
        if (user == null) {
            throw RuntimeException("User not found")
        } else return user
    }

    /**
     * Обновляет информацию о пользователе.
     *
     * @param user Объект пользователя с обновленными данными.
     */
    fun updateUserInfo(user: User) {
        userRepository.updateUser(user)
    }
}
