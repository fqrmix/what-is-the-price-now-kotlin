package org.example.parser.selenium

import org.example.parser.selenium.stealth.SeleniumStealth4j
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions


/**
 * Объект для управления экземпляром `ChromeDriver` в приложении.
 *
 * Создает и настраивает драйвер для работы с браузером Chrome с помощью Selenium.
 * Объект автоматически проверяет, открыт ли браузер, и пересоздает драйвер при необходимости.
 */
object SeleniumManager {

    /**
     * Экземпляр ChromeDriver.
     *
     * Инициализируется при первом доступе, а также пересоздается, если браузер закрыт.
     */
    var webDriver: ChromeDriver? = null
        get() {
            if (field == null) {
                generateWebdriver()
            } else if (isBrowserClosed(field!!)) {
                generateWebdriver()
            }
            return field!!
        }

    /**
     * Проверяет, закрыт ли браузер.
     *
     * @param driver Экземпляр WebDriver для проверки.
     * @return `true`, если браузер закрыт, иначе `false`.
     */
    private fun isBrowserClosed(driver: WebDriver): Boolean {
        var isClosed = false
        try {
            driver.title
        } catch (ubex: WebDriverException) {
            isClosed = true
        }

        return isClosed
    }

    /**
     * Создает и настраивает экземпляр `ChromeDriver` с параметрами.
     *
     * Настройки включают: максимизацию окна, использование `no-sandbox` (для работы с Docker),
     * отключение автоматизации и установку стратегий загрузки страниц.
     */
    private fun generateWebdriver() {
        val options = ChromeOptions()
        options.addArguments("start-maximized")
        // options.addArguments("--headless") // Используйте этот аргумент для безголового режима
        options.addArguments("--no-sandbox") // для работы из Docker
        options.setExperimentalOption("excludeSwitches", listOf("enable-automation"))
        options.setExperimentalOption("useAutomationExtension", listOf("false"))
        options.setPageLoadStrategy(PageLoadStrategy.NONE)

        webDriver = ChromeDriver(options)

        SeleniumStealth4j.Builder(webDriver!!)
            .userAgent(
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) " +
                        "Chrome/83.0.4103.53 Safari/537.36"
            )
            .languages(arrayOf("en-US", "en"))
            .vendor("Google Inc.")
            .platform("Win32")
            //                .webglVendor("Intel Inc.")
            //                .renderer("Intel Iris OpenGL Engine")
            //                .fixHairline(true)
            //                .runOnInsecureOrigins(false)
            .build()
    }

    /**
     * Инициализатор объекта, который создает драйвер при создании экземпляра `SeleniumManager`.
     */
    init {
        generateWebdriver()
    }
}
