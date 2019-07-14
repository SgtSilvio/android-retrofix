package com.github.sgtsilvio.gradle.android.retrofix.backport;

import com.github.sgtsilvio.gradle.android.retrofix.transform.MethodMap;
import com.github.sgtsilvio.gradle.android.retrofix.transform.TypeMap;
import org.jetbrains.annotations.NotNull;

/**
 * @author Silvio Giebl
 */
public interface Backport {

    void map(@NotNull TypeMap typeMap, @NotNull MethodMap methodMap);
}
