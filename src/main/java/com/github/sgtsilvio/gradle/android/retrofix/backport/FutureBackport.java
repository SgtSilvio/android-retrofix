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
public class FutureBackport implements Backport {

    private static final @NotNull Logger logger = LoggerFactory.getLogger(FutureBackport.class);

    @Override
    public boolean isPresent(@NotNull final ClassPool classPool) {
        return classPool.find("java9/util/concurrent/CompletableFuture") != null;
    }

    @Override
    public void apply(final @NotNull TypeMap typeMap, final @NotNull MethodMap methodMap) {
        logger.info("Backporting android-retrofuture");
        mapTypes(typeMap);
    }

    private static void mapTypes(final @NotNull TypeMap map) {
        // java.util.concurrent
        map.put("java/util/concurrent/CompletableFuture", "java9/util/concurrent/CompletableFuture");
        map.put("java/util/concurrent/CompletionException", "java9/util/concurrent/CompletionException");
        map.put("java/util/concurrent/CompletionStage", "java9/util/concurrent/CompletionStage");
    }
}
