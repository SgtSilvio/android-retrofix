# Android RetroFix

Backports Java 8 APIs to Android APIs &lt; 24 (Android 7.0 Nougat)

## [android-retrostreams](https://github.com/retrostreams/android-retrostreams)

Backported new types:
- `java.lang.FuncionalInterface`
- `java.util.DoubleSummaryStatistics`, `java.util.IntSummaryStatistics`, `java.util.LongSummaryStatistics`
- `java.util.Optional**`
- `java.util.PrimitiveIterator**`
- `java.util.Spliterator**`
- `java.util.SplittableRandom`
- `java.util.StringJoiner`
- `java.util.function.**`
- `java.util.stream.**`
- `java.util.concurrent.CountedCompleter`
- `java.util.concurrent.ForkJoinPool`
- `java.util.concurrent.ForkJoinTask`
- `java.util.concurrent.ForkJoinWorkerTask`
- `java.util.concurrent.RecursiveAction`
- `java.util.concurrent.RecursiveTask`
- `java.util.concurrent.ThreadLocalRandom`

Backported static/default methods of:
- `java.lang.Arrays`
- `java.lang.Double`
- `java.lang.Integer`
- `java.lang.Iterable`
- `java.lang.Long`
- `java.lang.Objects`
- `java.util.Collection`
- `java.util.Comparator`
- `java.util.Iterator`
- `java.util.List`
- `java.util.Map`
- `java.util.Map$Entry`
- `java.util.concurrent.ConcurrentMap`

## [android-retrofuture](https://github.com/retrostreams/android-retrostreams)

Backported new types:
- `java.util.concurrent.CompletableFuture`
- `java.util.concurrent.CompletionException`
- `java.util.concurrent.CompletionStage`

# How to use

```groovy
plugins {
    id 'com.android.application'
    id 'com.github.sgtsilvio.gradle.android-retrofix' version '0.1.1'
    ...
}

android {
    compileOptions {
        sourceCompatibility 1.8
        targetCompatibility 1.8
    }
    ...
}

dependencies {
    implementation 'net.sourceforge.streamsupport:android-retrostreams:1.7.1'
    implementation 'net.sourceforge.streamsupport:android-retrofuture:1.7.1'
    ...
}
```
