package me.ws.service

import me.ws.model.Currency
import me.ws.model.Symbol
import me.ws.model.TransactionType
import me.ws.model.account.Account
import me.ws.model.stock.HistoricalQuote
import me.ws.model.transaction.TransactionHistory
import me.ws.model.transaction.TransactionReservation
import mu.KLogging
import java.math.BigDecimal
import java.time.LocalDate

class TransactionService(
    private val account: Account,
    private val historicalQuoteService: HistoricalQuoteService,
) : KLogging() {
    private val transactionReservations: MutableMap<Symbol, MutableSet<TransactionReservation>> = mutableMapOf()

    fun resetDaily() {
        transactionReservations.clear()
    }

    fun exchange(date: LocalDate, amount: BigDecimal, from: Currency, to: Currency) {
        require(
            account.isAvailableWIthDraw(from, amount)
        ) { "$date - [환전][$from] 잔고 부족." }
        val yesterdayClose = historicalQuoteService.get(from.toCurrencySymbol(to), date)!!
            .close
        val toAmount = account.withdrawal(from, amount).amount * yesterdayClose
        account.deposit(to, toAmount)
        logger.info(String.format("$date - [환전] $from %.2f -> $to %.2f", amount, toAmount))
    }

    fun getTotalReservedBalance(symbol: Symbol): BigDecimal {
        return (transactionReservations[symbol]?.sumOf { it.unitPrice * BigDecimal(it.quantity) } ?: BigDecimal.ZERO)
    }

    fun reserve(reservation: TransactionReservation) {
        val total = reservation.quantity * reservation.unitPrice + getTotalReservedBalance(reservation.symbol)
        if (reservation.type == TransactionType.BUY) {
            require(
                account.isAvailableWIthDraw(Currency.USD, total)
            ) {
                "[${reservation.symbol}] 잔고 부족. $reservation ${account.balances[Currency.USD]?.amount}"
            }
        }
        logger.info("[예약] $reservation")
        transactionReservations.getOrPut(reservation.symbol) { mutableSetOf() }.add(reservation)
    }

    fun doDailyTransaction(symbol: Symbol, historicalQuote: HistoricalQuote): List<TransactionHistory> {
        val reservations = transactionReservations[symbol] ?: return emptyList()
        return reservations.asSequence()
            .filter { it.method.canDailyTransaction(historicalQuote, it) }
            .mapNotNull {
                val result = account.settlement(historicalQuote, it)
                if (result != null) {
                    reservations.remove(it)
                }
                result
            }
            .toList()
    }
}