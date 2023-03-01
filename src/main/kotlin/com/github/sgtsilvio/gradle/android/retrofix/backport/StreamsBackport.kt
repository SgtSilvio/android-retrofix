package com.github.sgtsilvio.gradle.android.retrofix.backport

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import javassist.ClassPool
import org.slf4j.LoggerFactory

/**
 * @author Silvio Giebl
 */
class StreamsBackport : Backport {

    companion object {
        private val logger = LoggerFactory.getLogger(StreamsBackport::class.java)
    }

    override fun isPresent(classPool: ClassPool) = classPool.find("java9/lang/FunctionalInterface") != null

    override fun apply(typeMap: TypeMap, methodMap: MethodMap) {
        logger.info("Backporting android-retrostreams")
        mapTypes(typeMap)
        mapMethods(methodMap)
    }

    private fun mapTypes(map: TypeMap) {
        // java.lang
        map["java/lang/FunctionalInterface"] = "java9/lang/FunctionalInterface"

        // java.util
        map["java/util/DoubleSummaryStatistics"] = "java9/util/DoubleSummaryStatistics"
        map["java/util/IntSummaryStatistics"] = "java9/util/IntSummaryStatistics"
        map["java/util/LongSummaryStatistics"] = "java9/util/LongSummaryStatistics"
        map.putPrefix("java/util/Optional", "java9/util/Optional")
        map.putPrefix("java/util/PrimitiveIterator", "java9/util/PrimitiveIterator")
        map["java/util/SplittableRandom"] = "java9/util/SplittableRandom"
        map.putPrefix("java/util/Spliterator", "java9/util/Spliterator")
        map["java/util/StringJoiner"] = "java9/util/StringJoiner"

        // java.util.concurrent
        map["java/util/concurrent/CountedCompleter"] = "java9/util/concurrent/CountedCompleter"
        map["java/util/concurrent/ForkJoinPool"] = "java9/util/concurrent/ForkJoinPool"
        map["java/util/concurrent/ForkJoinTask"] = "java9/util/concurrent/ForkJoinTask"
        map["java/util/concurrent/ForkJoinWorkerTask"] = "java9/util/concurrent/ForkJoinWorkerTask"
        map["java/util/concurrent/RecursiveAction"] = "java9/util/concurrent/RecursiveAction"
        map["java/util/concurrent/RecursiveTask"] = "java9/util/concurrent/RecursiveTask"
        map["java/util/concurrent/ThreadLocalRandom"] = "java9/util/concurrent/ThreadLocalRandom"

        // java.util.function
        map.putPrefix("java/util/function", "java9/util/function")

        // java.util.stream
        map.putPrefix("java/util/stream", "java9/util/stream")

        // missing: java.util.Base64
        // missing: java.util.concurrent.locks.StampedLock
        // missing: java.io.UncheckedIOException
    }

