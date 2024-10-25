package org.example.storage.dao

import org.example.storage.models.User

class UserRepository {

    private val userList = listOf(
        User(1, "Test1", "Test1@test.ru"),
        User(2, "Test2", "Test2@test.ru"),
        User(3, "Test3", "Test3@test.ru"),
        User(4, "Test4", "Test4@test.ru"),
        User(5, "Test5", "Test5@test.ru"),
        User(6, "Test6", "Test6@test.ru")
    )

    fun getAllUsers(): List<User> {
        return userList
    }

    fun getUserById(id: Long): User {
        return userList.first {
            it.id == id
        }
    }
}