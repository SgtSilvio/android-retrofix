package com.github.sgtsilvio.gradle.android.retrofix.backport;

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap;
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap;
import javassist.ClassPool;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public interface Backport {

    void apply(@NotNull ClassPool classPool, @NotNull TypeMap typeMap, @NotNull MethodMap methodMap);
}
