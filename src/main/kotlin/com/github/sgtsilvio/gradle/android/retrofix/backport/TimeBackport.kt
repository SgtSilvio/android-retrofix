package com.github.sgtsilvio.gradle.android.retrofix.backport

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import org.slf4j.LoggerFactory

/**
 * @author Silvio Giebl
 */
class TimeBackport : Backport {

    companion object {
        private val logger = LoggerFactory.getLogger(TimeBackport::class.java)
    }

    override val indicatorClass get() = "org/threeten/bp/Clock"

    override fun isInstrumentable(className: String) = !className.startsWith("org/threeten/bp/")

    override fun apply(typeMap: TypeMap, methodMap: MethodMap) {
        logger.info("Backporting threetenbp")
        mapTypes(typeMap)
        mapMethods(methodMap)
    }

    private fun mapTypes(map: TypeMap) {
        // java.time
        map["java/time/Clock"] = "org/threeten/bp/Clock"
        map["java/time/DateTimeException"] = "org/threeten/bp/DateTimeException"
        map["java/time/DayOfWeek"] = "org/threeten/bp/DayOfWeek"
        map["java/time/Duration"] = "org/threeten/bp/Duration"
        map["java/time/Instant"] = "org/threeten/bp/Instant"
        map["java/time/LocalDate"] = "org/threeten/bp/LocalDate"
        map["java/time/LocalDateTime"] = "org/threeten/bp/LocalDateTime"
        map["java/time/LocalTime"] = "org/threeten/bp/LocalTime"
        map["java/time/Month"] = "org/threeten/bp/Month"
        map["java/time/MonthDay"] = "org/threeten/bp/MonthDay"
        map["java/time/OffsetDateTime"] = "org/threeten/bp/OffsetDateTime"
        map["java/time/OffsetTime"] = "org/threeten/bp/OffsetTime"
        map["java/time/Period"] = "org/threeten/bp/Period"
        map["java/time/Year"] = "org/threeten/bp/Year"
        map["java/time/YearMonth"] = "org/threeten/bp/YearMonth"
        map["java/time/ZonedDateTime"] = "org/threeten/bp/ZonedDateTime"
        map["java/time/ZoneId"] = "org/threeten/bp/ZoneId"
        map["java/time/ZoneOffset"] = "org/threeten/bp/ZoneOffset"

        // java.time.chrono
        map["java/time/chrono/AbstractChronology"] = "org/threeten/bp/chrono/AbstractChronology"
        map["java/time/chrono/ChronoLocalDate"] = "org/threeten/bp/chrono/ChronoLocalDate"
        map["java/time/chrono/ChronoLocalDateTime"] = "org/threeten/bp/chrono/ChronoLocalDateTime"
        map["java/time/chrono/Chronology"] = "org/threeten/bp/chrono/Chronology"
        map["java/time/chrono/ChronoPeriod"] = "org/threeten/bp/chrono/ChronoPeriod"
        map["java/time/chrono/ChronoZonedDateTime"] = "org/threeten/bp/chrono/ChronoZonedDateTime"
        map["java/time/chrono/Era"] = "org/threeten/bp/chrono/Era"
        map["java/time/chrono/HijrahChronology"] = "org/threeten/bp/chrono/HijrahChronology"
        map["java/time/chrono/HijrahDate"] = "org/threeten/bp/chrono/HijrahDate"
        map["java/time/chrono/HijrahEra"] = "org/threeten/bp/chrono/HijrahEra"
        map["java/time/chrono/IsoChronology"] = "org/threeten/bp/chrono/IsoChronology"
        map["java/time/chrono/IsoEra"] = "org/threeten/bp/chrono/IsoEra"
        map["java/time/chrono/JapaneseChronology"] = "org/threeten/bp/chrono/JapaneseChronology"
        map["java/time/chrono/JapaneseDate"] = "org/threeten/bp/chrono/JapaneseDate"
        map["java/time/chrono/JapaneseEra"] = "org/threeten/bp/chrono/JapaneseEra"
        map["java/time/chrono/MinguoChronology"] = "org/threeten/bp/chrono/MinguoChronology"
        map["java/time/chrono/MinguoDate"] = "org/threeten/bp/chrono/MinguoDate"
        map["java/time/chrono/MinguoEra"] = "org/threeten/bp/chrono/MinguoEra"
        map["java/time/chrono/ThaiBuddhistChronology"] = "org/threeten/bp/chrono/ThaiBuddhistChronology"
        map["java/time/chrono/ThaiBuddhistDate"] = "org/threeten/bp/chrono/ThaiBuddhistDate"
        map["java/time/chrono/ThaiBuddhistEra"] = "org/threeten/bp/chrono/ThaiBuddhistEra"

        // java.time.format
        map["java/time/format/DateTimeFormatter"] = "org/threeten/bp/format/DateTimeFormatter"
        map["java/time/format/DateTimeFormatterBuilder"] = "org/threeten/bp/format/DateTimeFormatterBuilder"
        map["java/time/format/DateTimeParseException"] = "org/threeten/bp/format/DateTimeParseException"
        map["java/time/format/DecimalStyle"] = "org/threeten/bp/format/DecimalStyle"
        map["java/time/format/FormatStyle"] = "org/threeten/bp/format/FormatStyle"
        map["java/time/format/ResolverStyle"] = "org/threeten/bp/format/ResolverStyle"
        map["java/time/format/SignStyle"] = "org/threeten/bp/format/SignStyle"
        map["java/time/format/TextStyle"] = "org/threeten/bp/format/TextStyle"

        // java.time.temporal
        map["java/time/temporal/ChronoField"] = "org/threeten/bp/temporal/ChronoField"
        map["java/time/temporal/ChronoUnit"] = "org/threeten/bp/temporal/ChronoUnit"
        map["java/time/temporal/IsoFields"] = "org/threeten/bp/temporal/IsoFields"
        map["java/time/temporal/JulianFields"] = "org/threeten/bp/temporal/JulianFields"
        map["java/time/temporal/Temporal"] = "org/threeten/bp/temporal/Temporal"
        map["java/time/temporal/TemporalAccessor"] = "org/threeten/bp/temporal/TemporalAccessor"
        map["java/time/temporal/TemporalAdjuster"] = "org/threeten/bp/temporal/TemporalAdjuster"
        map["java/time/temporal/TemporalAdjusters"] = "org/threeten/bp/temporal/TemporalAdjusters"
        map["java/time/temporal/TemporalAmount"] = "org/threeten/bp/temporal/TemporalAmount"
        map["java/time/temporal/TemporalField"] = "org/threeten/bp/temporal/TemporalField"
        map["java/time/temporal/TemporalQueries"] = "org/threeten/bp/temporal/TemporalQueries"
        map["java/time/temporal/TemporalQuery"] = "org/threeten/bp/temporal/TemporalQuery"
        map["java/time/temporal/TemporalUnit"] = "org/threeten/bp/temporal/TemporalUnit"
        map["java/time/temporal/UnsupportedTemporalTypeException"] =
            "org/threeten/bp/temporal/UnsupportedTemporalTypeException"
        map["java/time/temporal/ValueRange"] = "org/threeten/bp/temporal/ValueRange"
        map["java/time/temporal/WeekFields"] = "org/threeten/bp/temporal/WeekFields"

        // java.time.zone
        map["java/time/zone/ZoneOffsetTransition"] = "org/threeten/bp/zone/ZoneOffsetTransition"
        map["java/time/zone/ZoneOffsetTransitionRule"] = "org/threeten/bp/zone/ZoneOffsetTransitionRule"
        map["java/time/zone/ZoneOffsetTransitionRule\$TimeDefinition"] =
            "org/threeten/bp/zone/ZoneOffsetTransitionRule\$TimeDefinition"
        map["java/time/zone/ZoneRules"] = "org/threeten/bp/zone/ZoneRules"
        map["java/time/zone/ZoneRulesException"] = "org/threeten/bp/zone/ZoneRulesException"
        map["java/time/zone/ZoneRulesProvider"] = "org/threeten/bp/zone/ZoneRulesProvider"
    }

