package me.ws.repo

import kotliquery.using
import me.ws.model.stock.HistoricalSplit
import me.ws.repo.BaseDao.Companion.getSession

object HistoricalSplitDao : BaseDao {
    fun inserts(historicalSplits: Collection<HistoricalSplit>): Boolean {
        val res = using(getSession()) { session ->
            session.batchPreparedStatement(
                """
                    insert into historical_split(symbol, date, numerator, denominator) 
                    values (?, ?, ?, ?)
                    on conflict do nothing
                """.trimIndent(),
                historicalSplits.map { listOf(it.symbol, it.date, it.numerator, it.denominator) }
                    .toList()
            )
        }

        return res.sum() == historicalSplits.size
    }
}