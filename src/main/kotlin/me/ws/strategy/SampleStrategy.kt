package me.ws.strategy

import me.ws.model.*
import me.ws.model.account.Account
import me.ws.model.stock.HistoricalQuote
import me.ws.model.transaction.TransactionHistory
import me.ws.model.transaction.TransactionReservation
import me.ws.serialization.BigDecimalSerializer
import me.ws.service.HistoricalQuoteService
import me.ws.service.TransactionService
import java.math.BigDecimal
import java.time.LocalDate
import kotlin.math.floor
import kotlin.math.round

/**
 * 전략:
 * 직전 200일 종가 평균 보다
 *     낮으면 전날 종가의 -5% 단가로 예수금의 10% 를 loc 매수,
 *     높으면 전날 종가의 +5% 단가로 포트폴리오의 10% 를 loc 매도,
 * -50% 면 stop loss
 */
class SampleStrategy(
    private val account: Account,
    private val transactionService: TransactionService,
    private val historicalQuoteService: HistoricalQuoteService,
) : Strategy {
    private lateinit var close200DayAverages: Map<LocalDate, BigDecimal>
    private lateinit var symbol: Symbol
    private lateinit var from: LocalDate
    private lateinit var to: LocalDate
    private lateinit var cashFlow: CashFlow

    override fun init(symbol: Symbol, from: LocalDate, to: LocalDate, cashFlow: CashFlow) {
        this.symbol = symbol
        this.from = from
        this.to = to
        this.cashFlow = cashFlow
        this.close200DayAverages = from.datesUntil(to.plusDays(1))
            .toList()
            .associateBy(
                { it },
                { close200DayAverage(symbol, it) }
            )
        transactionService.exchange(from, cashFlow.seedMoney, Currency.KRW, Currency.USD)
    }

    private fun close200DayAverage(symbol: Symbol, date: LocalDate): BigDecimal {
        val quotes = historicalQuoteService.getRange(symbol, date.minusDays(200), date)
        return quotes.sumOf { it.close }.divide(BigDecimal(quotes.size))
    }

    override fun performInPremarketHours(date: LocalDate) {
        val yesterday = date.minusDays(1)
        val portfolio = account.portfolios[this.symbol]
        val high200DayAvg = historicalQuoteService.get(symbol, yesterday)!!.close > close200DayAverage(symbol, date)
        if (portfolio != null && high200DayAvg && portfolio.quantity > 0) {
            // 매도 예약
            transactionService.reserve(
                TransactionReservation(
                    date = date,
                    symbol = this.symbol,
                    type = TransactionType.SELL,
                    method = TransactionMethod.LOC,
                    unitPrice = portfolio.averageUnitPrice * BigDecimal(1.05),
                    quantity = floor(portfolio.quantity / 4.0).toInt(),
                )
            )
        } else {
            // 매수 예약
            val price = historicalQuoteService.get(
                this.symbol,
                yesterday
            )!!
                .close * BigDecimal(0.95)
            val availableAmount = account.balances.getValue(Currency.USD).amount -
                    transactionService.getTotalReservedBalance(this.symbol)

            transactionService.reserve(
                TransactionReservation(
                    date = date,
                    symbol = this.symbol,
                    type = TransactionType.BUY,
                    method = TransactionMethod.LOC,
                    unitPrice = price,
                    quantity = floor(availableAmount / price).toInt(),
                )
            )
        }
    }

    override fun performInRegularMarketHours(historicalQuote: HistoricalQuote) {
    }

    override fun performInAfterHours(historicalQuote: HistoricalQuote, results: Collection<TransactionHistory>) {
    }

}