package me.ws.repo

import kotliquery.Row
import kotliquery.queryOf
import kotliquery.using
import me.ws.model.Symbol
import me.ws.model.stock.HistoricalQuote
import me.ws.repo.BaseDao.Companion.getSession
import java.time.LocalDate

object HistoricalQuoteDao : BaseDao {
    fun inserts(historicalQuotes: Collection<HistoricalQuote>): Boolean {
        val res = using(getSession()) { session ->

            session.batchPreparedStatement(
                """
                    insert into historical_quote(symbol, date, open, low, high, close, adjClose, volume) 
                    values (?, ?, ?, ?, ?, ?, ?, ?)
                    on conflict do nothing
                """.trimIndent(),
                historicalQuotes.map {
                    listOf(it.symbol, it.date, it.open, it.low, it.high, it.close, it.adjClose, it.volume)
                }
                    .toList()
            )
        }

        return res.sum() == historicalQuotes.size
    }

    private val toHistoricalQuote: (Symbol, Row) -> HistoricalQuote = { symbol, row ->
        HistoricalQuote(
            symbol = symbol,
            date = row.localDate("date"),
            open = row.bigDecimal("open"),
            low = row.bigDecimal("low"),
            high = row.bigDecimal("high"),
            close = row.bigDecimal("close"),
            adjClose = row.bigDecimal("adjClose"),
            volume = row.bigDecimal("volume"),
        )
    }

    fun fetches(symbol: Symbol, from: LocalDate, to: LocalDate = LocalDate.now()): List<HistoricalQuote> =
        using(getSession()) { session ->
            session.run(
                queryOf(
                    """
                    |select date, open, low, high, close, adjClose, volume 
                    |from historical_quote 
                    |where symbol = :symbol and date between :from and :to
                    |order by date
                    """.trimMargin("|"),
                    mapOf("symbol" to symbol, "from" to from, "to" to to)
                )
                    .map { row ->
                        toHistoricalQuote(symbol, row)
                    }
                    .asList
            )
        }

    fun fetch(symbol: Symbol, date: LocalDate): HistoricalQuote? =
        using(getSession()) { session ->
            session.run(
                queryOf(
                    """
                    |select date, open, low, high, close, adjClose, volume 
                    |from historical_quote 
                    |where symbol = :symbol and date = :date
                    |order by date
                    """.trimMargin("|"),
                    mapOf("symbol" to symbol, "date" to date)
                )
                    .map { row ->
                        toHistoricalQuote(symbol, row)
                    }
                    .asSingle
            )
        }
}