package com.example.currencyexchangerateapp.data

data class Currency(
    val code: String,
    val name: String,
    val countryCode: String
)

object CurrencyData {

    val currencies = listOf(
        Currency("PLN", "Polish Zloty", "pl"),
        Currency("USD", "United States Dollar", "us"),
        Currency("EUR", "Euro", "eu"),
        Currency("GBP", "British Pound", "gb"),
        Currency("CHF", "Swiss Franc", "ch"),
        Currency("JPY", "Japanese Yen", "jp"),
        Currency("AUD", "Australian Dollar", "au"),
        Currency("CAD", "Canadian Dollar", "ca"),
        Currency("CNY", "Chinese Yuan", "cn"),
        Currency("HKD", "Hong Kong Dollar", "hk"),
        Currency("NZD", "New Zealand Dollar", "nz"),
        Currency("SEK", "Swedish Krona", "se"),
        Currency("KRW", "South Korean Won", "kr"),
        Currency("SGD", "Singapore Dollar", "sg"),
        Currency("NOK", "Norwegian Krone", "no")
    )

    fun getCurrencyByCode(code: String): Currency? {
        return currencies.find { it.code == code }
    }

    fun getCurrencyName(code: String): String {
        return getCurrencyByCode(code)?.name ?: "Unknown"
    }

    fun getFlagUrl(code: String): String {
        val countryCode = getCurrencyByCode(code)?.countryCode ?: "unknown"
        return "https://flagcdn.com/w40/$countryCode.png"
    }

    fun getAllCodes(): List<String> {
        return currencies.map { it.code }
    }
}
