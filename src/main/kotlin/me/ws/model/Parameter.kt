package me.ws.model

import me.ws.serialization.BigDecimalSerializer
import me.ws.strategy.Strategy
import java.math.BigDecimal
import java.time.LocalDate
import java.util.Currency

/**
 * Initial Amount: 최초 투자금
 * ticker symbol (복수)
 * 전략 (복수)
 */
class Parameter(
    val strategy: Strategy,
    val symbol: Symbol,
    val from: LocalDate,
    val to: LocalDate = LocalDate.now(),
    val cashFlow: CashFlow = CashFlow(),
    val cacheFrom: LocalDate = from,
    val cacheTo: LocalDate = to,
) {

}

class CashFlow(
    val currency: Currency = Currency.getInstance("KRW"),
    val seedMoney: BigDecimal = BigDecimal.ZERO,
    val type: CashFlowType = CashFlowType.NONE,
    val amount: BigDecimal = BigDecimal.ZERO,
    val dayOfMonth: Int = 0
)

enum class CashFlowType {
    /**
     * 없음
     */
    NONE,

    /**
     * 매달 특정 금액을 투자할 경우
     */
    CONTRIBUTE,

    /**
     * 매달 돈을 뺴 쓸 경우
     */
    WITHDRAW
}