    private fun mapMethods(map: MethodMap) {
        map.forType("java.lang.Iterable")
            .redirect("forEach", "(Ljava/util/function/Consumer;)V", "java9.lang.Iterables")
            .redirect("spliterator", "()Ljava/util/Spliterator;", "java9.lang.Iterables")

        map.forType("java.util.Iterator")
            .redirect("forEachRemaining", "(Ljava/util/function/Consumer;)V", "java9.util.Iterators")

        map.forType("java.util.Collection")
            .redirect("removeIf", "(Ljava/util/function/Predicate;)Z", "java9.lang.Iterables")
            .redirect("spliterator", "()Ljava/util/Spliterator;", "java9.util.Spliterators")
            .redirect("stream", "()Ljava/util/stream/Stream;", "java9.util.stream.StreamSupport")
            .redirect("parallelStream", "()Ljava/util/stream/Stream;", "java9.util.stream.StreamSupport")

        map.forType("java.util.List")
            .redirect("replaceAll", "(Ljava/util/Collection;)V", "java9.util.Lists")
            .redirect("sort", "(Ljava/util/Comparator;)V", "java9.util.Lists")

        map.forType("java.util.Map")
            .redirect("compute", "(Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", "java9.util.Maps")
            .redirect("computeIfAbsent", "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", "java9.util.Maps")
            .redirect("computeIfPresent", "(Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", "java9.util.Maps")
            .redirect("forEach", "(Ljava/util/function/BiConsumer;)V", "java9.util.Maps")
            .redirect("getOrDefault", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "java9.util.Maps")
            .redirect("merge", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", "java9.util.Maps")
            .redirect("putIfAbsent", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "java9.util.Maps")
            .redirect("remove", "(Ljava/lang/Object;Ljava/lang/Object;)Z", "java9.util.Maps")
            .redirect("replace", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "java9.util.Maps")
            .redirect("replace", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Z", "java9.util.Maps")
            .redirect("replaceAll", "(Ljava/util/function/BiFunction;)V", "java9.util.Maps")

        map.forType("java.util.Map\$Entry")
            .redirectStatic("comparingByKey", "()Ljava/util/Comparator;", "java9.util.Maps.Entry")
            .redirectStatic("comparingByKey", "(Ljava/util/Comparator;)Ljava/util/Comparator;", "java9.util.Maps.Entry")
            .redirectStatic("comparingByValue", "()Ljava/util/Comparator;", "java9.util.Maps.Entry")
            .redirectStatic("comparingByValue", "(Ljava/util/Comparator;)Ljava/util/Comparator;", "java9.util.Maps.Entry")

        map.forType("java.util.concurrent.ConcurrentMap")
            .redirect("compute", "(Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", "java9.util.concurrent.ConcurrentMaps")
            .redirect("computeIfAbsent", "(Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", "java9.util.concurrent.ConcurrentMaps")
            .redirect("computeIfPresent", "(Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", "java9.util.concurrent.ConcurrentMaps")
            .redirect("forEach", "(Ljava/util/function/BiConsumer;)V", "java9.util.concurrent.ConcurrentMaps")
            .redirect("getOrDefault", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", "java9.util.concurrent.ConcurrentMaps")
            .redirect("merge", "(Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", "java9.util.concurrent.ConcurrentMaps")
            .redirect("replaceAll", "(Ljava/util/function/BiFunction;)V", "java9.util.concurrent.ConcurrentMaps")

        map.forType("java.util.Comparator")
            .redirectStatic("comparing", "(Ljava/util/function/Function;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirectStatic("comparing", "(Ljava/util/function/Function;Ljava/util/Comparator;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirectStatic("comparingInt", "(Ljava/util/function/ToIntFunction;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirectStatic("comparingLong", "(Ljava/util/function/ToLongFunction;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirectStatic("comparingDouble", "(Ljava/util/function/ToDoubleFunction;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirectStatic("naturalOrder", "()Ljava/util/Comparator;", "java9.util.Comparators")
            .redirectStatic("nullsFirst", "(Ljava/util/Comparator;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirectStatic("nullsLast", "(Ljava/util/Comparator;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirectStatic("reverseOrder", "()Ljava/util/Comparator;", "java9.util.Comparators")
            .redirect("reversed", "(Ljava/util/Comparator;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirect("thenComparing", "(Ljava/util/Comparator;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirect("thenComparing", "(Ljava/util/function/Function;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirect("thenComparing", "(Ljava/util/function/Function;Ljava/util/Comparator;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirect("thenComparingInt", "(Ljava/util/function/ToIntFunction;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirect("thenComparingLong", "(Ljava/util/function/ToLongFunction;)Ljava/util/Comparator;", "java9.util.Comparators")
            .redirect("thenComparingDouble", "(Ljava/util/function/ToDoubleFunction;)Ljava/util/Comparator;", "java9.util.Comparators")

        map.forType("java.util.Objects")
            .redirectStatic("nonNull", "(Ljava/util/Object;)Z", "java9.util.Objects")
            .redirectStatic("isNull", "(Ljava/util/Object;)Z", "java9.util.Objects")
            .redirectStatic("requireNonNull", "(Ljava/util/Object;Ljava/util/function/Supplier;)Ljava/util/Object;", "java9.util.Objects")

        map.forType("java.lang.Integer")
            .redirectStatic("compareUnsigned", "(II)I", "java9.lang.Integers")
            .redirectStatic("divideUnsigned", "(II)I", "java9.lang.Integers")
            .redirectStatic("hashCode", "(I)I", "java9.lang.Integers")
            .redirectStatic("max", "(II)I", "java9.lang.Integers")
            .redirectStatic("min", "(II)I", "java9.lang.Integers")
            .redirectStatic("remainderUnsigned", "(II)I", "java9.lang.Integers")
            .redirectStatic("sum", "(II)I", "java9.lang.Integers")
            .redirectStatic("toUnsignedLong", "(I)J", "java9.lang.Integers")
        // missing: toUnsignedString, parseUnsignedInt

        map.forType("java.lang.Long")
            .redirectStatic("compareUnsigned", "(JJ)I", "java9.lang.Longs")
            .redirectStatic("divideUnsigned", "(JJ)J", "java9.lang.Longs")
            .redirectStatic("hashCode", "(J)I", "java9.lang.Longs")
            .redirectStatic("max", "(JJ)J", "java9.lang.Longs")
            .redirectStatic("min", "(JJ)J", "java9.lang.Longs")
            .redirectStatic("remainderUnsigned", "(JJ)J", "java9.lang.Longs")
            .redirectStatic("sum", "(JJ)J", "java9.lang.Longs")
        // missing: toUnsignedString, parseUnsignedLong

        map.forType("java.lang.Double")
            .redirectStatic("hashCode", "(D)I", "java9.lang.Doubles")
            .redirectStatic("isFinite", "(D)Z", "java9.lang.Doubles")
            .redirectStatic("max", "(DD)D", "java9.lang.Doubles")
            .redirectStatic("min", "(DD)D", "java9.lang.Doubles")
            .redirectStatic("sum", "(DD)D", "java9.lang.Doubles")

        map.forType("java.util.Arrays")
            .redirectStatic("parallelSort", "([B)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([BII)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([C)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([CII)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([S)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([SII)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([I)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([III)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([J)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([JII)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([F)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([FII)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([D)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([DII)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([Ljava/lang/Comparable;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([Ljava/lang/Comparable;II)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([Ljava/lang/Object;Ljava/util/Comparator;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSort", "([Ljava/lang/Object;IILjava/util/Comparator;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelPrefix", "([ILjava/util/function/IntBinaryOperator;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelPrefix", "([IIILjava/util/function/IntBinaryOperator;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelPrefix", "([JLjava/util/function/LongBinaryOperator;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelPrefix", "([JIILjava/util/function/LongBinaryOperator;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelPrefix", "([DLjava/util/function/DoubleBinaryOperator;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelPrefix", "([DIILjava/util/function/DoubleBinaryOperator;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelPrefix", "([Ljava/lang/Object;Ljava/util/function/BinaryOperator;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelPrefix", "([Ljava/lang/Object;IILjava/util/function/BinaryOperator;)V", "java9.util.J8Arrays")
            .redirectStatic("setAll", "([ILjava/util/function/IntUnaryOperator;)V", "java9.util.J8Arrays")
            .redirectStatic("setAll", "([JLjava/util/function/IntToLongFunction;)V", "java9.util.J8Arrays")
            .redirectStatic("setAll", "([DLjava/util/function/IntToDoubleFunction;)V", "java9.util.J8Arrays")
            .redirectStatic("setAll", "([Ljava/lang/Object;Ljava/util/function/IntFunction;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSetAll", "([ILjava/util/function/IntUnaryOperator;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSetAll", "([JLjava/util/function/IntToLongFunction;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSetAll", "([DLjava/util/function/IntToDoubleFunction;)V", "java9.util.J8Arrays")
            .redirectStatic("parallelSetAll", "([Ljava/lang/Object;Ljava/util/function/IntFunction;)V", "java9.util.J8Arrays")
            .redirectStatic("spliterator", "([I)Ljava/util/Spliterator\$OfInt;", "java9.util.J8Arrays")
            .redirectStatic("spliterator", "([III)Ljava/util/Spliterator\$OfInt;", "java9.util.J8Arrays")
            .redirectStatic("spliterator", "([J)Ljava/util/Spliterator\$OfLong;", "java9.util.J8Arrays")
            .redirectStatic("spliterator", "([JII)Ljava/util/Spliterator\$OfLong;", "java9.util.J8Arrays")
            .redirectStatic("spliterator", "([D)Ljava/util/Spliterator\$OfDouble;", "java9.util.J8Arrays")
            .redirectStatic("spliterator", "([DII)Ljava/util/Spliterator\$OfDouble;", "java9.util.J8Arrays")
            .redirectStatic("spliterator", "([Ljava/lang/Object;)Ljava/util/Spliterator;", "java9.util.J8Arrays")
            .redirectStatic("spliterator", "([Ljava/lang/Object;II)Ljava/util/Spliterator;", "java9.util.J8Arrays")
            .redirectStatic("stream", "([I)Ljava/util/stream/IntStream;", "java9.util.J8Arrays")
            .redirectStatic("stream", "([III)Ljava/util/stream/IntStream;", "java9.util.J8Arrays")
            .redirectStatic("stream", "([J)Ljava/util/stream/LongStream", "java9.util.J8Arrays")
            .redirectStatic("stream", "([JII)Ljava/util/stream/LongStream", "java9.util.J8Arrays")
            .redirectStatic("stream", "([D)Ljava/util/stream/DoubleStream;", "java9.util.J8Arrays")
            .redirectStatic("stream", "([DII)Ljava/util/stream/DoubleStream;", "java9.util.J8Arrays")
            .redirectStatic("stream", "([Ljava/lang/Object;)Ljava/util/stream/Stream;", "java9.util.J8Arrays")
            .redirectStatic("stream", "([Ljava/lang/Object;II)Ljava/util/stream/Stream;", "java9.util.J8Arrays")

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
