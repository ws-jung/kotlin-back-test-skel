package me.ws.model.account

import kotlinx.serialization.Serializable
import me.ws.model.*
import me.ws.model.Currency
import me.ws.model.stock.HistoricalQuote
import me.ws.model.transaction.TransactionHistory
import me.ws.model.transaction.TransactionReservation
import mu.KLogging
import java.math.BigDecimal
import kotlin.random.Random

open class Account {
    val balances: MutableMap<Currency, Balance> = mutableMapOf()
    val portfolios: MutableMap<Symbol, Portfolio> = mutableMapOf()
    val transactionHistories: MutableList<TransactionHistory> = mutableListOf()

    fun init(seedMoney: BigDecimal) {
        balances[Currency.KRW] = Balance(Currency.KRW).apply {
            this.addAmount(seedMoney)
        }
        balances[Currency.USD] = Balance(Currency.USD)
    }

    /**
     * 입금
     */
    fun deposit(currency: Currency, amount: BigDecimal) {
        balances.getValue(currency).addAmount(amount)
    }

    /**
     * 출금
     */
    fun withdrawal(currency: Currency, amount: BigDecimal): Balance {
        balances.getValue(currency).addAmount(-amount)
        return Balance(currency).apply {
            this.addAmount(amount)
        }
    }

    /**
     * 출금 가능?
     */
    fun isAvailableWIthDraw(currency: Currency, amount: BigDecimal): Boolean {
        return balances.getValue(currency).amount >= amount
    }

    fun settlement(
        historicalQuote: HistoricalQuote,
        transactionReservation: TransactionReservation
    ): TransactionHistory? {
        val settlementCount = when (transactionReservation.type) {
            TransactionType.BUY -> {
                val boughtCount = buy(
                    transactionReservation.symbol,
                    transactionReservation.quantity,
                    transactionReservation.method.unitPrice(historicalQuote, transactionReservation),
                )
                boughtCount
            }

            TransactionType.SELL -> {
                val soldCount = sell(
                    transactionReservation.symbol,
                    transactionReservation.quantity,
                    transactionReservation.method.unitPrice(historicalQuote, transactionReservation),
                )
                soldCount
            }
        }
        if (settlementCount == 0) {
            return null
        }
        val history = TransactionHistory(transactionReservation)
        history.quantity = settlementCount
        transactionHistories.add(history)
        transactionReservation.quantity -= settlementCount
        return history
    }

    private fun buy(symbol: Symbol, quantity: Int, unitPrice: BigDecimal): Int {
        var bought = 0
        repeat(quantity) {
            if (
                Random.nextDouble(0.0, 1.0) <= TRANSACTION_PROBABILITY
                && isAvailableWIthDraw(Currency.USD, unitPrice)
            ) {
                withdrawal(Currency.USD, unitPrice)
                this.portfolios.getOrPut(symbol) { Portfolio(symbol) }
                    .buy(1, unitPrice)
                bought++
            }
        }
        return bought
    }

    private fun sell(symbol: Symbol, quantity: Int, unitPrice: BigDecimal): Int {
        var sold = 0
        repeat(quantity) {
            if (
                Random.nextDouble(0.0, 1.0) <= TRANSACTION_PROBABILITY
                && this.portfolios[symbol] != null
            ) {
                this.portfolios.getValue(symbol)
                    .sell(1)
                sold++
            }
        }
        deposit(Currency.USD, sold * unitPrice)
        transactionHistories
        return sold
    }

    companion object : KLogging()
}

@Serializable
open class Balance(
    val currency: Currency
) {
    var amount: BigDecimal = 0.0
        private set

    fun addAmount(amount: BigDecimal) {
        this.amount += amount
    }
}

@Serializable
open class Portfolio(
    val symbol: Symbol
) {
    var quantity: Int = 0
        private set
    var averageUnitPrice: BigDecimal = 0.0
        private set

    fun buy(quantity: Int, unitPrice: BigDecimal) {
        this.averageUnitPrice =
            (this.quantity * this.averageUnitPrice) + (quantity * unitPrice) / (this.quantity + quantity) * 1.0
        this.quantity += quantity
    }

    fun sell(quantity: Int) {
        check(this.quantity > 0)
        this.quantity -= quantity
    }
}

