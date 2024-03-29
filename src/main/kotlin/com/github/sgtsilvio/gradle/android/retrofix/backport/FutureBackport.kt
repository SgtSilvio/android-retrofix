package com.github.sgtsilvio.gradle.android.retrofix.backport

import com.github.sgtsilvio.gradle.android.retrofix.transform.ClassMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap

/**
 * @author Silvio Giebl
 */
object FutureBackport : Backport {

    override val indicatorClass get() = "java9/util/concurrent/CompletableFuture"

    override fun isInstrumentable(className: String) = !className.startsWith("java9/")

    override fun apply(classMap: ClassMap, methodMap: MethodMap) {
        mapTypes(classMap)
    }

    private fun mapTypes(map: ClassMap) {
        // java.util.concurrent
        map["java/util/concurrent/CompletableFuture"] = "java9/util/concurrent/CompletableFuture"
        map["java/util/concurrent/CompletableFuture\$AsynchronousCompletionTask"] =
            "java9/util/concurrent/CompletableFuture\$AsynchronousCompletionTask"
        map["java/util/concurrent/CompletionException"] = "java9/util/concurrent/CompletionException"
        map["java/util/concurrent/CompletionStage"] = "java9/util/concurrent/CompletionStage"
    }
}
