package me.ws.strategy

import me.ws.model.CashFlow
import me.ws.model.Symbol
import me.ws.model.stock.HistoricalQuote
import me.ws.model.transaction.TransactionHistory
import java.time.LocalDate

interface Strategy {
    fun init(symbol: Symbol, from: LocalDate, to: LocalDate, cashFlow: CashFlow)

    /**
     * 04:00 ~ 08:00: Premarket Hours
     *
     * 지정가 매수/매도 걸 때 여기서 한다.
     */
    fun performInPremarketHours(date: LocalDate)

    /**
     * 08:00 ~ 16:00: Regular Market Hours
     *
     * 지정가 매수/매도, stop loss 매도 체결될 떄 여기서 한다.
     *
     */
    fun performInRegularMarketHours(historicalQuote: HistoricalQuote)

    /**
     * 16:00 ~ 20:00: After Hours
     *
     * 결과 저장 시에 여기서 한다.
     */
    fun performInAfterHours(historicalQuote: HistoricalQuote, results: Collection<TransactionHistory>)
}