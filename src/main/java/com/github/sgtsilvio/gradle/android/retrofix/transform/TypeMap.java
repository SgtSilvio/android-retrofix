package com.github.sgtsilvio.gradle.android.retrofix.transform;

import javassist.ClassMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * @author Silvio Giebl
 */
public class TypeMap extends ClassMap {

    private final @NotNull HashMap<String, String> prefixMap = new HashMap<>();

    @Override
    public @Nullable String get(final @NotNull Object jvmClassName) {
        final String s = (String) jvmClassName;
        for (final Entry<String, String> entry : prefixMap.entrySet()) {
            if (s.startsWith(entry.getKey())) {
                return entry.getValue() + s.substring(entry.getKey().length());
            }
        }
        return super.get(jvmClassName);
    }

    public @Nullable String putPrefix(final @NotNull String oldPrefix, final @NotNull String newPrefix) {
        return prefixMap.put(oldPrefix, newPrefix);
    }
}
