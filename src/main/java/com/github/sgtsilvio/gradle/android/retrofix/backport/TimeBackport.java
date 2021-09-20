package com.github.sgtsilvio.gradle.android.retrofix.backport;

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap;
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap;
import javassist.ClassPool;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Silvio Giebl
 */
public class TimeBackport implements Backport {

    private static final @NotNull Logger logger = LoggerFactory.getLogger(TimeBackport.class);

    @Override
    public boolean isPresent(@NotNull final ClassPool classPool) {
        return classPool.find("org/threeten/bp/Clock") != null;
    }

    @Override
    public void apply(final @NotNull TypeMap typeMap, final @NotNull MethodMap methodMap) {
        logger.info("Backporting threetenbp");
        mapTypes(typeMap);
        mapMethods(methodMap);
    }

    private void mapTypes(final @NotNull TypeMap map) {
        // java.time
        map.put("java/time/Clock", "org/threeten/bp/Clock");
        map.put("java/time/DateTimeException", "org/threeten/bp/DateTimeException");
        map.put("java/time/DayOfWeek", "org/threeten/bp/DayOfWeek");
        map.put("java/time/Duration", "org/threeten/bp/Duration");
        map.put("java/time/Instant", "org/threeten/bp/Instant");
        map.put("java/time/LocalDate", "org/threeten/bp/LocalDate");
        map.put("java/time/LocalDateTime", "org/threeten/bp/LocalDateTime");
        map.put("java/time/LocalTime", "org/threeten/bp/LocalTime");
        map.put("java/time/Month", "org/threeten/bp/Month");
        map.put("java/time/MonthDay", "org/threeten/bp/MonthDay");
        map.put("java/time/OffsetDateTime", "org/threeten/bp/OffsetDateTime");
        map.put("java/time/OffsetTime", "org/threeten/bp/OffsetTime");
        map.put("java/time/Period", "org/threeten/bp/Period");
        map.put("java/time/Year", "org/threeten/bp/Year");
        map.put("java/time/YearMonth", "org/threeten/bp/YearMonth");
        map.put("java/time/ZonedDateTime", "org/threeten/bp/ZonedDateTime");
        map.put("java/time/ZoneId", "org/threeten/bp/ZoneId");
        map.put("java/time/ZoneOffset", "org/threeten/bp/ZoneOffset");

        // java.time.chrono
        map.put("java/time/chrono/AbstractChronology", "org/threeten/bp/chrono/AbstractChronology");
        map.put("java/time/chrono/ChronoLocalDate", "org/threeten/bp/chrono/ChronoLocalDate");
        map.put("java/time/chrono/ChronoLocalDateTime", "org/threeten/bp/chrono/ChronoLocalDateTime");
        map.put("java/time/chrono/Chronology", "org/threeten/bp/chrono/Chronology");
        map.put("java/time/chrono/ChronoPeriod", "org/threeten/bp/chrono/ChronoPeriod");
        map.put("java/time/chrono/ChronoZonedDateTime", "org/threeten/bp/chrono/ChronoZonedDateTime");
        map.put("java/time/chrono/Era", "org/threeten/bp/chrono/Era");
        map.put("java/time/chrono/HijrahChronology", "org/threeten/bp/chrono/HijrahChronology");
        map.put("java/time/chrono/HijrahDate", "org/threeten/bp/chrono/HijrahDate");
        map.put("java/time/chrono/HijrahEra", "org/threeten/bp/chrono/HijrahEra");
        map.put("java/time/chrono/IsoChronology", "org/threeten/bp/chrono/IsoChronology");
        map.put("java/time/chrono/IsoEra", "org/threeten/bp/chrono/IsoEra");
        map.put("java/time/chrono/JapaneseChronology", "org/threeten/bp/chrono/JapaneseChronology");
        map.put("java/time/chrono/JapaneseDate", "org/threeten/bp/chrono/JapaneseDate");
        map.put("java/time/chrono/JapaneseEra", "org/threeten/bp/chrono/JapaneseEra");
        map.put("java/time/chrono/MinguoChronology", "org/threeten/bp/chrono/MinguoChronology");
        map.put("java/time/chrono/MinguoDate", "org/threeten/bp/chrono/MinguoDate");
        map.put("java/time/chrono/MinguoEra", "org/threeten/bp/chrono/MinguoEra");
        map.put("java/time/chrono/ThaiBuddhistChronology", "org/threeten/bp/chrono/ThaiBuddhistChronology");
        map.put("java/time/chrono/ThaiBuddhistDate", "org/threeten/bp/chrono/ThaiBuddhistDate");
        map.put("java/time/chrono/ThaiBuddhistEra", "org/threeten/bp/chrono/ThaiBuddhistEra");

        // java.time.format
        map.put("java/time/format/DateTimeFormatter", "org/threeten/bp/format/DateTimeFormatter");
        map.put("java/time/format/DateTimeFormatterBuilder", "org/threeten/bp/format/DateTimeFormatterBuilder");
        map.put("java/time/format/DateTimeParseException", "org/threeten/bp/format/DateTimeParseException");
        map.put("java/time/format/DecimalStyle", "org/threeten/bp/format/DecimalStyle");
        map.put("java/time/format/FormatStyle", "org/threeten/bp/format/FormatStyle");
        map.put("java/time/format/ResolverStyle", "org/threeten/bp/format/ResolverStyle");
        map.put("java/time/format/SignStyle", "org/threeten/bp/format/SignStyle");
        map.put("java/time/format/TextStyle", "org/threeten/bp/format/TextStyle");

        // java.time.temporal
        map.put("java/time/temporal/ChronoField", "org/threeten/bp/temporal/ChronoField");
        map.put("java/time/temporal/ChronoUnit", "org/threeten/bp/temporal/ChronoUnit");
        map.put("java/time/temporal/IsoFields", "org/threeten/bp/temporal/IsoFields");
        map.put("java/time/temporal/JulianFields", "org/threeten/bp/temporal/JulianFields");
        map.put("java/time/temporal/Temporal", "org/threeten/bp/temporal/Temporal");
        map.put("java/time/temporal/TemporalAccessor", "org/threeten/bp/temporal/TemporalAccessor");
        map.put("java/time/temporal/TemporalAdjuster", "org/threeten/bp/temporal/TemporalAdjuster");
        map.put("java/time/temporal/TemporalAdjusters", "org/threeten/bp/temporal/TemporalAdjusters");
        map.put("java/time/temporal/TemporalAmount", "org/threeten/bp/temporal/TemporalAmount");
        map.put("java/time/temporal/TemporalField", "org/threeten/bp/temporal/TemporalField");
        map.put("java/time/temporal/TemporalQueries", "org/threeten/bp/temporal/TemporalQueries");
        map.put("java/time/temporal/TemporalQuery", "org/threeten/bp/temporal/TemporalQuery");
        map.put("java/time/temporal/TemporalUnit", "org/threeten/bp/temporal/TemporalUnit");
        map.put("java/time/temporal/UnsupportedTemporalTypeException", "org/threeten/bp/temporal/UnsupportedTemporalTypeException");
        map.put("java/time/temporal/ValueRange", "org/threeten/bp/temporal/ValueRange");
        map.put("java/time/temporal/WeekFields", "org/threeten/bp/temporal/WeekFields");

        // java.time.zone
        map.put("java/time/zone/ZoneOffsetTransition", "org/threeten/bp/zone/ZoneOffsetTransition");
        map.put("java/time/zone/ZoneOffsetTransitionRule", "org/threeten/bp/zone/ZoneOffsetTransitionRule");
        map.put("java/time/zone/ZoneOffsetTransitionRule.TimeDefinition", "org/threeten/bp/zone/ZoneOffsetTransitionRule.TimeDefinition");
        map.put("java/time/zone/ZoneRules", "org/threeten/bp/zone/ZoneRules");
        map.put("java/time/zone/ZoneRulesException", "org/threeten/bp/zone/ZoneRulesException");
        map.put("java/time/zone/ZoneRulesProvider", "org/threeten/bp/zone/ZoneRulesProvider");
    }