    private fun mapMethods(map: MethodMap) {
        // java.util
        map.forType("java/util/Date")
            .redirect("toInstant", "()Ljava/time/Instant;", "org/threeten/bp/DateTimeUtils")
            .redirectStatic("from", "(Ljava/time/Instant;)Ljava/util/Date;", "org/threeten/bp/DateTimeUtils", "toDate")
        map.forType("java/util/Calendar")
            .redirect("toInstant", "()Ljava/time/Instant;", "org/threeten/bp/DateTimeUtils")
            .redirect("toZonedDateTime", "()Ljava/time/ZonedDateTime;", "org/threeten/bp/DateTimeUtils") // TODO
        map.forType("java/util/GregorianCalendar")
            .redirectStatic("from", "(Ljava/time/ZonedDateTime;)Ljava/util/GregorianCalendar;", "org/threeten/bp/DateTimeUtils", "toGregorianCalendar")
        map.forType("java/util/TimeZone")
            .redirect("toZoneId", "()Ljava/time/ZoneId;", "org/threeten/bp/DateTimeUtils")
            .redirectStatic("getTimeZone", "(Ljava/time/ZoneId;)Ljava/util/TimeZone;", "org/threeten/bp/DateTimeUtils", "toTimeZone")

        // java.sql
        map.forType("java/sql/Date")
            .redirect("toLocalDate", "()Ljava/time/LocalDate;", "org/threeten/bp/DateTimeUtils")
            .redirectStatic("valueOf", "(Ljava/time/LocalDate;)Ljava/sql/Date;", "org/threeten/bp/DateTimeUtils", "toSqlDate")
        map.forType("java/sql/Time")
            .redirect("toLocalTime", "()Ljava/time/LocalTime;", "org/threeten/bp/DateTimeUtils")
            .redirectStatic("valueOf", "(Ljava/time/LocalTime;)Ljava/sql/Time;", "org/threeten/bp/DateTimeUtils", "toSqlTime")
        map.forType("java/sql/Timestamp")
            .redirect("toInstant", "()Ljava/time/Instant;", "org/threeten/bp/DateTimeUtils")
            .redirectStatic("from", "(Ljava/time/Instant;)Ljava/sql/Timestamp;", "org/threeten/bp/DateTimeUtils", "toSqlTimestamp")
            .redirect("toLocalDateTime", "()Ljava/time/LocalDateTime;", "org/threeten/bp/DateTimeUtils")
            .redirectStatic("valueOf", "(Ljava/time/LocalDateTime;)Ljava/sql/Timestamp;", "org/threeten/bp/DateTimeUtils", "toSqlTimestamp")
    }
}
