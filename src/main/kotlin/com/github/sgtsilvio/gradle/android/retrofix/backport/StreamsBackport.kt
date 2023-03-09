package com.github.sgtsilvio.gradle.android.retrofix.backport

import com.github.sgtsilvio.gradle.android.retrofix.transform.ClassMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap

/**
 * @author Silvio Giebl
 */
class StreamsBackport : Backport {

    override val indicatorClass get() = "java9/lang/FunctionalInterface"

    override fun isInstrumentable(className: String) = !className.startsWith("java9/")

    override fun apply(classMap: ClassMap, methodMap: MethodMap) {
        mapTypes(classMap)
        mapMethods(methodMap)
    }

    private fun mapTypes(map: ClassMap) {
        // java.lang
        map["java/lang/FunctionalInterface"] = "java9/lang/FunctionalInterface"

        // java.util
        map["java/util/DoubleSummaryStatistics"] = "java9/util/DoubleSummaryStatistics"
        map["java/util/IntSummaryStatistics"] = "java9/util/IntSummaryStatistics"
        map["java/util/LongSummaryStatistics"] = "java9/util/LongSummaryStatistics"
        map["java/util/Optional"] = "java9/util/Optional"
        map["java/util/OptionalDouble"] = "java9/util/OptionalDouble"
        map["java/util/OptionalInt"] = "java9/util/OptionalInt"
        map["java/util/OptionalLong"] = "java9/util/OptionalLong"
        map["java/util/PrimitiveIterator"] = "java9/util/PrimitiveIterator"
        map["java/util/PrimitiveIterator\$OfDouble"] = "java9/util/PrimitiveIterator\$OfDouble"
        map["java/util/PrimitiveIterator\$OfInt"] = "java9/util/PrimitiveIterator\$OfInt"
        map["java/util/PrimitiveIterator\$OfLong"] = "java9/util/PrimitiveIterator\$OfLong"
        map["java/util/Spliterator"] = "java9/util/Spliterator"
        map["java/util/Spliterator\$OfDouble"] = "java9/util/Spliterator\$OfDouble"
        map["java/util/Spliterator\$OfInt"] = "java9/util/Spliterator\$OfInt"
        map["java/util/Spliterator\$OfLong"] = "java9/util/Spliterator\$OfLong"
        map["java/util/Spliterator\$OfPrimitive"] = "java9/util/Spliterator\$OfPrimitive"
        map["java/util/Spliterators"] = "java9/util/Spliterators"
        map["java/util/Spliterators\$AbstractDoubleSpliterator"] = "java9/util/Spliterators\$AbstractDoubleSpliterator"
        map["java/util/Spliterators\$AbstractIntSpliterator"] = "java9/util/Spliterators\$AbstractIntSpliterator"
        map["java/util/Spliterators\$AbstractLongSpliterator"] = "java9/util/Spliterators\$AbstractLongSpliterator"
        map["java/util/Spliterators\$AbstractSpliterator"] = "java9/util/Spliterators\$AbstractSpliterator"
        map["java/util/SplittableRandom"] = "java9/util/SplittableRandom"
        map["java/util/StringJoiner"] = "java9/util/StringJoiner"

        // java.util.concurrent
        map["java/util/concurrent/CountedCompleter"] = "java9/util/concurrent/CountedCompleter"
        map["java/util/concurrent/ForkJoinPool"] = "java9/util/concurrent/ForkJoinPool"
        map["java/util/concurrent/ForkJoinPool\$ForkJoinWorkerThreadFactory"] =
            "java9/util/concurrent/ForkJoinPool\$ForkJoinWorkerThreadFactory"
        map["java/util/concurrent/ForkJoinPool\$ManagedBlocker"] = "java9/util/concurrent/ForkJoinPool\$ManagedBlocker"
        map["java/util/concurrent/ForkJoinTask"] = "java9/util/concurrent/ForkJoinTask"
        map["java/util/concurrent/ForkJoinWorkerThread"] = "java9/util/concurrent/ForkJoinWorkerThread"
        map["java/util/concurrent/RecursiveAction"] = "java9/util/concurrent/RecursiveAction"
        map["java/util/concurrent/RecursiveTask"] = "java9/util/concurrent/RecursiveTask"
        map["java/util/concurrent/ThreadLocalRandom"] = "java9/util/concurrent/ThreadLocalRandom"

        // java.util.function
        map.putPrefix("java/util/function/", "java9/util/function/")

        // java.util.stream
        map.putPrefix("java/util/stream/", "java9/util/stream/")

        // missing: java.util.Base64
        // missing: java.util.concurrent.locks.StampedLock
        // missing: java.io.UncheckedIOException
    }

