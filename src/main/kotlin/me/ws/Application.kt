package me.ws

import kotlinx.serialization.encodeToString
import me.ws.model.CashFlow
import me.ws.model.Parameter
import me.ws.model.account.Account
import me.ws.model.stock.HistoricalQuote
import me.ws.model.transaction.TransactionHistory
import me.ws.serialization.json
import me.ws.service.HistoricalQuoteService
import me.ws.service.TransactionService
import me.ws.strategy.SampleStrategy
import mu.KLogging
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import kotlin.math.floor

class BackTestApplication(
    private val account: Account = Account(),
    private val historicalQuoteService: HistoricalQuoteService = HistoricalQuoteService(),
    private val transactionService: TransactionService = TransactionService(account, historicalQuoteService),
    private val parameter: Parameter = Parameter(
        strategy = SampleStrategy(account, transactionService, historicalQuoteService),
        symbol = "FNGU",
        from = LocalDate.of(2024, 8, 1),
        to = LocalDate.of(2024, 9, 20),
        cashFlow = CashFlow(seedMoney = BigDecimal(100_000_000)),
        cacheFrom = LocalDate.of(2024, 8, 1).minusDays(200)
    )
) {
    fun run() {
        account.init(parameter.cashFlow.seedMoney)
        historicalQuoteService.init(parameter.symbol, parameter.cacheFrom, parameter.cacheTo)
        parameter.strategy.init(parameter.symbol, parameter.from, parameter.to, parameter.cashFlow)
        parameter.from.datesUntil(parameter.to.plusDays(1)).forEach { date ->
            reserveTransaction(date)
            historicalQuoteService.get(parameter.symbol, date)?.let { historicalQuote ->
                val results = doTransaction(historicalQuote)
                finishTransaction(historicalQuote, results)
            }
        }
        account.balances.values.forEach {

            println(json.encodeToString(it))
        }
        account.portfolios.values.forEach {
            println("symbol: ${it.symbol}, quantity: ${it.quantity}, averageUnitPrice: ${it.averageUnitPrice.setScale(2, RoundingMode.HALF_UP)}")
        }
    }

    /**
     * 장 시작 전 수행
     */
    fun reserveTransaction(date: LocalDate) {
        transactionService.resetDaily()
        parameter.strategy.performInPremarketHours(date)
    }

    /**
     * 장 중 수행
     */
    fun doTransaction(historicalQuote: HistoricalQuote): Collection<TransactionHistory> {
        parameter.strategy.performInRegularMarketHours(historicalQuote)
        return transactionService.doDailyTransaction(parameter.symbol, historicalQuote)
    }

    /**
     * 장 종료 후 수행
     */
    fun finishTransaction(historicalQuote: HistoricalQuote, results: Collection<TransactionHistory>) {
        parameter.strategy.performInAfterHours(historicalQuote, results)
    }

    companion object : KLogging()
}

fun main() {
    BackTestApplication().run()
}