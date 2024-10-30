package org.example.parser.selenium

import io.github.bonigarcia.wdm.WebDriverManager
import io.github.oshai.kotlinlogging.KotlinLogging
import org.example.parser.selenium.stealth.SeleniumStealth4j
import org.openqa.selenium.PageLoadStrategy
import org.openqa.selenium.WebDriver
import org.openqa.selenium.WebDriverException
import org.openqa.selenium.chrome.ChromeDriver
import org.openqa.selenium.chrome.ChromeOptions
import org.openqa.selenium.chromium.ChromiumDriver
import org.openqa.selenium.remote.DesiredCapabilities
import org.openqa.selenium.remote.RemoteWebDriver
import java.net.URL


/**
 * Объект для управления экземпляром `ChromeDriver` в приложении.
 *
 * Создает и настраивает драйвер для работы с браузером Chrome с помощью Selenium.
 * Объект автоматически проверяет, открыт ли браузер, и пересоздает драйвер при необходимости.
 */
class SeleniumManager {

    companion object {
        val logger = KotlinLogging.logger {}
    }

    /**
     * Экземпляр ChromeDriver.
     *
     * Инициализируется при первом доступе, а также пересоздается, если браузер закрыт.
     */
    var webDriver: ChromiumDriver? = null
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
        try {
            val chromeOptions = ChromeOptions()
            // options.addArguments("--headless") // Используйте этот аргумент для безголового режима
            chromeOptions.setExperimentalOption("excludeSwitches", listOf("enable-automation"))
            chromeOptions.setExperimentalOption("useAutomationExtension", listOf("false"))
            chromeOptions.setPageLoadStrategy(PageLoadStrategy.NONE)
//            chromeOptions.addArguments("--headless")
            chromeOptions.addArguments("--no-sandbox")
            chromeOptions.addArguments("--disable-gpu")
            chromeOptions.addArguments("--window-size=1280x1696")
            chromeOptions.addArguments("--single-process")
            chromeOptions.addArguments("--disable-dev-shm-usage")
            chromeOptions.addArguments("--disable-dev-tools")
            chromeOptions.addArguments("--no-zygote")
            chromeOptions.addArguments("--user-data-dir=/tmp")
            chromeOptions.addArguments("--data-path=/tmp")
            chromeOptions.addArguments("--disk-cache-dir=/tmp")
            chromeOptions.addArguments("--remote-debugging-port=9222")
//            chromeOptions.setBinary("/opt/chrome/chrome")
//            System.setProperty("webdriver.chrome.driver", "/usr/local/bin/chromedriver")

//            webDriver = ChromeDriver(chromeOptions)
            val capabilities = DesiredCapabilities();
            capabilities.setCapability(ChromeOptions.CAPABILITY, chromeOptions)

            WebDriverManager.chromedriver()
                .capabilities(capabilities)
                .remoteAddress("http://standalone-chrome:4444/wd/hub")
                .create()

//            webDriver = RemoteWebDriver(URL("http://standalone-chrome:4444/wd/hub"), capabilities) as ChromiumDriver

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
        } catch (e: Exception) {
            logger.error { e.printStackTrace() }
            throw e
        }

    }

    /**
     * Инициализатор объекта, который создает драйвер при создании экземпляра `SeleniumManager`.
     */
    init {
        generateWebdriver()
    }
}
