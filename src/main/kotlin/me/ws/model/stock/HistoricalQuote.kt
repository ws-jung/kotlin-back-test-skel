package me.ws.model.stock

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import me.ws.model.JsonBigDecimal
import me.ws.model.JsonLocalDate
import me.ws.model.Symbol
import me.ws.serialization.BigDecimalSerializer
import me.ws.serialization.LocalDateSerializer
import me.ws.serialization.json
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

@Serializable
class HistoricalQuote(
    val symbol: Symbol,
    val date: JsonLocalDate,
    val open: JsonBigDecimal,
    val low: JsonBigDecimal,
    val high: JsonBigDecimal,
    val close: JsonBigDecimal,
    val adjClose: JsonBigDecimal,
    val volume: JsonBigDecimal,
) : Comparable<HistoricalQuote> {
    override fun compareTo(other: HistoricalQuote): Int = compareValuesBy(this, other, { it.symbol }, { it.date })

    override fun hashCode(): Int {
        return Objects.hash(symbol, date)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as HistoricalQuote
        return symbol == other.symbol && date == other.date
    }

    override fun toString(): String = json.encodeToString(this)
}