package org.example.parser

enum class ParserConstants {
    ;

    enum class YandexMarket(val value: String) {
        PRICE("//*[@id=\"/content/page/fancyPage/defaultPage/mainDO\"]/div/div[2]/div[1]/div[4]/div[1]/div/div[2]/div[1]/div/div/div[2]/div[1]/div/div[1]/h3"),
        ARTICLE_NAME("//*[@id=\"/content/page/fancyPage/defaultPage/productTitle\"]/h1");
    }

    enum class Ozon(val value: String) {
        PRICE("//*[@id=\"layoutPage\"]/div[1]/div[4]/div[3]/div[2]/div/div/div[1]/div[3]/div/div[1]/div/div/div[1]/div[1]/button/span/div/div[1]/div/div/span"),
        ARTICLE_NAME("//*[@id=\"layoutPage\"]/div[1]/div[4]/div[3]/div[1]/div[1]/div[2]/div/div/div/div[1]/h1");
    }
}