    private void mapMethods(final @NotNull MethodMap map) {
        // java.util
        map.forType("java.util.Date")
                .redirect("toInstant", "()Ljava/time/Instant;", "org.threeten.bp.DateTimeUtils")
                .redirectStatic("from", "(Ljava/time/Instant;)Ljava/util/Date;", "org.threeten.bp.DateTimeUtils", "toDate");
        map.forType("java.util.Calendar")
                .redirect("toInstant", "()Ljava/time/Instant;", "org.threeten.bp.DateTimeUtils");
        map.forType("java.util.GregorianCalendar")
                .redirect("toZonedDateTime", "()Ljava/time/ZonedDateTime;", "org.threeten.bp.DateTimeUtils")
                .redirectStatic("from", "(Ljava/time/ZonedDateTime;)Ljava/util/GregorianCalendar;", "org.threeten.bp.DateTimeUtils", "toGregorianCalendar");
        map.forType("java.util.TimeZone")
                .redirect("toZoneId", "()Ljava/time/ZoneId;", "org.threeten.bp.DateTimeUtils")
                .redirectStatic("getTimeZone", "(Ljava/time/ZoneId;)Ljava/util/TimeZone;", "org.threeten.bp.DateTimeUtils", "toTimeZone");

        // java.sql
        map.forType("java.sql.Date")
                .redirect("toLocalDate", "()Ljava/time/LocalDate;", "org.threeten.bp.DateTimeUtils")
                .redirectStatic("valueOf", "(Ljava/time/LocalDate;)Ljava/sql/Date;", "org.threeten.bp.DateTimeUtils", "toSqlDate");
        map.forType("java.sql.Time")
                .redirect("toLocalTime", "()Ljava/time/LocalTime;", "org.threeten.bp.DateTimeUtils")
                .redirectStatic("valueOf", "(Ljava/time/LocalTime;)Ljava/sql/Time;", "org.threeten.bp.DateTimeUtils", "toSqlTime");
        map.forType("java.sql.Timestamp")
                .redirect("toInstant", "()Ljava/time/Instant;", "org.threeten.bp.DateTimeUtils")
                .redirectStatic("from", "(Ljava/time/Instant;)Ljava/sql/Timestamp;", "org.threeten.bp.DateTimeUtils", "toSqlTimestamp")
                .redirect("toLocalDateTime", "()Ljava/time/LocalDateTime;", "org.threeten.bp.DateTimeUtils")
                .redirectStatic("valueOf", "(Ljava/time/LocalDateTime;)Ljava/sql/Timestamp;", "org.threeten.bp.DateTimeUtils", "toSqlTimestamp");
    }
}