    private fun mapMethods(map: MethodMap) {
        map.forOwner("java/lang/Iterable", "java9/lang/Iterables")
            .redirect("forEach", "(Ljava/util/function/Consumer;)V")
            .redirect("spliterator", "()Ljava/util/Spliterator;")
            .redirect("removeIf", "(Ljava/util/function/Predicate;)Z") // Collection.removeIf

        map.forOwner("java/util/Iterator", "java9/util/Iterators")
            .redirect("forEachRemaining", "(Ljava/util/function/Consumer;)V")

        map.forOwner("java/util/Collection", "java9/util/Spliterators")
            .redirect("spliterator", "()Ljava/util/Spliterator;")
        map.forOwner("java/util/Collection", "java9/util/stream/StreamSupport")
            .redirect("stream", "()Ljava/util/stream/Stream;")
            .redirect("parallelStream", "()Ljava/util/stream/Stream;")

        map.forOwner("java/util/List", "java9/util/Lists")
            .redirect("replaceAll", "(Ljava/util/Collection;)V")
            .redirect("sort", "(Ljava/util/Comparator;)V")

        map.forOwner("java/util/Map", "java9/util/Maps")
            .redirect("compute", "(Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;")
            .redirect("computeIfAbsent", "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;")
            .redirect("computeIfPresent", "(Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;")
            .redirect("forEach", "(Ljava/util/function/BiConsumer;)V")
            .redirect("getOrDefault", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
            .redirect("merge", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;")
            .redirect("replaceAll", "(Ljava/util/function/BiFunction;)V")
        map.forOwner("java/util/Map", "java9/util/Maps", listOf("java/util/concurrent/ConcurrentMap"))
            .redirect("putIfAbsent", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
            .redirect("remove", "(Ljava/lang/Object;Ljava/lang/Object;)Z")
            .redirect("replace", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
            .redirect("replace", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Z")

        map.forOwner("java/util/Map\$Entry", "java9/util/Maps\$Entry")
            .redirectStatic("comparingByKey", "()Ljava/util/Comparator;")
            .redirectStatic("comparingByKey", "(Ljava/util/Comparator;)Ljava/util/Comparator;")
            .redirectStatic("comparingByValue", "()Ljava/util/Comparator;")
            .redirectStatic("comparingByValue", "(Ljava/util/Comparator;)Ljava/util/Comparator;")

        map.forOwner("java/util/concurrent.ConcurrentMap", "java9/util/concurrent/ConcurrentMaps")
            .redirect("compute", "(Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;")
            .redirect("computeIfAbsent", "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;")
            .redirect("computeIfPresent", "(Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;")
            .redirect("forEach", "(Ljava/util/function/BiConsumer;)V")
            .redirect("getOrDefault", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;")
            .redirect("merge", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;")
            .redirect("replaceAll", "(Ljava/util/function/BiFunction;)V")

        map.forOwner("java/util/Comparator", "java9/util/Comparators")
            .redirectStatic("comparing", "(Ljava/util/function/Function;)Ljava/util/Comparator;")
            .redirectStatic("comparing", "(Ljava/util/function/Function;Ljava/util/Comparator;)Ljava/util/Comparator;")
            .redirectStatic("comparingInt", "(Ljava/util/function/ToIntFunction;)Ljava/util/Comparator;")
            .redirectStatic("comparingLong", "(Ljava/util/function/ToLongFunction;)Ljava/util/Comparator;")
            .redirectStatic("comparingDouble", "(Ljava/util/function/ToDoubleFunction;)Ljava/util/Comparator;")
            .redirectStatic("naturalOrder", "()Ljava/util/Comparator;")
            .redirectStatic("nullsFirst", "(Ljava/util/Comparator;)Ljava/util/Comparator;")
            .redirectStatic("nullsLast", "(Ljava/util/Comparator;)Ljava/util/Comparator;")
            .redirectStatic("reverseOrder", "()Ljava/util/Comparator;")
            .redirect("thenComparing", "(Ljava/util/function/Function;)Ljava/util/Comparator;")
            .redirect("thenComparing", "(Ljava/util/function/Function;Ljava/util/Comparator;)Ljava/util/Comparator;")
            .redirect("thenComparingInt", "(Ljava/util/function/ToIntFunction;)Ljava/util/Comparator;")
            .redirect("thenComparingLong", "(Ljava/util/function/ToLongFunction;)Ljava/util/Comparator;")
            .redirect("thenComparingDouble", "(Ljava/util/function/ToDoubleFunction;)Ljava/util/Comparator;")
        map.forOwner("java/util/Comparator", "java9/util/Comparators", listOf("java9/util/Comparators\$NullComparator"))
            .redirect("reversed", "(Ljava/util/Comparator;)Ljava/util/Comparator;")
            .redirect("thenComparing", "(Ljava/util/Comparator;)Ljava/util/Comparator;")

        map.forOwner("java/util/Objects", "java9/util/Objects")
            .redirectStatic("nonNull", "(Ljava/util/Object;)Z")
            .redirectStatic("isNull", "(Ljava/util/Object;)Z")
            .redirectStatic("requireNonNull", "(Ljava/util/Object;Ljava/util/function/Supplier;)Ljava/util/Object;")

        map.forOwner("java/lang/Integer", "java9/lang/Integers")
            .redirectStatic("compareUnsigned", "(II)I")
            .redirectStatic("divideUnsigned", "(II)I")
            .redirectStatic("hashCode", "(I)I")
            .redirectStatic("max", "(II)I")
            .redirectStatic("min", "(II)I")
            .redirectStatic("remainderUnsigned", "(II)I")
            .redirectStatic("sum", "(II)I")
            .redirectStatic("toUnsignedLong", "(I)J")
        // missing: toUnsignedString, parseUnsignedInt

        map.forOwner("java/lang/Long", "java9/lang/Longs")
            .redirectStatic("compareUnsigned", "(JJ)I")
            .redirectStatic("divideUnsigned", "(JJ)J")
            .redirectStatic("hashCode", "(J)I")
            .redirectStatic("max", "(JJ)J")
            .redirectStatic("min", "(JJ)J")
            .redirectStatic("remainderUnsigned", "(JJ)J")
            .redirectStatic("sum", "(JJ)J")
        // missing: toUnsignedString, parseUnsignedLong

        map.forOwner("java/lang/Double", "java9/lang/Doubles")
            .redirectStatic("hashCode", "(D)I")
            .redirectStatic("isFinite", "(D)Z")
            .redirectStatic("max", "(DD)D")
            .redirectStatic("min", "(DD)D")
            .redirectStatic("sum", "(DD)D")

        map.forOwner("java/util/Arrays", "java9/util/J8Arrays")
            .redirectStatic("parallelSort", "([B)V")
            .redirectStatic("parallelSort", "([BII)V")
            .redirectStatic("parallelSort", "([C)V")
            .redirectStatic("parallelSort", "([CII)V")
            .redirectStatic("parallelSort", "([S)V")
            .redirectStatic("parallelSort", "([SII)V")
            .redirectStatic("parallelSort", "([I)V")
            .redirectStatic("parallelSort", "([III)V")
            .redirectStatic("parallelSort", "([J)V")
            .redirectStatic("parallelSort", "([JII)V")
            .redirectStatic("parallelSort", "([F)V")
            .redirectStatic("parallelSort", "([FII)V")
            .redirectStatic("parallelSort", "([D)V")
            .redirectStatic("parallelSort", "([DII)V")
            .redirectStatic("parallelSort", "([Ljava/lang/Comparable;)V")
            .redirectStatic("parallelSort", "([Ljava/lang/Comparable;II)V")
            .redirectStatic("parallelSort", "([Ljava/lang/Object;Ljava/util/Comparator;)V")
            .redirectStatic("parallelSort", "([Ljava/lang/Object;IILjava/util/Comparator;)V")
            .redirectStatic("parallelPrefix", "([ILjava/util/function/IntBinaryOperator;)V")
            .redirectStatic("parallelPrefix", "([IIILjava/util/function/IntBinaryOperator;)V")
            .redirectStatic("parallelPrefix", "([JLjava/util/function/LongBinaryOperator;)V")
            .redirectStatic("parallelPrefix", "([JIILjava/util/function/LongBinaryOperator;)V")
            .redirectStatic("parallelPrefix", "([DLjava/util/function/DoubleBinaryOperator;)V")
            .redirectStatic("parallelPrefix", "([DIILjava/util/function/DoubleBinaryOperator;)V")
            .redirectStatic("parallelPrefix", "([Ljava/lang/Object;Ljava/util/function/BinaryOperator;)V")
            .redirectStatic("parallelPrefix", "([Ljava/lang/Object;IILjava/util/function/BinaryOperator;)V")
            .redirectStatic("setAll", "([ILjava/util/function/IntUnaryOperator;)V")
            .redirectStatic("setAll", "([JLjava/util/function/IntToLongFunction;)V")
            .redirectStatic("setAll", "([DLjava/util/function/IntToDoubleFunction;)V")
            .redirectStatic("setAll", "([Ljava/lang/Object;Ljava/util/function/IntFunction;)V")
            .redirectStatic("parallelSetAll", "([ILjava/util/function/IntUnaryOperator;)V")
            .redirectStatic("parallelSetAll", "([JLjava/util/function/IntToLongFunction;)V")
            .redirectStatic("parallelSetAll", "([DLjava/util/function/IntToDoubleFunction;)V")
            .redirectStatic("parallelSetAll", "([Ljava/lang/Object;Ljava/util/function/IntFunction;)V")
            .redirectStatic("spliterator", "([I)Ljava/util/Spliterator\$OfInt;")
            .redirectStatic("spliterator", "([III)Ljava/util/Spliterator\$OfInt;")
            .redirectStatic("spliterator", "([J)Ljava/util/Spliterator\$OfLong;")
            .redirectStatic("spliterator", "([JII)Ljava/util/Spliterator\$OfLong;")
            .redirectStatic("spliterator", "([D)Ljava/util/Spliterator\$OfDouble;")
            .redirectStatic("spliterator", "([DII)Ljava/util/Spliterator\$OfDouble;")
            .redirectStatic("spliterator", "([Ljava/lang/Object;)Ljava/util/Spliterator;")
            .redirectStatic("spliterator", "([Ljava/lang/Object;II)Ljava/util/Spliterator;")
            .redirectStatic("stream", "([I)Ljava/util/stream/IntStream;")
            .redirectStatic("stream", "([III)Ljava/util/stream/IntStream;")
            .redirectStatic("stream", "([J)Ljava/util/stream/LongStream")
            .redirectStatic("stream", "([JII)Ljava/util/stream/LongStream")
            .redirectStatic("stream", "([D)Ljava/util/stream/DoubleStream;")
            .redirectStatic("stream", "([DII)Ljava/util/stream/DoubleStream;")
            .redirectStatic("stream", "([Ljava/lang/Object;)Ljava/util/stream/Stream;")
            .redirectStatic("stream", "([Ljava/lang/Object;II)Ljava/util/stream/Stream;")

        // missing: java.lang.Float hashCode, isFinite, max, min, sum
        // missing: java.lang.Boolean hashCode, logicalAnd, logicalOr, logicalXor
        // missing: java.lang.CharSequence chars, codePoints
        // missing: java.lang.String join
        // missing: java.lang.Math ...
        // missing: java.lang.ThreadLocal withInitial
        // missing: java.math.BigInteger ...
        // missing: java.security.SecureRandom getInstanceStrong
        // missing: java.util.BitSet stream
        // missing: java.util.Collections unmodifiableNavigableSet, unmodifiableNavigableMap, synchronizedNavigableSet, synchronizedNavigableMap,
        //          checkedQueue, checkedNavigableSet, checkedNavigableMap, emptySortedSet, emptyNavigableSet, emptySortedMap, emptyNavigableMap,
        // missing: java.util.Random ints, longs, doubles
        // missing: java.util.concurrent.ConcurrentHashMap ...
        // missing: java.util.concurrent.Executors newWorkStealingPool
        // missing: java.util.concurrent.atomic.AtomicInteger/Long/ReferencArray) getAndUpdate, updateAndGet, getAndAccumulate, accumulateAndGet
        // missing: java.util.zip.ZipFile stream
        // missing: java.util.logging.Logger log, ...
        // missing: java.util.regex.Pattern asPredicate, splitAsStream
        // missing: java.io.BufferedReader lines
        // missing: java.nio.File newBufferedReader, newBufferedWriter, readAllLines, write, list, walk, find, lines
    }
}
