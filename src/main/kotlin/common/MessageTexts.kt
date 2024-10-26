package org.example.common

enum class MessageTexts(val text: String) {
    MAIN_MENU("Добро пожаловать!"),
    SUBSCRIPTION_NOT_FOUND("У вас пока нет добавленных товаров"),
    AWAITING_FOR_LINK("Пришлите, пожалуйста, ссылку на товар"),
    CHOOSE_SUBSCRIPTION("Выберите товар из списка"),
    SUBSCRIPTION_ADDED("Товар добавлен!");

    enum class Error(val text: String) {
        SHOP_NOT_SUPPORTED("Ошибка! Магазин не поддерживается"),
        URL_IS_MALFORMED("Ошибка! Некорректная ссылка"),
        UNKNOWN_COMMAND("Ошибка! Неизвестная команда")
    }
}