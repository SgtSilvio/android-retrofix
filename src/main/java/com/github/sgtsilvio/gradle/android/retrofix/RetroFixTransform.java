package com.github.sgtsilvio.gradle.android.retrofix;

import com.android.build.api.transform.*;
import com.android.build.gradle.BaseExtension;
import com.android.utils.FileUtils;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.taskdefs.Zip;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author Silvio Giebl
 */
class RetroFixTransform extends Transform {

    private final BaseExtension android;

    RetroFixTransform(final BaseExtension android) {
        this.android = android;
    }

    @Override
    public String getName() {
        return "androidRetroFix";
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return ImmutableSet.of(QualifiedContent.DefaultContentType.CLASSES);
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return ImmutableSet.of(
                QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES);
    }

    @Override
    public Set<? super QualifiedContent.Scope> getReferencedScopes() {
        return ImmutableSet.of(
                QualifiedContent.Scope.PROJECT,
                QualifiedContent.Scope.SUB_PROJECTS,
                QualifiedContent.Scope.EXTERNAL_LIBRARIES);
    }

    @Override
    public boolean isIncremental() {
        return true;
    }

    @Override
    public void transform(final TransformInvocation transformInvocation) {
        final Stream<File> depsStream = Stream.concat(
                transformInvocation.getReferencedInputs().stream().map(TransformInput::getJarInputs),
                transformInvocation.getReferencedInputs().stream().map(TransformInput::getDirectoryInputs))
                .flatMap(Collection::stream)
                .map(QualifiedContent::getFile);

        final List<String> deps = Stream.concat(android.getBootClasspath().stream(), depsStream)
                .map(File::getAbsolutePath)
                .collect(Collectors.toList());

        final HashMap<String, String> prefixMap = new HashMap<>();
        final ClassMap classMap = new ClassMap() {
            @Override
            public String get(final Object jvmClassName) {
                final String s = (String) jvmClassName;
                for (final Entry<String, String> entry : prefixMap.entrySet()) {
                    if (s.startsWith(entry.getKey())) {
                        return entry.getValue() + s.substring(entry.getKey().length());
                    }
                }
                return super.get(jvmClassName);
            }
        };
        classMap.put("java/lang/FunctionalInterface", "java9/lang/FunctionalInterface");

        classMap.put("java/util/DoubleSummaryStatistics", "java9/util/DoubleSummaryStatistics");
        classMap.put("java/util/IntSummaryStatistics", "java9/util/IntSummaryStatistics");
        classMap.put("java/util/LongSummaryStatistics", "java9/util/LongSummaryStatistics");
        prefixMap.put("java/util/Optional", "java9/util/Optional");
        prefixMap.put("java/util/PrimitiveIterator", "java9/util/PrimitiveIterator");
        classMap.put("java/util/SplittableRandom", "java9/util/SplittableRandom");
        prefixMap.put("java/util/Spliterator", "java9/util/Spliterator");
        classMap.put("java/util/StringJoiner", "java9/util/StringJoiner");

        prefixMap.put("java/util/function", "java9/util/function");

        prefixMap.put("java/util/stream", "java9/util/stream");

        classMap.put("java/util/concurrent/CompletableFuture", "java9/util/concurrent/CompletableFuture");
        classMap.put("java/util/concurrent/CompletionException", "java9/util/concurrent/CompletionException");
        classMap.put("java/util/concurrent/CompletionStage", "java9/util/concurrent/CompletionStage");
        classMap.put("java/util/concurrent/CountedCompleter", "java9/util/concurrent/CountedCompleter");
        classMap.put("java/util/concurrent/ForkJoinPool", "java9/util/concurrent/ForkJoinPool");
        classMap.put("java/util/concurrent/ForkJoinTask", "java9/util/concurrent/ForkJoinTask");
        classMap.put("java/util/concurrent/ForkJoinWorkerTask", "java9/util/concurrent/ForkJoinWorkerTask");
        classMap.put("java/util/concurrent/RecursiveAction", "java9/util/concurrent/RecursiveAction");
        classMap.put("java/util/concurrent/RecursiveTask", "java9/util/concurrent/RecursiveTask");
        classMap.put("java/util/concurrent/ThreadLocalRandom", "java9/util/concurrent/ThreadLocalRandom");

        // missing: java.util.Base64
        // missing: java.util.concurrent.locks.StampedLock
        // missing: java.io.UncheckedIOException

        final Map<String, RedirectEntry> redirectMap = ImmutableMap.<String, RedirectEntry>builder()
                // java.util.Collection, java.lang.Iterable, java.util.Iterator
                .put("forEach (Ljava/util/function/Consumer;)V", e(
                        "java.lang.Iterable", "java9.lang.Iterables.forEach($0, $$);"))
                .put("forEachRemaining (Ljava/util/function/Consumer;)V", e(
                        "java.util.Iterator", "java9.util.Iterators.forEachRemaining($0, $$);"))
                .put("removeIf (Ljava/util/function/Predicate;)Z", e(
                        "java.util.Collection", "$_ = java9.lang.Iterables.removeIf($0, $$);"))
                .put("spliterator ()Ljava/util/Spliterator;", e(
                        "java.util.Collection", "$_ = java9.util.Spliterators.spliterator($0, $$);",
                        "java.lang.Iterable", "$_ = java9.lang.Iterables.spliterator($0, $$);"))
                .put("stream ()Ljava/util/stream/Stream;", e(
                        "java.util.Collection", "$_ = java9.util.stream.StreamSupport.stream($0, $$);"))
                .put("parallelStream ()Ljava/util/stream/Stream;", e(
                        "java.util.Collection", "$_ = java9.util.stream.StreamSupport.parallelStream($0, $$);"))

                // java.util.List
                .put("replaceAll (Ljava/util/Collection;)V", e(
                        "java.util.List", "java9.util.Lists.replaceAll($0, $$);"))
                .put("sort (Ljava/util/Comparator;)V", e(
                        "java.util.List", "java9.util.Lists.sort($0, $$);"))

                // java.util.Map, java.util.concurrent.ConcurrentMap
                .put("compute (Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", e(
                        "java.util.concurrent.ConcurrentMap", "$_ = java9.util.concurrent.ConcurrentMaps.compute($0, $$);",
                        "java.util.Map", "$_ = java9.util.Maps.compute($0, $$);"))
                .put("computeIfAbsent (Ljava/lang/Object;Ljava/util/function/Function;)Ljava/lang/Object;", e(
                        "java.util.concurrent.ConcurrentMap", "$_ = java9.util.concurrent.ConcurrentMaps.computeIfAbsent($0, $$);",
                        "java.util.Map", "$_ = java9.util.Maps.computeIfAbsent($0, $$);"))
                .put("computeIfPresent (Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", e(
                        "java.util.concurrent.ConcurrentMap", "$_ = java9.util.concurrent.ConcurrentMaps.computeIfPresent($0, $$);",
                        "java.util.Map", "$_ = java9.util.Maps.computeIfPresent($0, $$);"))
                .put("forEach (Ljava/util/function/BiConsumer;)V", e(
                        "java.util.concurrent.ConcurrentMap", "java9.util.concurrent.ConcurrentMaps.forEach($0, $$);",
                        "java.util.Map", "java9.util.Maps.forEach($0, $$);"))
                .put("getOrDefault (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", e(
                        "java.util.concurrent.ConcurrentMap", "$_ = java9.util.concurrent.ConcurrentMaps.getOrDefault($0, $$);",
                        "java.util.Map", "$_ = java9.util.Maps.getOrDefault($0, $$);"))
                .put("merge (Ljava/lang/Object;Ljava/lang/Object;Ljava/util/function/BiFunction;)Ljava/lang/Object;", e(
                        "java.util.concurrent.ConcurrentMap", "$_ = java9.util.concurrent.ConcurrentMaps.merge($0, $$);",
                        "java.util.Map", "$_ = java9.util.Maps.merge($0, $$);"))
                .put("putIfAbsent (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", e(
                        "java.util.Map", "$_ = java9.util.Maps.putIfAbsent($0, $$);"))
                .put("remove (Ljava/lang/Object;Ljava/lang/Object;)Z", e(
                        "java.util.Map", "$_ = java9.util.Maps.remove($0, $$);"))
                .put("replace (Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;", e(
                        "java.util.Map", "$_ = java9.util.Maps.replace($0, $$);"))
                .put("replace (Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)Z", e(
                        "java.util.Map", "$_ = java9.util.Maps.replace($0, $$);"))
                .put("replaceAll (Ljava/util/function/BiFunction;)V", e(
                        "java.util.concurrent.ConcurrentMap", "java9.util.concurrent.ConcurrentMaps.replaceAll($0, $$);",
                        "java.util.Map", "java9.util.Maps.replaceAll($0, $$);"))

                // java.util.Map.Entry
                .put("comparingByKey ()Ljava/util/Comparator;", e(
                        "java.util.Map$Entry", "$_ = java9.util.Maps.Entry.comparingByKey($$);"))
                .put("comparingByKey (Ljava/util/Comparator;)Ljava/util/Comparator;", e(
                        "java.util.Map$Entry", "$_ = java9.util.Maps.Entry.comparingByKey($$);"))
                .put("comparingByValue ()Ljava/util/Comparator;", e(
                        "java.util.Map$Entry", "$_ = java9.util.Maps.Entry.comparingByValue($$);"))
                .put("comparingByValue (Ljava/util/Comparator;)Ljava/util/Comparator;", e(
                        "java.util.Map$Entry", "$_ = java9.util.Maps.Entry.comparingByValue($$);"))

                // java.util.Comparator
                .put("comparing (Ljava/util/function/Function;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.comparing($$);"))
                .put("comparing (Ljava/util/function/Function;Ljava/util/Comparator;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.comparing($$);"))
                .put("comparingInt (Ljava/util/function/ToIntFunction;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.comparingInt($$);"))
                .put("comparingLong (Ljava/util/function/ToLongFunction;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.comparingLong($$);"))
                .put("comparingDouble (Ljava/util/function/ToDoubleFunction;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.comparingDouble($$);"))
                .put("naturalOrder ()Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.naturalOrder($$);"))
                .put("nullsFirst (Ljava/util/Comparator;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.nullsFirst($$);"))
                .put("nullsLast (Ljava/util/Comparator;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.nullsLast($$);"))
                .put("reverseOrder ()Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.reverseOrder($$);"))
                .put("reversed (Ljava/util/Comparator;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.reverseOrder($0, $$);"))
                .put("thenComparing (Ljava/util/Comparator;Ljava/util/Comparator;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.thenComparing($0, $$);"))
                .put("thenComparing (Ljava/util/Comparator;Ljava/util/function/Function;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.thenComparing($0, $$);"))
                .put("thenComparing (Ljava/util/Comparator;Ljava/util/function/Function;Ljava/util/Comparator;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.thenComparing($0, $$);"))
                .put("thenComparingInt (Ljava/util/Comparator;Ljava/util/function/ToIntFunction;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.thenComparingInt($0, $$);"))
                .put("thenComparingLong (Ljava/util/Comparator;Ljava/util/function/ToLongFunction;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.thenComparingLong($0, $$);"))
                .put("thenComparingDouble (Ljava/util/Comparator;Ljava/util/function/ToDoubleFunction;)Ljava/util/Comparator;", e(
                        "java.util.Comparator", "$_ = java9.util.Comparators.thenComparingDouble($0, $$);"))

                // java.util.Objects
                .put("nonNull (Ljava/util/Object;)Z", e(
                        "java.util.Objects", "$_ = java9.util.Objects.nonNull($$);"))
                .put("isNull (Ljava/util/Object;)Z", e(
                        "java.util.Objects", "$_ = java9.util.Objects.isNull($$);"))
                .put("requireNonNull (Ljava/util/Object;Ljava/util/function/Supplier;)Ljava/util/Object;", e(
                        "java.util.Objects", "$_ = java9.util.Objects.requireNonNull($$);"))

                // java.lang.Integer
                .put("compareUnsigned (II)I", e("java.lang.Integer", "$_ = java9.lang.Integers.compareUnsigned($$);"))
                .put("divideUnsigned (II)I", e("java.lang.Integer", "$_ = java9.lang.Integers.divideUnsigned($$);"))
                .put("hashCode (I)I", e("java.lang.Integer", "$_ = java9.lang.Integers.hashCode($$);"))
                .put("max (II)I", e("java.lang.Integer", "$_ = java9.lang.Integers.max($$);"))
                .put("min (II)I", e("java.lang.Integer", "$_ = java9.lang.Integers.min($$);"))
                .put("remainderUnsigned (II)I", e("java.lang.Integer", "$_ = java9.lang.Integers.remainderUnsigned($$);"))
                .put("sum (II)I", e("java.lang.Integer", "$_ = java9.lang.Integers.sum($$);"))
                .put("toUnsignedLong (I)J", e("java.lang.Integer", "$_ = java9.lang.Integers.toUnsignedLong($$);"))
                // missing: toUnsignedString, parseUnsignedInt

                // java.lang.Long
                .put("compareUnsigned (JJ)I", e("java.lang.Long", "$_ = java9.lang.Longs.compareUnsigned($$);"))
                .put("divideUnsigned (JJ)J", e("java.lang.Long", "$_ = java9.lang.Longs.divideUnsigned($$);"))
                .put("hashCode (J)I", e("java.lang.Long", "$_ = java9.lang.Longs.hashCode($$);"))
                .put("max (JJ)J", e("java.lang.Long", "$_ = java9.lang.Longs.max($$);"))
                .put("min (JJ)J", e("java.lang.Long", "$_ = java9.lang.Longs.min($$);"))
                .put("remainderUnsigned (JJ)J", e("java.lang.Long", "$_ = java9.lang.Longs.remainderUnsigned($$);"))
                .put("sum (JJ)J", e("java.lang.Long", "$_ = java9.lang.Longs.sum($$);"))
                // missing: toUnsignedString, parseUnsignedLong

                // java.lang.Double
                .put("hashCode (D)I", e("java.lang.Double", "$_ = java9.lang.Doubles.hashCode($$);"))
                .put("isFinite (D)Z", e("java.lang.Double", "$_ = java9.lang.Doubles.isFinite($$);"))
                .put("max (DD)D", e("java.lang.Double", "$_ = java9.lang.Doubles.max($$);"))
                .put("min (DD)D", e("java.lang.Double", "$_ = java9.lang.Doubles.min($$);"))
                .put("sum (DD)D", e("java.lang.Double", "$_ = java9.lang.Doubles.sum($$);"))

                // java.util.Arrays
                .put("parallelSort ([B)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([BII)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([C)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([CII)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([S)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([SII)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([I)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([III)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([J)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([JII)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([F)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([FII)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([D)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([DII)V", e("java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([Ljava/lang/Comparable;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([Ljava/lang/Comparable;II)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([Ljava/lang/Object;Ljava/util/Comparator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelSort ([Ljava/lang/Object;IILjava/util/Comparator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelSort($$);"))
                .put("parallelPrefix ([ILjava/util/function/IntBinaryOperator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelPrefix($$);"))
                .put("parallelPrefix ([IIILjava/util/function/IntBinaryOperator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelPrefix($$);"))
                .put("parallelPrefix ([JLjava/util/function/LongBinaryOperator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelPrefix($$);"))
                .put("parallelPrefix ([JIILjava/util/function/LongBinaryOperator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelPrefix($$);"))
                .put("parallelPrefix ([DLjava/util/function/DoubleBinaryOperator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelPrefix($$);"))
                .put("parallelPrefix ([DIILjava/util/function/DoubleBinaryOperator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelPrefix($$);"))
                .put("parallelPrefix ([Ljava/lang/Object;Ljava/util/function/BinaryOperator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelPrefix($$);"))
                .put("parallelPrefix ([Ljava/lang/Object;IILjava/util/function/BinaryOperator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelPrefix($$);"))
                .put("setAll ([ILjava/util/function/IntUnaryOperator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.setAll($$);"))
                .put("setAll ([JLjava/util/function/IntToLongFunction;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.setAll($$);"))
                .put("setAll ([DLjava/util/function/IntToDoubleFunction;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.setAll($$);"))
                .put("setAll ([Ljava/lang/Object;Ljava/util/function/IntFunction;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.setAll($$);"))
                .put("parallelSetAll ([ILjava/util/function/IntUnaryOperator;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelSetAll($$);"))
                .put("parallelSetAll ([JLjava/util/function/IntToLongFunction;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelSetAll($$);"))
                .put("parallelSetAll ([DLjava/util/function/IntToDoubleFunction;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelSetAll($$);"))
                .put("parallelSetAll ([Ljava/lang/Object;Ljava/util/function/IntFunction;)V", e(
                        "java.util.Arrays", "java9.util.J8Arrays.parallelSetAll($$);"))
                .put("spliterator ([I)Ljava/util/Spliterator$OfInt;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.spliterator($$);"))
                .put("spliterator ([III)Ljava/util/Spliterator$OfInt;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.spliterator($$);"))
                .put("spliterator ([J)Ljava/util/Spliterator$OfLong;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.spliterator($$);"))
                .put("spliterator ([JII)Ljava/util/Spliterator$OfLong;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.spliterator($$);"))
                .put("spliterator ([D)Ljava/util/Spliterator$OfDouble;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.spliterator($$);"))
                .put("spliterator ([DII)Ljava/util/Spliterator$OfDouble;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.spliterator($$);"))
                .put("spliterator ([Ljava/lang/Object;)Ljava/util/Spliterator;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.spliterator($$);"))
                .put("spliterator ([Ljava/lang/Object;II)Ljava/util/Spliterator;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.spliterator($$);"))
                .put("stream ([I)Ljava/util/stream/IntStream;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.stream($$);"))
                .put("stream ([III)Ljava/util/stream/IntStream;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.stream($$);"))
                .put("stream ([J)Ljava/util/stream/LongStream;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.stream($$);"))
                .put("stream ([JII)Ljava/util/stream/LongStream;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.stream($$);"))
                .put("stream ([D)Ljava/util/stream/DoubleStream;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.stream($$);"))
                .put("stream ([DII)Ljava/util/stream/DoubleStream;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.stream($$);"))
                .put("stream ([Ljava/lang/Object;)Ljava/util/stream/Stream;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.stream($$);"))
                .put("stream ([Ljava/lang/Object;II)Ljava/util/stream/Stream;", e(
                        "java.util.Arrays", "$_ = java9.util.J8Arrays.stream($$);"))

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
                // missing: java.util.concurrent.atomic.AtomicInteger/Long/Reference(Array) getAndUpdate, updateAndGet, getAndAccumulate, accumulateAndGet
                // missing: java.util.zip.ZipFile stream
                // missing: java.util.logging.Logger log, ...
                // missing: java.util.regex.Pattern asPredicate, splitAsStream
                // missing: java.io.BufferedReader lines
                // missing: java.nio.File newBufferedReader, newBufferedWriter, readAllLines, write, list, walk, find, lines
                .build();

        transformInvocation.getInputs().forEach(transformInput -> {
            transformInput.getDirectoryInputs().forEach(directoryInput -> {
                try {
                    final File outputDir = transformInvocation.getOutputProvider().getContentLocation(
                            directoryInput.getName(), directoryInput.getContentTypes(), directoryInput.getScopes(), Format.DIRECTORY);

                    FileUtils.deleteRecursivelyIfExists(outputDir);

                    final ClassPool classPool = new ClassPool();
                    classPool.appendSystemPath();
                    classPool.insertClassPath(directoryInput.getFile().getAbsolutePath());
                    for (final String dep : deps) {
                        classPool.insertClassPath(dep);
                    }

                    final Path inputPath = Paths.get(directoryInput.getFile().getAbsolutePath());
                    Files.walk(inputPath)
                            .filter(Files::isRegularFile)
                            .map(inputPath::relativize)
                            .filter(path -> {
                                if (path.getFileName().toFile().getName().endsWith(".class")) {
                                    return true;
                                }
                                try {
                                    final File file = new File(outputDir, path.toString());
                                    //noinspection ResultOfMethodCallIgnored
                                    file.getParentFile().mkdirs();
                                    Files.copy(path, file.toPath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return false;
                            })
                            .map(Path::toString)
                            .map(s -> s.replaceAll("/", ".").replaceAll("\\\\", "."))
                            .map(s -> s.substring(0, s.length() - ".class".length()))
                            .forEach(s -> transformClass(classPool, s, classMap, redirectMap, outputDir));

                } catch (final Exception e) {
                    e.printStackTrace();
                }
            });
            transformInput.getJarInputs().forEach(jarInput -> {
                try {
                    final File outputDir = transformInvocation.getOutputProvider().getContentLocation(
                            jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.DIRECTORY);
                    final File outputJar = transformInvocation.getOutputProvider().getContentLocation(
                            jarInput.getName(), jarInput.getContentTypes(), jarInput.getScopes(), Format.JAR);

                    if ((jarInput.getStatus() == Status.NOTCHANGED) && outputJar.exists()) {
                        return;
                    }
                    if (jarInput.getName().startsWith("net.sourceforge.streamsupport")) {
                        FileUtils.copyFile(jarInput.getFile(), outputJar);
                        return;
                    }
                    FileUtils.deleteRecursivelyIfExists(outputDir);
                    FileUtils.deleteIfExists(outputJar);

                    final ClassPool classPool = new ClassPool();
                    classPool.appendSystemPath();
                    classPool.insertClassPath(jarInput.getFile().getAbsolutePath());
                    for (final String dep : deps) {
                        classPool.insertClassPath(dep);
                    }

                    final ZipFile zipFile = new ZipFile(jarInput.getFile());
                    zipFile.stream()
                            .filter(entry -> !entry.isDirectory())
                            .filter(entry -> {
                                if (entry.getName().endsWith(".class")) {
                                    return true;
                                }
                                try {
                                    final File file = new File(outputDir, entry.getName());
                                    //noinspection ResultOfMethodCallIgnored
                                    file.getParentFile().mkdirs();
                                    Files.copy(zipFile.getInputStream(entry), file.toPath());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                return false;
                            })
                            .map(ZipEntry::getName)
                            .map(s -> s.replaceAll("/", ".").replaceAll("\\\\", "."))
                            .map(s -> s.substring(0, s.length() - ".class".length()))
                            .forEach(s -> transformClass(classPool, s, classMap, redirectMap, outputDir));

                    zip(outputDir, outputJar);
                    FileUtils.deleteRecursivelyIfExists(outputDir);

                } catch (final Exception e) {
                    e.printStackTrace();
                }
            });
        });
    }

    private static void transformClass(
            final ClassPool classPool,
            final String className,
            final ClassMap classMap,
            final Map<String, RedirectEntry> redirectMap,
            final File outputDir) {

        try {
            final CtClass ctClass = classPool.get(className);

            final HashMap<Integer, String> replaceMap = new HashMap<>();

            ctClass.instrument(new ExprEditor() {
                private int c = 0;

                @Override
                public void edit(final MethodCall m) {
                    final String key = m.getMethodName() + " " + m.getSignature();
                    RedirectEntry redirectEntry = redirectMap.get(key);
                    while (redirectEntry != null) {
                        try {
                            final CtMethod method = m.getMethod();
                            final CtClass declaringClass = method.getDeclaringClass();
                            final boolean matches;
                            if (Modifier.isStatic(method.getModifiers())) {
                                matches = declaringClass.getName().equals(redirectEntry.declaringClass);
                            } else {
                                matches = declaringClass.subtypeOf(
                                        declaringClass.getClassPool().get(redirectEntry.declaringClass));
                            }
                            if (matches) {
                                replaceMap.put(c, redirectEntry.replacement);
                                break;
                            }
                            redirectEntry = redirectEntry.next;
                        } catch (NotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    c++;
                }
            });

            ctClass.getClassFile().renameClass(classMap);

            ctClass.instrument(new ExprEditor() {
                private int c = 0;

                @Override
                public void edit(final MethodCall m) throws CannotCompileException {
                    final String replacement = replaceMap.get(c);
                    if (replacement != null) {
                        m.replace(replacement);
                    }
                    c++;
                }
            });

            ctClass.getClassFile().compact();

            ctClass.writeFile(outputDir.getAbsolutePath());

        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    private static void zip(final File inputDir, final File outputFile) {
        final Project p = new Project();
        p.init();
        final Zip zip = new Zip();
        zip.setProject(p);
        zip.setDestFile(outputFile);
        zip.setBasedir(inputDir);
        zip.perform();
    }

    private static class RedirectEntry {
        final String declaringClass;
        final String replacement;
        final RedirectEntry next;

        RedirectEntry(final String declaringClass, final String replacement) {
            this.declaringClass = declaringClass;
            this.replacement = replacement;
            this.next = null;
        }

        RedirectEntry(final String declaringClass, final String replacement, final RedirectEntry next) {
            this.declaringClass = declaringClass;
            this.replacement = replacement;
            this.next = next;
        }
    }

    private static RedirectEntry e(final String declaringClass, final String replacement) {
        return new RedirectEntry(declaringClass, replacement);
    }

    private static RedirectEntry e(
            final String declaringClass, final String replacement,
            final String declaringClass2, final String replacement2) {

        return new RedirectEntry(declaringClass, replacement, e(declaringClass2, replacement2));
    }
}
