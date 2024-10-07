package me.ws.model.transaction

import me.ws.model.Symbol
import me.ws.model.TransactionMethod
import me.ws.model.TransactionType
import java.math.BigDecimal
import java.time.LocalDate

class TransactionHistory private constructor(
    override val date: LocalDate,
    override val symbol: Symbol,
    override val type: TransactionType,
    override val method: TransactionMethod,
    override val unitPrice: BigDecimal,
    override var quantity: Int
) : TransactionReservation(
    date,
    symbol,
    type,
    method,
    unitPrice,
    quantity,
) {
    val createdAt: LocalDate = LocalDate.now()

    constructor(transactionReservation: TransactionReservation) : this(
        transactionReservation.date,
        transactionReservation.symbol,
        transactionReservation.type,
        transactionReservation.method,
        transactionReservation.unitPrice,
        transactionReservation.quantity
    )
}