# Android RetroFix

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.github.sgtsilvio.gradle.android-retrofix?color=brightgreen&style=for-the-badge)](https://plugins.gradle.org/plugin/com.github.sgtsilvio.gradle.android-retrofix)
[![GitHub](https://img.shields.io/github/license/sgtsilvio/android-retrofix?color=brightgreen&style=for-the-badge)](LICENSE)
[![GitHub Workflow Status (with branch)](https://img.shields.io/github/actions/workflow/status/sgtsilvio/android-retrofix/check.yml?branch=main&style=for-the-badge)](https://github.com/SgtSilvio/android-retrofix/actions/workflows/check.yml?query=branch%3Amain)

Seamlessly backports Java 8 APIs to Android below API 24 (Android 7.0 Nougat)

Seamless means that you use the official Java 8 APIs, or libraries that internally use the official Java 8 APIs,
and the plugin replaces the official Java 8 APIs with backport libraries only in the generated artifact.
When you increase the minimum Android API level to 24 or higher in the future, 
you will only need to remove the plugin and the backport dependencies.
You do not have to change your code.

Although Android by now supports
[some Java 8 APIs through desugaring](https://developer.android.com/studio/write/java8-support-table),
some important APIs are still not possible to use on Android APIs below 24 - for example `CompletableFuture`.

## How to Use

`settings.gradle(.kts)`:

```kotlin
pluginManagement {
    repositories {
        google() // to retrieve the google android plugins
        gradlePluginPortal() // to retrieve this plugin
    }
}
//...
```

`app/build.gradle(.kts)`:

```kotlin
plugins {
    id("com.android.application")
    id("com.github.sgtsilvio.gradle.android-retrofix") version "1.0.0"
    //...
}

android {
    //...
    defaultConfig {
        //...
        minSdk = 21 // has to be < 24, if you have 24+ this plugin is not needed
        //...
    }
    //...
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8 // enables lambdas, method references,
        targetCompatibility = JavaVersion.VERSION_1_8 //         default methods, static interface methods
    }
    //...
}

dependencies {
    retrofix("net.sourceforge.streamsupport:android-retrostreams:1.7.4") // for backporting streams
    retrofix("net.sourceforge.streamsupport:android-retrofuture:1.7.4") // for backporting future
    retrofix("org.threeten:threetenbp:1.6.5") // for backporting time
    // or retrofix("com.jakewharton.threetenabp:threetenabp:1.4.4")
    //...
}
```

Android Studio will still display an error "Call requires API level 24 (current min is 21)".
This error is actually just a warning.
Android Studio does not know that we backport the API, so it still thinks that the API can not be used with the
minSdkVersion.
You can build and run your app without any problems.
If you want to get rid of the warning, just add `@SuppressLint("NewApi")` to the method or class where you use the API.

## Requirements

- Gradle 7.0 or higher
- Android Gradle Plugin 7.0 or higher

## Supported Backport Libraries

The following sections list the backported APIs when adding the respective backport library as dependency.

### [android-retrostreams](https://github.com/retrostreams/android-retrostreams)

Dependency:
```kotlin
dependencies {
    retrofix("net.sourceforge.streamsupport:android-retrostreams:1.7.4")
}
```

Backported new types:
- `java.lang.FuncionalInterface`
- `java.util.IntSummaryStatistics`, `java.util.DoubleSummaryStatistics`, `java.util.LongSummaryStatistics`
- `java.util.Optional**`
- `java.util.PrimitiveIterator**`
- `java.util.Spliterator**`
- `java.util.SplittableRandom`
- `java.util.StringJoiner`
- `java.util.concurrent.CountedCompleter`
- `java.util.concurrent.ForkJoinPool`
- `java.util.concurrent.ForkJoinTask`
- `java.util.concurrent.ForkJoinWorkerThread`
- `java.util.concurrent.RecursiveAction`
- `java.util.concurrent.RecursiveTask`
- `java.util.concurrent.ThreadLocalRandom`
- `java.util.function.**`
- `java.util.stream.**`

Backported static/default methods of:
- `java.lang.Arrays`
- `java.lang.Integer`, `java.lang.Double`, `java.lang.Long`
- `java.lang.Iterable`
- `java.lang.Objects`
- `java.util.Collection`
- `java.util.Comparator`
- `java.util.Iterator`
- `java.util.List`
- `java.util.Map`
- `java.util.Map$Entry`
- `java.util.concurrent.ConcurrentMap`

### [android-retrofuture](https://github.com/retrostreams/android-retrofuture)

Dependency:
```kotlin
dependencies {
    retrofix("net.sourceforge.streamsupport:android-retrofuture:1.7.4")
}
```

Backported new types:
- `java.util.concurrent.CompletableFuture`
- `java.util.concurrent.CompletionException`
- `java.util.concurrent.CompletionStage`

### [threetenbp](https://github.com/ThreeTen/threetenbp) or [threetenabp](https://github.com/JakeWharton/ThreeTenABP)

Dependency:
```kotlin
dependencies {
    retrofix("org.threeten:threetenbp:1.6.5")
    // or 
    retrofix("com.jakewharton.threetenabp:threetenabp:1.4.4")
}
```

Backported new types:
- `java.time.**`
- `java.time.chrono.**`
- `java.time.format.**`
- `java.time.temporal.**`
- `java.time.zone.**`

Backported conversion methods of:
- `java.util.Date`
- `java.util.Calendar`
- `java.util.GregorianCalendar`
- `java.util.TimeZone`
- `java.sql.Date`
- `java.sql.Time`
- `java.sql.Timestamp`
