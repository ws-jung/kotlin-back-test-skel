package me.ws.util

import java.time.LocalDate

class LocalDateIterator(val startDate: LocalDate,
                   val endDateInclusive: LocalDate,
                   val stepDays: Long): Iterator<LocalDate> {
    private var currentDate = startDate

    override fun hasNext() = currentDate <= endDateInclusive

    override fun next(): LocalDate {
        val next = currentDate
        currentDate = currentDate.plusDays(stepDays)
        return next
    }
}

class LocalDateProgression(override val start: LocalDate,
                      override val endInclusive: LocalDate,
                      val stepDays: Long = 1) :
    Iterable<LocalDate>, ClosedRange<LocalDate> {
    override fun iterator(): Iterator<LocalDate> = LocalDateIterator(start, endInclusive, stepDays)
    infix fun step(days: Long) = LocalDateProgression(start, endInclusive, days)

}

fun main() {
    val startDate = LocalDate.of(2020, 1, 1)
    val endDate = LocalDate.of(2020, 12, 31)
    startDate.datesUntil(endDate.plusDays(1))
        .forEach { println(it) }
}