package com.github.sgtsilvio.gradle.android.retrofix.backport;

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap;
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public class FutureBackport implements Backport {

    @Override
    public void map(final @NotNull TypeMap typeMap, final @NotNull MethodMap methodMap) {
        mapTypes(typeMap);
    }

    private static void mapTypes(final @NotNull TypeMap map) {
        // java.util.concurrent
        map.put("java/util/concurrent/CompletableFuture", "java9/util/concurrent/CompletableFuture");
        map.put("java/util/concurrent/CompletionException", "java9/util/concurrent/CompletionException");
        map.put("java/util/concurrent/CompletionStage", "java9/util/concurrent/CompletionStage");
    }
}
