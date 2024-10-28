package org.example.parser.selenium.stealth

import org.openqa.selenium.chrome.ChromeDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*


/**
 * Класс `SeleniumStealthWrapper` предоставляет методы для выполнения JavaScript-кода
 * с аргументами в контексте `ChromeDriver`, добавляя скрывающие автоматизацию настройки.
 */
class SeleniumStealthWrapper {

    /**
     * Генерирует строку JavaScript для выполнения на странице на основе переданной функции и аргументов.
     *
     * @param T тип аргументов.
     * @param `fun` строка, представляющая JavaScript-функцию, которую нужно выполнить.
     * @param args переменное количество аргументов для JavaScript-функции.
     * @return строка JavaScript, содержащая вызов функции с переданными аргументами.
     */
    @SafeVarargs
    fun <T> evaluationString(`fun`: String, vararg args: T): String {
        var _args = ""
        val stringJoiner = StringJoiner(", ")

        // Добавляет аргументы в строку вызова, поддерживаются только Boolean и String.
        if (args.isNotEmpty()) {
            for (arg in args) {
                if (arg!!::class.java == Boolean::class.java) {
                    stringJoiner.add(com.google.gson.JsonPrimitive(arg as Boolean).toString())
                } else if (arg.javaClass == String::class.java) {
                    stringJoiner.add(com.google.gson.JsonPrimitive(arg as String).toString())
                }
            }
            _args = stringJoiner.toString()
        }
        logger.debug("($`fun`)($_args)")
        return "($`fun`)($_args)"
    }

    /**
     * Выполняет JavaScript-код на новой странице через `ChromeDriver`, используя CDP-команду `Page.addScriptToEvaluateOnNewDocument`.
     * Это позволяет выполнять скрывающие автоматизацию скрипты до загрузки основной страницы.
     *
     * @param T тип аргументов.
     * @param webDriver экземпляр `ChromeDriver`, на котором выполняется скрипт.
     * @param pageFunction JavaScript-код для выполнения.
     * @param args аргументы для переданного JavaScript-кода.
     */
    @SafeVarargs
    fun <T> evaluateOnNewDocument(webDriver: ChromeDriver, pageFunction: String, vararg args: T) {
        val jsCode = evaluationString(pageFunction, *args)
        webDriver.executeCdpCommand(
            "Page.addScriptToEvaluateOnNewDocument",
            mapOf("source" to jsCode)
        )
    }

    companion object {
        /**
         * Логгер для класса `SeleniumStealthWrapper`.
         */
        private val logger: Logger = LoggerFactory.getLogger(SeleniumStealthWrapper::class.java)
    }
}
