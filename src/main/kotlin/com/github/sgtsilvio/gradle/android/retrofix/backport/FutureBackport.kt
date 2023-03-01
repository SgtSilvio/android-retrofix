package com.github.sgtsilvio.gradle.android.retrofix.backport

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap
import javassist.ClassPool
import org.slf4j.LoggerFactory

/**
 * @author Silvio Giebl
 */
class FutureBackport : Backport {

    companion object {
        private val logger = LoggerFactory.getLogger(FutureBackport::class.java)
    }

    override fun isPresent(classPool: ClassPool) = classPool.find("java9/util/concurrent/CompletableFuture") != null

    override fun apply(typeMap: TypeMap, methodMap: MethodMap) {
        logger.info("Backporting android-retrofuture")
        mapTypes(typeMap)
    }

    private fun mapTypes(map: TypeMap) {
        // java.util.concurrent
        map["java/util/concurrent/CompletableFuture"] = "java9/util/concurrent/CompletableFuture"
        map["java/util/concurrent/CompletionException"] = "java9/util/concurrent/CompletionException"
        map["java/util/concurrent/CompletionStage"] = "java9/util/concurrent/CompletionStage"
    }
}
