package me.ws.model.transaction

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import me.ws.model.*
import me.ws.serialization.json

@Serializable
open class TransactionReservation(
    open val date: JsonLocalDate,
    open val symbol: Symbol,
    open val type: TransactionType,
    open val method: TransactionMethod,
    open val unitPrice: JsonBigDecimal,
    open var quantity: Int
) {
    override fun toString(): String = json.encodeToString(this)
}