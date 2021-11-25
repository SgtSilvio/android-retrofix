# Android RetroFix

[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/com.github.sgtsilvio.gradle.android-retrofix?color=brightgreen&style=for-the-badge)](https://plugins.gradle.org/plugin/com.github.sgtsilvio.gradle.android-retrofix)

Seamlessly backports Java 8 APIs to Android below API 24 (Android 7.0 Nougat)

Seamless means that you use the official Java 8 APIs, or libraries that internally use the official Java 8 APIs,
and the plugin replaces the official Java 8 APIs with backport libraries only in the generated artifact.
When you increase the minimum Android API level to 24 or higher in the future, 
you will only need to remove the plugin and the backport dependencies.
You do not have to change your code.

Although Android by now supports
[some Java 8 APIs through desugaring](https://developer.android.com/studio/write/java8-support-table),
some important APIs are still not possible to use on Android APIs below 24 - for example `CompletableFuture`.

## Backport Libraries

The following sections list the backported APIs when adding the respective backport library as dependency.

### [android-retrostreams](https://github.com/retrostreams/android-retrostreams)

Dependency:
```groovy
dependencies {
    implementation("net.sourceforge.streamsupport:android-retrostreams:1.7.4")
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
- `java.util.concurrent.ForkJoinWorkerTask`
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
```groovy
dependencies {
    implementation("net.sourceforge.streamsupport:android-retrofuture:1.7.4")
}
```

Backported new types:
- `java.util.concurrent.CompletableFuture`
- `java.util.concurrent.CompletionException`
- `java.util.concurrent.CompletionStage`

### [threetenbp](https://github.com/ThreeTen/threetenbp) or [threetenabp](https://github.com/JakeWharton/ThreeTenABP)

Dependency:
```groovy
dependencies {
    implementation("org.threeten:threetenbp:1.5.1")
    // or 
    implementation("com.jakewharton.threetenabp:threetenabp:1.3.1")
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


## How to Use

Configure your `app/build.gradle` like the following:

```groovy
buildscript {
    repositories {
        google() // necessary as this plugin depends on the android gradle api
        gradlePluginPortal() // where this plugin is hosted
    }
    dependencies {
        classpath 'gradle.plugin.com.github.sgtsilvio.gradle:android-retrofix:0.4.1'
    }
}

apply plugin: 'com.android.application'
apply plugin: 'com.github.sgtsilvio.gradle.android-retrofix'
...

android {
    ...
    defaultConfig {
        ...
        minSdkVersion 21 // has to be < 24, if you have 24+ this plugin is not needed
        ...
    }
    ...
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8 // enables lambdas, method references,
        targetCompatibility JavaVersion.VERSION_1_8 //         default methods, static interface methods
    }
    ...
}

dependencies {
    implementation 'net.sourceforge.streamsupport:android-retrostreams:1.7.4' // for backporting streams
    implementation 'net.sourceforge.streamsupport:android-retrofuture:1.7.4' // for backporting future
    implementation 'org.threeten:threetenbp:1.5.1' // for backporting time
    // or implementation 'com.jakewharton.threetenabp:threetenabp:1.3.1'
    ...
}
```

Android Studio will still display an error "Call requires API level 24 (current min is 21)".
This error is actually just a warning.
Android Studio does not know that we backport the API, so it still thinks that the API can not be used with the 
minSdkVersion.
You can build and run your app without any problems.
If you want to get rid of the warning, just add `@SuppressLint("NewApi")` to the method or class where you use the API.
