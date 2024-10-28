package org.example.parser.selenium.stealth


import org.openqa.selenium.chrome.ChromeDriver
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Map

/**
 * Класс `SeleniumStealth4j` предназначен для настройки `ChromeDriver` таким образом,
 * чтобы минимизировать вероятность обнаружения автоматизации браузера через Selenium.
 * Он скрывает автоматизацию, подстраивая различные параметры и поведение браузера.
 *
 * @property webDriver Экземпляр ChromeDriver, к которому применяются настройки.
 */
class SeleniumStealth4j private constructor(builder: Builder) {

    private val wrapper: SeleniumStealthWrapper = SeleniumStealthWrapper()
    private val webDriver: ChromeDriver = builder.webDriver
    private val userAgent: String
    private val languages: Array<String>
    private val vendor: String
    private val platform: String
    private val webglVendor: String
    private val renderer: String
    private val fixHairline: Boolean
    private val runOnInsecureOrigins: Boolean

    /**
     * Класс `Builder` предоставляет возможность пошаговой настройки и создания
     * объекта `SeleniumStealth4j`.
     *
     * @property webDriver Экземпляр ChromeDriver, требуемый для инициализации `SeleniumStealth4j`.
     */
    class Builder(
        internal val webDriver: ChromeDriver
    ) {
        var userAgent: String = ""
        var languages: Array<String> = arrayOf()
        var vendor: String = ""
        var platform: String = ""
        var webglVendor: String = "Intel Inc."
        var renderer: String = "Intel Iris OpenGL Engine"
        var fixHairline: Boolean = true
        var runOnInsecureOrigins: Boolean = false

        /**
         * Устанавливает пользовательский агент для браузера.
         * @param `val` строка user-agent.
         * @return Builder текущий экземпляр для поддержки цепочки вызовов.
         */
        fun userAgent(`val`: String): Builder {
            userAgent = `val`
            return this
        }

        /**
         * Устанавливает массив языков браузера.
         * @param `val` массив строк, представляющий языки.
         * @return Builder текущий экземпляр для поддержки цепочки вызовов.
         */
        fun languages(`val`: Array<String>): Builder {
            languages = `val`
            return this
        }

        /**
         * Устанавливает производителя браузера.
         * @param `val` строка, представляющая производителя, например, "Google Inc.".
         * @return Builder текущий экземпляр для поддержки цепочки вызовов.
         */
        fun vendor(`val`: String): Builder {
            vendor = `val`
            return this
        }

        /**
         * Устанавливает платформу для браузера.
         * @param `val` строка, представляющая платформу, например, "Win32".
         * @return Builder текущий экземпляр для поддержки цепочки вызовов.
         */
        fun platform(`val`: String): Builder {
            platform = `val`
            return this
        }

        /**
         * Устанавливает WebGL производителя.
         * @param `val` строка, представляющая WebGL производителя.
         * @return Builder текущий экземпляр для поддержки цепочки вызовов.
         */
        fun webglVendor(`val`: String): Builder {
            webglVendor = `val`
            return this
        }

        /**
         * Устанавливает WebGL рендерер.
         * @param `val` строка, представляющая рендерер.
         * @return Builder текущий экземпляр для поддержки цепочки вызовов.
         */
        fun renderer(`val`: String): Builder {
            renderer = `val`
            return this
        }

        /**
         * Включает или отключает фиксацию тонких линий рендеринга.
         * @param `val` значение true включает фиксацию.
         * @return Builder текущий экземпляр для поддержки цепочки вызовов.
         */
        fun fixHairline(`val`: Boolean): Builder {
            fixHairline = `val`
            return this
        }

        /**
         * Включает или отключает работу на небезопасных ресурсах.
         * @param `val` значение true включает эту возможность.
         * @return Builder текущий экземпляр для поддержки цепочки вызовов.
         */
        fun runOnInsecureOrigins(`val`: Boolean): Builder {
            runOnInsecureOrigins = `val`
            return this
        }

        /**
         * Создает и возвращает экземпляр `SeleniumStealth4j`.
         * @return `SeleniumStealth4j` объект с примененными настройками.
         */
        fun build(): SeleniumStealth4j {
            return SeleniumStealth4j(this)
        }
    }

