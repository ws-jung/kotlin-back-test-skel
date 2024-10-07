package me.ws.service

import me.ws.model.*
import me.ws.model.stock.HistoricalQuote
import me.ws.repo.HistoricalQuoteDao
import java.time.LocalDate

class HistoricalQuoteService {
    private val historicalQuotes: MutableMap<Symbol, MutableMap<LocalDate, HistoricalQuote>> = mutableMapOf()

    fun init(
        symbol: Symbol,
        from: LocalDate,
        to: LocalDate,
    ) {
        attach(HistoricalQuoteDao.fetches(Currency.KRW.toCurrencySymbol(Currency.USD), from, to))
        attach(HistoricalQuoteDao.fetches(Currency.USD.toCurrencySymbol(Currency.KRW), from, to))
        attach(HistoricalQuoteDao.fetches(symbol, from, to))
    }

    private fun attach(historicalQuotes: Collection<HistoricalQuote>) {
        historicalQuotes.forEach { historicalQuote ->
            this.historicalQuotes.getOrPut(historicalQuote.symbol) { mutableMapOf() }[historicalQuote.date] =
                historicalQuote
        }
    }

    fun get(symbol: Symbol, date: LocalDate): HistoricalQuote? = historicalQuotes[symbol]?.get(date)
        ?: get(symbol, date.minusDays(1))

    fun getRange(symbol: Symbol, from: LocalDate, to: LocalDate): List<HistoricalQuote> = historicalQuotes[symbol]
        ?.let { quotes ->
            from.datesUntil(to.plusDays(1)).toList().mapNotNull { quotes[it] }
        }
        ?: emptyList()


}