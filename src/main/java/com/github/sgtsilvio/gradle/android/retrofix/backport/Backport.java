package com.github.sgtsilvio.gradle.android.retrofix.backport;

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap;
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap;
import javassist.ClassPool;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public interface Backport {

    boolean isPresent(@NotNull ClassPool classPool);

    void apply(@NotNull TypeMap typeMap, @NotNull MethodMap methodMap);
}