    init {
        userAgent = builder.userAgent
        languages = builder.languages
        vendor = builder.vendor
        platform = builder.platform
        webglVendor = builder.webglVendor
        renderer = builder.renderer
        fixHairline = builder.fixHairline
        runOnInsecureOrigins = builder.runOnInsecureOrigins

        stealth(
            webDriver, userAgent, languages, vendor, platform, webglVendor, renderer, fixHairline,
            runOnInsecureOrigins
        )
    }

    /**
     * Применяет параметры скрытия к экземпляру `webDriver`.
     * @param webDriver ChromeDriver к которому применяются параметры.
     * @param userAgent строка user-agent.
     * @param languages массив языков.
     * @param vendor производитель браузера.
     * @param platform платформа браузера.
     * @param webglVendor WebGL производитель.
     * @param renderer WebGL рендерер.
     * @param fixHairline включает или отключает фиксацию тонких линий.
     * @param runOnInsecureOrigins включает работу на небезопасных ресурсах.
     */
    private fun stealth(
        webDriver: ChromeDriver,
        userAgent: String,
        languages: Array<String>,
        vendor: String,
        platform: String,
        webglVendor: String,
        renderer: String,
        fixHairline: Boolean,
        runOnInsecureOrigins: Boolean
    ) {
        val uaLanguages = java.lang.String.join(",", *languages)
        logger.debug("uaLanguages: $uaLanguages")

        jsLoader<Any>(webDriver, "js/utils.js")
        jsLoader<Any>(webDriver, "js/chrome.app.js")
        jsLoader(webDriver, "js/chrome.runtime.js", runOnInsecureOrigins)
        jsLoader<Any>(webDriver, "js/iframe.contentWindow.js")
        jsLoader<Any>(webDriver, "js/media.codecs.js")
        jsLoader(webDriver, "js/navigator.languages.js", *languages)
        jsLoader<Any>(webDriver, "js/navigator.permissions.js")
        jsLoader<Any>(webDriver, "js/navigator.plugins.js")
        jsLoader(webDriver, "js/navigator.vendor.js", vendor)
        jsLoader<Any>(webDriver, "js/navigator.webdriver.js")
        userAgentOverride(webDriver, userAgent, uaLanguages, platform)
        jsLoader(webDriver, "js/webgl.vendor.js", webglVendor, renderer)
        jsLoader<Any>(webDriver, "js/window.outerdimensions.js")
        if (fixHairline) jsLoader<Any>(webDriver, "js/hairline.fix.js")
    }

    /**
     * Загрузчик JavaScript, выполняет скрипты в `ChromeDriver`.
     * @param T тип параметров.
     * @param webDriver ChromeDriver, в который загружается JS.
     * @param jsFile название файла JS для загрузки.
     * @param args параметры, передаваемые в файл JS.
     */
    @SafeVarargs
    private fun <T> jsLoader(webDriver: ChromeDriver, jsFile: String, vararg args: T) {
        val inputStream = javaClass.classLoader.getResourceAsStream(jsFile)
        var pageFunction: String? = null
        try {
            if (inputStream != null) {
                pageFunction = String(inputStream.readAllBytes(), StandardCharsets.UTF_8)
            }
        } catch (e: IOException) {
            logger.info("Error occurred while reading $jsFile")
        }
        wrapper.evaluateOnNewDocument(webDriver, pageFunction!!, args)
    }

    /**
     * Устанавливает и/или изменяет `userAgent` и языковые настройки.
     * @param webDriver ChromeDriver, для которого применяется `userAgent`.
     * @param userAgent строка user-agent для применения.
     * @param uaLanguages строка, представляющая языковые настройки.
     * @param platform платформа браузера.
     */
    private fun userAgentOverride(webDriver: ChromeDriver, userAgent: String, uaLanguages: String, platform: String) {
        var ua: String = if (userAgent == "") {
            webDriver.executeCdpCommand(
                "Browser.getVersion",
                Map.of()
            )["userAgent"].toString()
        } else {
            userAgent
        }
        ua = ua.replace("HeadlessChrome".toRegex(), "Chrome")
        val overrideUserAgent = HashMap<String, Any>()

        overrideUserAgent["userAgent"] = ua
        if (uaLanguages != "") overrideUserAgent["acceptLanguage"] = uaLanguages
        if (platform != "") overrideUserAgent["platform"] = platform
        webDriver.executeCdpCommand("Network.setUserAgentOverride", overrideUserAgent)
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(SeleniumStealth4j::class.java)
    }
}
