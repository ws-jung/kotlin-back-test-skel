package me.ws.model.stock

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import me.ws.model.JsonBigDecimal
import me.ws.model.JsonLocalDate
import me.ws.model.Symbol
import me.ws.serialization.LocalDateSerializer
import me.ws.serialization.json
import java.math.BigDecimal
import java.time.LocalDate
import java.util.*

/**
 * denominator 개가 numerator 개로 액변 분할
 */
@Serializable
class HistoricalSplit(
    val symbol: Symbol,
    val date: JsonLocalDate,
    val numerator: JsonBigDecimal, // 분자
    val denominator: JsonBigDecimal, // 분모
) : Comparable<HistoricalSplit> {
    override fun compareTo(other: HistoricalSplit): Int = compareValuesBy(this, other, { it.symbol }, { it.date })

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