package me.ws.model

import kotlinx.serialization.Serializable
import me.ws.model.stock.HistoricalQuote
import me.ws.model.transaction.TransactionReservation
import me.ws.serialization.BigDecimalSerializer
import me.ws.serialization.LocalDateSerializer
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.min

const val TRANSACTION_PROBABILITY: BigDecimal = BigDecimal(0.8)

typealias Symbol = String
typealias JsonLocalDate = @Serializable(with = LocalDateSerializer::class) LocalDate
typealias JsonBigDecimal = @Serializable(with = BigDecimalSerializer::class) BigDecimal

enum class Currency {
    KRW,
    USD;

    fun toCurrencySymbol(currency: Currency): Symbol = "$this$currency=X"
}

enum class TransactionType {
    BUY,
    SELL
}

enum class TransactionMethod(
    val canDailyTransaction: (HistoricalQuote, TransactionReservation) -> Boolean,
    val unitPrice: (HistoricalQuote, TransactionReservation) -> BigDecimal,
) {
    /**
     * 종가 보고 거래
     */
    LOC(
        { historicalQuote, reservation ->
            when (reservation.type) {
                TransactionType.BUY -> historicalQuote.close <= reservation.unitPrice
                TransactionType.SELL -> historicalQuote.close >= reservation.unitPrice
            }
        },
        { historicalQuote, reservation ->
            when (reservation.type) {
                TransactionType.BUY -> historicalQuote.close.min(reservation.unitPrice)
                TransactionType.SELL -> historicalQuote.close.max(reservation.unitPrice)
            }
        }
    ),

    /**
     * 지정가 거래
     */
    LIMIT_ORDER(
        { historicalQuote, reservation ->
            historicalQuote.low <= reservation.unitPrice && reservation.unitPrice <= historicalQuote.high
        },
        { _, reservation ->
            reservation.unitPrice
        }
    ),

    /**
     * 손절
     */
    STOP_LOSS(
        { _, _ ->
            false
        },
        { _, reservation ->
            reservation.unitPrice
        }
    )
}