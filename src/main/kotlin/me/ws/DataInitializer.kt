package me.ws

import kotlinx.serialization.json.*
import me.ws.model.stock.HistoricalQuote
import me.ws.model.stock.HistoricalSplit
import me.ws.repo.HistoricalQuoteDao
import me.ws.repo.HistoricalSplitDao
import me.ws.serialization.json
import me.ws.service.yfapi.YfApiClient
import retrofit2.awaitResponse
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneOffset

suspend fun main() {
    val toDouble: (JsonElement) -> Double? = {
        try {
            (it as JsonPrimitive).content.toDouble()
        } catch (e: Exception) {
            null
        }
    }
    val toLong: (JsonElement) -> Long? = {
        try {
            (it as JsonPrimitive).content.toLong()
        } catch (e: Exception) {
            null
        }
    }
    val toLocalDate: (JsonElement) -> LocalDate? = {
        try {
            val l = toLong(it)
            if (l == null) {
                null
            } else {
                LocalDateTime.ofEpochSecond(l, 0, ZoneOffset.UTC).toLocalDate()
            }
        } catch (e: Exception) {
            null
        }
    }

//    val inputSymbol = "FNGU"

    // JSON 파일 경로
    val response = YfApiClient.create().getChart("KRWUSD=X").awaitResponse().body()!!
    val data = json.decodeFromString<JsonElement>(response)
        .jsonObject.getValue("chart")
        .jsonObject.getValue("result")
        .jsonArray
        .first()

    val symbol = data.jsonObject.getValue("meta").jsonObject.getValue("symbol").jsonPrimitive.content
    val splits = data.jsonObject["events"]?.jsonObject?.get("splits")
    val historicalSplits = splits?.jsonObject?.mapNotNull { split ->
        val date = toLocalDate(split.value.jsonObject.getValue("date"))
        val numerator = toDouble(split.value.jsonObject.getValue("numerator"))
        val denominator = toDouble(split.value.jsonObject.getValue("denominator"))
        if (date == null || numerator == null || denominator == null) {
            null
        } else {
            HistoricalSplit(symbol, date, numerator, denominator)
        }
    }
        ?.toList()
        ?: emptyList()
    HistoricalSplitDao.inserts(historicalSplits)

    val quote = data.jsonObject.getValue("indicators").jsonObject.getValue("quote").jsonArray.first().jsonObject

    val volumes = quote.getValue("volume").jsonArray.map(toDouble).toList()
    val opens = quote.getValue("open").jsonArray.map(toDouble)
    val highs = quote.getValue("high").jsonArray.map(toDouble)
    val lows = quote.getValue("low").jsonArray.map(toDouble)
    val closes = quote.getValue("close").jsonArray.map(toDouble)
    val adjCloses = data.jsonObject.getValue("indicators")
        .jsonObject.getValue("adjclose")
        .jsonArray.first()
        .jsonObject.getValue("adjclose")
        .jsonArray.map(toDouble)

    val historicalQuotes = data.jsonObject.getValue("timestamp").jsonArray.mapIndexedNotNull { index, jsonElement ->
        val date = toLocalDate(jsonElement)
        val open = opens[index]
        val high = highs[index]
        val low = lows[index]
        val close = closes[index]
        val adjClose = adjCloses[index]
        val volume = volumes[index]

        if (date == null || open == null || high == null || low == null || close == null || adjClose == null || volume == null) {
            null
        } else {
            HistoricalQuote(
                date = date,
                open = open,
                high = high,
                low = low,
                close = close,
                adjClose = adjClose,
                volume = volume,
                symbol = symbol,
            )
        }
    }

    HistoricalQuoteDao.inserts(historicalQuotes)
}