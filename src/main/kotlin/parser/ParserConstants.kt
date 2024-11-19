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

    enum class PultRu(val value: String) {
        PRICE("body > div.page > div.content > div.b-card > div.container > div:nth-child(2) > div.b-card__bar > div.d-none.d-lg-block.d-xl-block > div.b-card-action > div.b-card-prices > div.amount.amount--lg.amount--current"),
        ARTICLE_NAME("body > div.page > div.content > div.b-card > div.container > div:nth-child(1) > div > h1")
    }

    enum class DrHead(val value: String) {
        PRICE("div.product-price:nth-child(1) > span:nth-child(1)"),
        ARTICLE_NAME("body > div.page-container > div.box.box--overlay > div > div:nth-child(8) > h1")
    }

    enum class PlastinkaCom(val value: String) {
        PRICE("body > main > div.page-product > div.product-card > div.product-card__col-2 > div > div.product-card__price > span:nth-child(1)"),
        ARTICLE_NAME("body > main > div.page-product > h1")
    }

    enum class KorobkaVinila(val value: String) {
        PRICE("/html/body/div[1]/div[4]/div[1]/div[2]/div/div/div[2]/div[3]/div[1]/div[1]"),
        ARTICLE_NAME("/html/body/div[1]/div[4]/div[1]/div[2]/div/div/div[2]/div[1]/h1")
    }
}