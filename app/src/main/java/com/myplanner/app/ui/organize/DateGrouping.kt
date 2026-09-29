package com.myplanner.app.ui.organize

import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

fun Long.toLocalDate(zone: ZoneId = ZoneId.systemDefault()): LocalDate =
    Instant.ofEpochMilli(this).atZone(zone).toLocalDate()

fun LocalDate.startMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    atStartOfDay(zone).toInstant().toEpochMilli()

fun LocalDate.endExclusiveMillis(zone: ZoneId = ZoneId.systemDefault()): Long =
    plusDays(1).startMillis(zone)

fun monthBounds(yearMonth: java.time.YearMonth, zone: ZoneId = ZoneId.systemDefault()): Pair<Long, Long> {
    val start = yearMonth.atDay(1).startMillis(zone)
    val end = yearMonth.plusMonths(1).atDay(1).startMillis(zone)
    return start to end
}

fun groupLabel(date: LocalDate, today: LocalDate = LocalDate.now()): String = when (date) {
    today -> "Today"
    today.plusDays(1) -> "Tomorrow"
    today.minusDays(1) -> "Yesterday"
    else -> date.format(DateTimeFormatter.ofPattern("EEEE, d MMM", Locale.getDefault()))
}

fun shortTime(millis: Long?, zone: ZoneId = ZoneId.systemDefault()): String? {
    if (millis == null) return null
    val z = Instant.ofEpochMilli(millis).atZone(zone)
    return "%02d:%02d".format(z.hour, z.minute)
}
